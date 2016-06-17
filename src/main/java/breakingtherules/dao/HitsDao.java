package breakingtherules.dao;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;
import breakingtherules.utilities.Utility;

/**
 * Component that supply hits from repository.
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * 
 */
public interface HitsDao {

    /**
     * Get hits from repository that match all rules and filter
     * 
     * @param jobName
     *            id of the hits' job
     * @param rules
     *            list of the current rules, act like additional filter
     * @param filter
     *            filter of the hits
     * @return all hits that match all rules and filter
     * @throws IOException
     *             if failed to read from memory
     * @throws ParseException
     *             if any parse errors occurs in the data.
     * @deprecated non practical when working on large data. Use
     *             {@link #getUnique(String, List, Filter)}.
     */
    @Deprecated
    public ListDto<Hit> getHits(String jobName, List<Rule> rules, Filter filter) throws IOException, ParseException;

    /**
     * Get all hits from repository that match all rules and filter
     * 
     * @param jobName
     *            Name of the hits' job
     * @param startIndex
     *            the start index of the hits list, including this index
     * @param endIndex
     *            the end index of the hits list, including this index
     * @param rules
     *            list of the current rules, act like additional filter
     * @param filter
     *            filter of the hits
     * @return all hits that match all rules and filter in range [startIndex,
     *         endIndex]
     * @throws IOException
     *             if failed to read from memory
     * @throws ParseException
     *             if any parse errors occurs in the data.
     */
    default ListDto<UniqueHit> getHits(String jobName, List<Rule> rules, Filter filter, int startIndex, int endIndex)
	    throws IOException, ParseException {
	final Set<UniqueHit> allHits = getUnique(jobName, rules, filter);
	final int size = allHits.size();
	final List<UniqueHit> hits = Utility.subList(allHits, startIndex, endIndex - startIndex);
	return new ListDto<>(hits, Math.min(startIndex, size), Math.min(endIndex, size), size);
    }

    /**
     * Get unique hits filtered by filter and rules.
     * 
     * @param jobName
     *            the name of the job.
     * @param rules
     *            list of current rules.
     * @param filter
     *            current filter/
     * @return set of unique hits.
     * @throws IOException
     *             if failed to read from memory.
     * @throws ParseException
     *             if any parse errors occurs in the data.
     */
    default Set<UniqueHit> getUnique(final String jobName, final List<Rule> rules, final Filter filter)
	    throws IOException, ParseException {
	final List<Hit> hits = getHits(jobName, rules, filter).getData();
	final Map<Hit, Integer> hitsCount = new HashMap<>();
	for (final Hit hit : hits) {
	    Integer count = hitsCount.get(hit);
	    count = Integer.valueOf(count == null ? 1 : count.intValue() + 1);
	    hitsCount.put(hit, count);
	}
	final Set<UniqueHit> uniqueHits = new HashSet<>();
	for (final Iterator<Map.Entry<Hit, Integer>> it = hitsCount.entrySet().iterator(); it.hasNext();) {
	    final Map.Entry<Hit, Integer> entry = it.next();
	    final Hit hit = entry.getKey();
	    final int amount = entry.getValue().intValue();
	    uniqueHits.add(new UniqueHit(hit, amount));
	    it.remove();
	}
	return uniqueHits;
    }

    /**
     * Get the number of hits in the job, that pass all the rules and are under
     * filter
     * 
     * @param jobName
     *            Name of the hits' job
     * @param rules
     *            list of the current rules, filter out
     * @param filter
     *            filter of the hits
     * @return The number of hits that are in the job, pass the rules but not
     *         the filter
     * @throws IOException
     *             if failed to read from memory.
     * @throws ParseException
     *             if any parse errors occurs in the data.
     */
    public int getHitsNumber(String jobName, List<Rule> rules, Filter filter) throws IOException, ParseException;

}
