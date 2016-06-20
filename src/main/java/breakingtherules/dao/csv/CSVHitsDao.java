package breakingtherules.dao.csv;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import breakingtherules.dao.DaoUtilities;
import breakingtherules.dao.HitsDao;
import breakingtherules.dao.UniqueHit;
import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;
import breakingtherules.utilities.Cache;
import breakingtherules.utilities.HeavySynchronizedHashCache;
import breakingtherules.utilities.MutableInteger;
import breakingtherules.utilities.Triple;
import breakingtherules.utilities.Triple.UnmodifiableTriple;

/**
 * The CSVHitsDao is a basic DAO that only parse hits, line by line from CSV
 * files.
 * <p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * 
 * @see CSVParser
 */
public class CSVHitsDao implements HitsDao {

    /**
     * Cache for loaded unique hits.
     */
    private final Cache<String, List<UniqueHit>> m_cacheHits;

    /**
     * Cache for hits number by filter and rules.
     * <p>
     * The 'number of hits' cache is keyed by the hits jobName, rules (which are
     * stored in a set, because there order doesn't change anything) and filter.
     */
    private final Cache<UnmodifiableTriple<String, Set<Rule>, Filter>, Integer> m_totalHitsCache;

    /**
     * Construct new CSVHitsDao.
     */
    public CSVHitsDao() {
	m_cacheHits = new HeavySynchronizedHashCache<>();
	m_totalHitsCache = new HeavySynchronizedHashCache<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initJob(final String jobName, final List<Hit> hits) throws IOException {
	String repoPath = CSVDaoConfig.getHitsFile(jobName);
	try {
	    CSVParser.toCSV(CSVParser.DEFAULT_COLUMNS_TYPES, hits, repoPath);
	} catch (final CSVParseException e) {
	    throw new IllegalArgumentException(e);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListDto<Hit> getHits(final String jobName, final List<Rule> rules, final Filter filter)
	    throws IOException, CSVParseException {
	final List<Hit> hits = new ArrayList<>();
	CSVParser.parseHits(CSVParser.DEFAULT_COLUMNS_TYPES, CSVDaoConfig.getHitsFile(jobName), rules, filter, hits);
	// TODO - take the opportunity and add hits to cache.
	final int size = hits.size();

	// Create new list of the rules to clone the list - so modifications
	// on the original list will not change the list saved in the cache
	m_totalHitsCache.add(
		new UnmodifiableTriple<>(jobName, Collections.unmodifiableSet(new HashSet<>(rules)), filter),
		Integer.valueOf(size));
	return new ListDto<>(hits, 0, size, size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<UniqueHit> getUniqueHits(final String jobName, final List<Rule> rules, final Filter filter)
	    throws CSVParseException, IOException {
	final List<UniqueHit> uniqueHits = getUniqueHitsInternal(CSVDaoConfig.getHitsFile(jobName));

	if (rules.isEmpty() && Filter.ANY_FILTER.equals(filter)) {
	    // No filter is needed.
	    return uniqueHits;
	}

	// Filter hits by the rules and filter.
	final List<UniqueHit> filteredHits = new ArrayList<>();
	for (final UniqueHit hit : uniqueHits) {
	    if (DaoUtilities.isMatch(hit, rules, filter)) {
		filteredHits.add(hit);
	    }
	}
	return filteredHits;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getHitsNumber(final String jobName, final List<Rule> rules, final Filter filter)
	    throws IOException, CSVParseException {
	try {
	    return m_totalHitsCache.getOrAdd(
		    new UnmodifiableTriple<>(jobName, Collections.unmodifiableSet(new HashSet<>(rules)), filter),
		    HITS_NUMBER_SUPPLIER).intValue();
	} catch (final UncheckedIOException e) {
	    throw e.getCause();
	} catch (final UncheckedCSVParseException e) {
	    throw e.getCause();
	}
    }

    /**
     * Get unique hits, used internally.
     * <p>
     * 
     * @param fileName
     *            the name of the input file.
     * @return set of unique hits, parsed from the files or returned from cache.
     * @throws CSVParseException
     *             if the data in the file is invalid.
     * @throws IOException
     *             if any I/O errors occurs.
     */
    private List<UniqueHit> getUniqueHitsInternal(final String fileName) throws CSVParseException, IOException {
	try {
	    return m_cacheHits.getOrAdd(fileName, UNIQUE_HITS_SUPPLIER);
	} catch (final UncheckedIOException e) {
	    throw e.getCause();
	} catch (final UncheckedCSVParseException e) {
	    throw e.getCause();
	}
    }

    /**
     * Supplier function of the 'hits number'.
     * <p>
     * 
     * @see #getHitsNumber(String, List, Filter)
     */
    private final Function<Triple<String, Set<Rule>, Filter>, Integer> HITS_NUMBER_SUPPLIER = triple -> {
	try {
	    final String jobName = triple.getFirst();
	    final Set<Rule> rules = triple.getSecond();
	    final Filter filter = triple.getThird();
	    final List<UniqueHit> uniqueHits = getUniqueHitsInternal(CSVDaoConfig.getHitsFile(jobName));
	    int hitsNumber = 0;
	    for (final UniqueHit hit : uniqueHits) {
		if (DaoUtilities.isMatch(hit, rules, filter)) {
		    hitsNumber += hit.getAmount();
		}
	    }
	    return Integer.valueOf(hitsNumber);
	} catch (final IOException e) {
	    throw new UncheckedIOException(e);
	} catch (final CSVParseException e) {
	    throw new UncheckedCSVParseException(e);
	}
    };

    /**
     * Supplier function of unique hits.
     * <p>
     * 
     * @see CSVHitsDao#getUniqueHitsInternal(String)
     */
    private static final Function<String, List<UniqueHit>> UNIQUE_HITS_SUPPLIER = fileName -> {
	final Map<Hit, MutableInteger> hitsCount = new HashMap<>();
	try {
	    CSVParser.parseHits(CSVParser.DEFAULT_COLUMNS_TYPES, fileName, Collections.emptyList(), Filter.ANY_FILTER,
		    new AbstractSet<Hit>() {

		@Override
		public boolean add(final Hit hit) {
		    hitsCount.computeIfAbsent(hit, MutableInteger.zeroInitializerFunction).value++;
		    return true;
		}

		@Override
		public Iterator<Hit> iterator() {
		    return hitsCount.keySet().iterator();
		}

		@Override
		public int size() {
		    return hitsCount.size();
		}

	    });
	} catch (final IOException e) {
	    throw new UncheckedIOException(e);
	} catch (final CSVParseException e) {
	    throw new UncheckedCSVParseException(e);
	}

	final List<UniqueHit> uniqueHits = new ArrayList<>(hitsCount.size());
	for (final Iterator<Map.Entry<Hit, MutableInteger>> it = hitsCount.entrySet().iterator(); it.hasNext();) {
	    final Map.Entry<Hit, MutableInteger> entry = it.next();
	    final Hit hit = entry.getKey();
	    uniqueHits.add(new UniqueHit(hit, entry.getValue().value));
	    it.remove();
	}
	// Don't let anyone change the cache
	return Collections.unmodifiableList(uniqueHits);
    };

}
