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

import breakingtherules.dao.HitsDao;
import breakingtherules.dao.UniqueHit;
import breakingtherules.dao.UtilityDao;
import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;
import breakingtherules.utilities.Cache;
import breakingtherules.utilities.SynchronizedHashCache;
import breakingtherules.utilities.Triple;
import breakingtherules.utilities.Triple.UnmodifiableTriple;
import breakingtherules.utilities.Utility;

public class HitsCSVDao implements HitsDao {

    private final Cache<UnmodifiableTriple<String, List<Rule>, Filter>, Integer> m_totalHitsCache;
    private final Cache<String, Set<UniqueHit>> m_cacheHits;

    public HitsCSVDao() {
	m_totalHitsCache = new SynchronizedHashCache<>();
	m_cacheHits = new SynchronizedHashCache<>();
    }

    @Override
    public ListDto<Hit> getHits(final String jobName, final List<Rule> rules, final Filter filter)
	    throws IOException, CSVParseException {
	final List<Hit> hits = CSVParser.fromCSV(CSVParser.DEFAULT_COLUMNS_TYPES, CSVDaoConfig.getHitsFile(jobName),
		rules, filter, -1);
	// TODO - take the opportunity and add hits to cache.
	final int size = hits.size();

	// Create new list of the rules to clone the list - so modifications
	// on the original list will not change the list saved in the cache
	m_totalHitsCache.add(
		new UnmodifiableTriple<>(jobName, Collections.unmodifiableList(Utility.newArrayList(rules)), filter),
		Integer.valueOf(size));
	return new ListDto<>(hits, 0, size, size);
    }

    @Override
    public Set<UniqueHit> getUnique(final String jobName, final List<Rule> rules, final Filter filter)
	    throws CSVParseException, IOException {
	final Set<UniqueHit> uniqueHits = getUniqueHitsInternal(CSVDaoConfig.getHitsFile(jobName));

	if (rules.isEmpty() && Filter.ANY_FILTER.equals(filter)) {
	    return uniqueHits;
	}

	final Set<UniqueHit> filteredHits = new HashSet<>();
	for (final UniqueHit hit : uniqueHits) {
	    if (UtilityDao.isMatch(hit, rules, filter)) {
		filteredHits.add(hit);
	    }
	}
	return filteredHits;
    }

    @Override
    public int getHitsNumber(final String jobName, final List<Rule> rules, final Filter filter)
	    throws IOException, CSVParseException {
	try {
	    return m_totalHitsCache
		    .getOrAdd(new UnmodifiableTriple<>(jobName,
			    Collections.unmodifiableList(Utility.newArrayList(rules)), filter), HITS_NUMBER_SUPPLIER)
		    .intValue();
	} catch (UncheckedIOException e) {
	    throw e.getCause();
	} catch (UncheckedCSVParseException e) {
	    throw e.getCause();
	}
    }

    private Set<UniqueHit> getUniqueHitsInternal(final String fileName) throws CSVParseException, IOException {
	try {
	    return m_cacheHits.getOrAdd(fileName, UNIQUE_HITS_SUPPLIER);
	} catch (UncheckedIOException e) {
	    throw e.getCause();
	} catch (UncheckedCSVParseException e) {
	    throw e.getCause();
	}
    }

    private final Function<Triple<String, List<Rule>, Filter>, Integer> HITS_NUMBER_SUPPLIER = (
	    final Triple<String, List<Rule>, Filter> triple) -> {
	try {
	    final String jobName = triple.getFirst();
	    final List<Rule> rules = triple.getSecond();
	    final Filter filter = triple.getThird();
	    final Set<UniqueHit> uniqueHits = getUniqueHitsInternal(CSVDaoConfig.getHitsFile(jobName));
	    int hitsNumber = 0;
	    for (final UniqueHit hit : uniqueHits) {
		if (UtilityDao.isMatch(hit, rules, filter)) {
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

    private static final Function<String, Set<UniqueHit>> UNIQUE_HITS_SUPPLIER = (final String fileName) -> {
	final Map<Hit, Integer> hitsCount = new HashMap<>();
	try {
	    CSVParser.fromCSV(CSVParser.DEFAULT_COLUMNS_TYPES, fileName, Collections.emptyList(), Filter.ANY_FILTER, 0,
		    -1, new AbstractSet<Hit>() {

		@Override
		public boolean add(Hit hit) {
		    Integer count = hitsCount.get(hit);
		    count = Integer.valueOf(count == null ? 1 : count.intValue() + 1);
		    hitsCount.put(hit, count);
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
	for (Iterator<Map.Entry<Hit, Integer>> it = hitsCount.entrySet().iterator(); it.hasNext();) {
	    Map.Entry<Hit, Integer> entry = it.next();
	    Hit hit = entry.getKey();
	    int amount = entry.getValue().intValue();
	    uniqueHits.add(new UniqueHit(hit, amount));
	    it.remove();
	}
	// Don't let anyone change the cache
	return Collections.unmodifiableSet(uniqueHits);
    };

}
