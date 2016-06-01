package breakingtherules.dao.csv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import breakingtherules.dao.HitsDao;
import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;
import breakingtherules.utilities.Triple;
import breakingtherules.utilities.Utility;

public class HitsCSVDao implements HitsDao {

    private Map<Triple<Integer, List<Rule>, Filter>, Integer> m_totalHitsCache;

    public HitsCSVDao() {
	m_totalHitsCache = new HashMap<>();
    }

    @Override
    public ListDto<Hit> getHits(int jobId, List<Rule> rules, Filter filter) throws IOException {
	try {
	    List<Hit> hits = CSVParser.fromCSV(CSVParser.DEFAULT_COLUMNS_TYPES, CSVDaoConfig.getHitsFile(jobId), rules,
		    filter, -1);
	    int size = hits.size();

	    // Create new list of the rules to clone the list - so modifications
	    // on the original list will not change the list saved in the cache
	    m_totalHitsCache.put(new Triple<>(Integer.valueOf(jobId), new ArrayList<>(rules), filter),
		    Integer.valueOf(size));
	    return new ListDto<>(hits, 0, size, size);
	} catch (CSVParseException e) {
	    throw new IOException(e);
	}
    }

    @Override
    public ListDto<Hit> getHits(int jobId, List<Rule> rules, Filter filter, int startIndex, int endIndex)
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

	    Integer cachedSize = m_totalHitsCache.get(new Triple<>(Integer.valueOf(jobId), rules, filter));
	    if (cachedSize != null) {
		final List<Hit> hits = CSVParser.fromCSV(CSVParser.DEFAULT_COLUMNS_TYPES,
			CSVDaoConfig.getHitsFile(jobId), rules, filter, endIndex);
		final int size = hits.size();
		final List<Hit> subList = Utility.subList(hits, startIndex, endIndex - startIndex);
		return new ListDto<>(subList, Math.min(startIndex, size), Math.min(endIndex, size),
			cachedSize.intValue());
	    } else {
		final ListDto<Hit> hits = getHits(jobId, rules, filter);
		final int size = hits.getSize();
		final List<Hit> subList = Utility.subList(hits.getData(), startIndex, endIndex - startIndex);
		return new ListDto<>(subList, Math.min(startIndex, size), Math.min(endIndex, size), size);
	    }

	} catch (CSVParseException e) {
	    throw new IOException(e);
	}
    }

    @Override
    public Set<Hit> getUnique(int jobId, List<Rule> rules, Filter filter) throws CSVParseException, IOException {
	Set<Hit> hits = new HashSet<>();
	CSVParser.fromCSV(CSVParser.DEFAULT_COLUMNS_TYPES, CSVDaoConfig.getHitsFile(jobId), rules, filter, 0, -1, hits);
	return hits;
    }

    @Override
    public Set<Hit> getUnique(int jobId, List<Rule> rules, Filter filter, int startIndex, int endIndex)
	    throws IOException, CSVParseException {
	if (endIndex < 0) {
	    throw new IllegalArgumentException("endIndex is negative: " + endIndex);
	}
	if (startIndex < 0) {
	    throw new IllegalArgumentException("startIndex is negative: " + startIndex);
	}
	if (startIndex > endIndex) {
	    throw new IllegalArgumentException("startIndex > endIndex");
	}

	Set<Hit> hits = new HashSet<>();
	CSVParser.fromCSV(CSVParser.DEFAULT_COLUMNS_TYPES, CSVDaoConfig.getHitsFile(jobId), rules, filter, startIndex,
		endIndex, hits);
	return hits;
    }

    @Override
    public int getHitsNumber(int jobId, List<Rule> rules, Filter filter) throws IOException {
	final Integer cachedSize = m_totalHitsCache.get(new Triple<>(Integer.valueOf(jobId), rules, filter));
	if (cachedSize != null) {
	    return cachedSize.intValue();
	} else {
	    return getHits(jobId, rules, filter).getSize();
	}
    }

}
