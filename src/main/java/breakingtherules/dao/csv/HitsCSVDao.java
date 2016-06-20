package breakingtherules.dao.csv;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.AbstractSet;
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
 * The HitsCSVDao is a basic DAO that only parse hits, line by line from CSV
 * files.
 * <p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * 
 * @see CSVParser
 *
 */
public class HitsCSVDao implements HitsDao {

    /**
     * Cache for loaded unique hits.
     */
    private final Cache<String, Set<UniqueHit>> m_cacheHits;

    /**
     * Cache for hits number by filter and rules.
     * <p>
     * The 'number of hits' cache is keyed by the hits jobName, rules (which are
     * stored in a set, because there order doesn't change anything) and filter.
     */
    private final Cache<UnmodifiableTriple<String, Set<Rule>, Filter>, Integer> m_totalHitsCache;

    /**
     * Construct new HitsCSVDao.
     */
    public HitsCSVDao() {
	m_cacheHits = new HeavySynchronizedHashCache<>();
	m_totalHitsCache = new HeavySynchronizedHashCache<>();
    }
    
    @Override
    public void initJob(String jobName, List<Hit> hits) throws IllegalArgumentException, IOException {
	try {
	    String repoPath = CSVDaoConfig.getHitsFile(jobName);
	    CSVParser.toCSV(CSVParser.DEFAULT_COLUMNS_TYPES, hits, repoPath);
	} catch (CSVParseException e) {
	    throw new IOException(e);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.dao.HitsDao#getHits(java.lang.String,
     * java.util.List, breakingtherules.firewall.Filter)
     */
    @Override
    public ListDto<Hit> getHits(final String jobName, final List<Rule> rules, final Filter filter)
	    throws IOException, CSVParseException {
	final List<Hit> hits = CSVParser.fromCSV(CSVParser.DEFAULT_COLUMNS_TYPES, CSVDaoConfig.getHitsFile(jobName),
		rules, filter);
	// TODO - take the opportunity and add hits to cache.
	final int size = hits.size();

	// Create new list of the rules to clone the list - so modifications
	// on the original list will not change the list saved in the cache
	m_totalHitsCache.add(
		new UnmodifiableTriple<>(jobName, Collections.unmodifiableSet(new HashSet<>(rules)), filter),
		Integer.valueOf(size));
	return new ListDto<>(hits, 0, size, size);
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.dao.HitsDao#getUnique(java.lang.String,
     * java.util.List, breakingtherules.firewall.Filter)
     */
    @Override
    public Set<UniqueHit> getUnique(final String jobName, final List<Rule> rules, final Filter filter)
	    throws CSVParseException, IOException {
	final Set<UniqueHit> uniqueHits = getUniqueHitsInternal(CSVDaoConfig.getHitsFile(jobName));

	if (rules.isEmpty() && Filter.ANY_FILTER.equals(filter)) {
	    return uniqueHits;
	}

	final Set<UniqueHit> filteredHits = new HashSet<>();
	for (final UniqueHit hit : uniqueHits) {
	    if (DaoUtilities.isMatch(hit, rules, filter)) {
		filteredHits.add(hit);
	    }
	}
	return filteredHits;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.dao.HitsDao#getHitsNumber(java.lang.String,
     * java.util.List, breakingtherules.firewall.Filter)
     */
    @Override
    public int getHitsNumber(final String jobName, final List<Rule> rules, final Filter filter)
	    throws IOException, CSVParseException {
	try {
	    return m_totalHitsCache.getOrAdd(
		    new UnmodifiableTriple<>(jobName, Collections.unmodifiableSet(new HashSet<>(rules)), filter),
		    HITS_NUMBER_SUPPLIER).intValue();
	} catch (UncheckedIOException e) {
	    throw e.getCause();
	} catch (UncheckedCSVParseException e) {
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
    private Set<UniqueHit> getUniqueHitsInternal(final String fileName) throws CSVParseException, IOException {
	try {
	    return m_cacheHits.getOrAdd(fileName, UNIQUE_HITS_SUPPLIER);
	} catch (UncheckedIOException e) {
	    throw e.getCause();
	} catch (UncheckedCSVParseException e) {
	    throw e.getCause();
	}
    }

    /**
     * Supplier function of the 'hits number'.
     * <p>
     * 
     * @see #getHitsNumber(String, List, Filter)
     */
    private final Function<Triple<String, Set<Rule>, Filter>, Integer> HITS_NUMBER_SUPPLIER = (
	    final Triple<String, Set<Rule>, Filter> triple) -> {
	try {
	    final String jobName = triple.getFirst();
	    final Set<Rule> rules = triple.getSecond();
	    final Filter filter = triple.getThird();
	    final Set<UniqueHit> uniqueHits = getUniqueHitsInternal(CSVDaoConfig.getHitsFile(jobName));
	    int hitsNumber = 0;
	    for (final UniqueHit hit : uniqueHits) {
		if (DaoUtilities.isMatch(hit, rules, filter)) {
		    hitsNumber += hit.getAmount();
		}
	    }
	    return Integer.valueOf(hitsNumber);
	} catch (IOException e) {
	    throw new UncheckedIOException(e);
	} catch (CSVParseException e) {
	    throw new UncheckedCSVParseException(e);
	}
    };

    /**
     * Supplier function of unique hits.
     * <p>
     * 
     * @see HitsCSVDao#getUniqueHitsInternal(String)
     */
    private static final Function<String, Set<UniqueHit>> UNIQUE_HITS_SUPPLIER = (final String fileName) -> {
	final Map<Hit, MutableInteger> hitsCount = new HashMap<>();
	try {
	    CSVParser.fromCSV(CSVParser.DEFAULT_COLUMNS_TYPES, fileName, Collections.emptyList(), Filter.ANY_FILTER,
		    new AbstractSet<Hit>() {

		@Override
		public boolean add(final Hit hit) {
		    MutableInteger count = hitsCount.get(hit);
		    if (count == null) {
			count = new MutableInteger(1);
			hitsCount.put(hit, count);
		    } else {
			count.value++;
		    }
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
	} catch (IOException e) {
	    throw new UncheckedIOException(e);
	} catch (CSVParseException e) {
	    throw new UncheckedCSVParseException(e);
	}
	final Set<UniqueHit> uniqueHits = new HashSet<>();
	for (final Iterator<Map.Entry<Hit, MutableInteger>> it = hitsCount.entrySet().iterator(); it.hasNext();) {
	    final Map.Entry<Hit, MutableInteger> entry = it.next();
	    final Hit hit = entry.getKey();
	    uniqueHits.add(new UniqueHit(hit, entry.getValue().value));
	    it.remove();
	}
	// Don't let anyone change the cache
	return Collections.unmodifiableSet(uniqueHits);
    };

}
