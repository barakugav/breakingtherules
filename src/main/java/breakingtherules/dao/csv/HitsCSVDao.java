package breakingtherules.dao.csv;

import java.io.IOException;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import breakingtherules.dao.HitsDao;
import breakingtherules.dao.UniqueHit;
import breakingtherules.dao.UtilityDao;
import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;
import breakingtherules.utilities.Triple;
import breakingtherules.utilities.Triple.UnmodifiableTriple;
import breakingtherules.utilities.Utility;

public class HitsCSVDao implements HitsDao {

    private final Map<UnmodifiableTriple<Integer, List<Rule>, Filter>, Integer> m_totalHitsCache;
    private final Map<String, Set<UniqueHit>> cacheHits;

    public HitsCSVDao() {
	m_totalHitsCache = new HashMap<>();
	cacheHits = new HashMap<>();
    }

    @Override
    public ListDto<Hit> getHits(String jobName, List<Rule> rules, Filter filter) throws IOException {
	try {
	    List<Hit> hits = CSVParser.fromCSV(CSVParser.DEFAULT_COLUMNS_TYPES, CSVDaoConfig.getHitsFile(jobName), rules,
		    filter, -1);
	    int size = hits.size();

	    // Create new list of the rules to clone the list - so modifications
	    // on the original list will not change the list saved in the cache
	    m_totalHitsCache.put(new UnmodifiableTriple<>(Integer.valueOf(jobName),
		    Collections.unmodifiableList(new ArrayList<>(rules)), filter), Integer.valueOf(size));
	    return new ListDto<>(hits, 0, size, size);
	} catch (CSVParseException e) {
	    throw new IOException(e);
	}
    }

    @Override
    public ListDto<Hit> getHits(String jobName, List<Rule> rules, Filter filter, int startIndex, int endIndex)
	    throws IOException {
	try {
	    if (endIndex < 0) {
		throw new IllegalArgumentException("endIndex is negative: " + endIndex);
	    }
	    if (startIndex < 0) {
		throw new IllegalArgumentException("startIndex is negative: " + startIndex);
	    }
	    if (startIndex > endIndex) {
		throw new IllegalArgumentException("startIndex > endIndex");
	    }

	    int numberOfHits = getHitsNumber(jobName, rules, filter);
	    final List<Hit> hits = CSVParser.fromCSV(CSVParser.DEFAULT_COLUMNS_TYPES, CSVDaoConfig.getHitsFile(jobName),
		    rules, filter, endIndex);
	    final int size = hits.size();
	    final List<Hit> subList = Utility.subList(hits, startIndex, endIndex - startIndex);
	    return new ListDto<>(subList, Math.min(startIndex, size), Math.min(endIndex, size), numberOfHits);

	} catch (CSVParseException e) {
	    throw new IOException(e);
	}
    }

    @Override
    public Set<UniqueHit> getUnique(String jobName, List<Rule> rules, Filter filter) throws CSVParseException, IOException {
	Set<UniqueHit> uniqueHits = getHitsInternal(CSVDaoConfig.getHitsFile(jobName));

	if (rules.isEmpty() && Filter.ANY_FILTER.equals(filter))
	    return uniqueHits;

	Set<UniqueHit> filteredHits = new HashSet<>();
	for (UniqueHit hit : uniqueHits)
	    if (UtilityDao.isMatch(hit, rules, filter))
		filteredHits.add(hit);
	return filteredHits;
    }

    @Override
    public int getHitsNumber(String jobName, List<Rule> rules, Filter filter) throws IOException {
	final Integer cachedSize = m_totalHitsCache.get(new Triple<>(Integer.valueOf(jobName), rules, filter));
	if (cachedSize != null) {
	    return cachedSize.intValue();
	} else {
	    try {
		Set<UniqueHit> uniqueHits = getHitsInternal(CSVDaoConfig.getHitsFile(jobName));
		int hitsNumber = 0;
		for (UniqueHit hit : uniqueHits) {
		    if (UtilityDao.isMatch(hit, rules, filter)) {
			hitsNumber += hit.getAmount();
		    }
		}
		m_totalHitsCache.put(
			new UnmodifiableTriple<>(Integer.valueOf(jobName),
				Collections.unmodifiableList(new ArrayList<>(rules)), filter),
			Integer.valueOf(hitsNumber));
		return hitsNumber;
	    } catch (CSVParseException e) {
		throw new IOException(e);
	    }
	}

    }

    private Set<UniqueHit> getHitsInternal(String fileName) throws CSVParseException, IOException {
	Set<UniqueHit> uniqueHits = cacheHits.get(fileName);
	if (uniqueHits == null) {
	    Map<Hit, Integer> hitsCount = new HashMap<>();
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
			    return null;
			}

			@Override
			public int size() {
			    return 0;
			}
		    });
	    uniqueHits = new HashSet<>();
	    for (Iterator<Map.Entry<Hit, Integer>> it = hitsCount.entrySet().iterator(); it.hasNext();) {
		Map.Entry<Hit, Integer> entry = it.next();
		Hit hit = entry.getKey();
		int amount = entry.getValue().intValue();
		uniqueHits.add(new UniqueHit(hit.getAttributes(), amount));
		it.remove();
	    }
	    // Don't let anyone change the cache
	    uniqueHits = Collections.unmodifiableSet(uniqueHits);
	    cacheHits.put(fileName, uniqueHits);
	}
	return uniqueHits;
    }

}
