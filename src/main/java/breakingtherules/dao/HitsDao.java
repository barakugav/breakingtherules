package breakingtherules.dao;

import java.io.IOException;
import java.util.List;

import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;
import breakingtherules.util.Utility;

/**
 * Component that supply hits from repository.
 * <p>
 * The supplied hits are always unique hits.
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @see Hit
 */
public interface HitsDao {

    /**
     * Get unique hits filtered by filter and rules.
     *
     * @param jobName
     *            the name of the job.
     * @param rules
     *            current rules, act like additional filters.
     * @param filter
     *            current filter.
     * @return iterable object of all unique hits.
     * @throws IOException
     *             if failed to read from memory.
     * @throws ParseException
     *             if any parse errors occurs in the data.
     * @throws NullPointerException
     *             if the rules or the filter is null.
     */
    public Iterable<Hit> getHits(final String jobName, final Iterable<Rule> rules, final Filter filter)
	    throws IOException, ParseException;

    /**
     * Get the number of (unique) hits in the job, that pass all the rules and
     * are under filter
     *
     * @param jobName
     *            Name of the hits' job
     * @param rules
     *            current rules, act like additional filters.
     * @param filter
     *            filter of the hits
     * @return The number of hits that are in the job, pass the rules but not
     *         the filter
     * @throws IOException
     *             if failed to read from memory.
     * @throws ParseException
     *             if any parse errors occurs in the data.
     * @throws NullPointerException
     *             if the rules or the filter is null.
     */
    public int getHitsNumber(String jobName, Iterable<Rule> rules, Filter filter) throws IOException, ParseException;

    /**
     * Initiate a repository for this job, with the given hits
     *
     * @param jobName
     *            The name for the new job
     * @param hits
     *            The hits that the job should be initiated with
     * @throws IllegalArgumentException
     *             If this job (name) already exists
     * @throws IOException
     *             If there was an error writing to IO
     * @throws NullPointerException
     *             if hits iterable is null.
     */
    public void initJob(String jobName, Iterable<Hit> hits) throws IOException;

    /**
     * Get list of (unique) hits from repository that match all rules and
     * filter.
     *
     * @param jobName
     *            id of the hits' job
     * @param rules
     *            current rules, act like additional filters.
     * @param filter
     *            filter of the hits
     * @return all (unique) hits that match all rules and filter
     * @throws IOException
     *             if failed to read from memory
     * @throws ParseException
     *             if any parse errors occurs in the data.
     */
    default ListDto<Hit> getHitsList(final String jobName, final Iterable<Rule> rules, final Filter filter)
	    throws IOException, ParseException {
	final List<Hit> hits = Utility.newArrayList(getHits(jobName, rules, filter));
	final int size = hits.size();
	return new ListDto<>(hits, 0, size, size);
    }

    /**
     * Get list of all (unique) hits from repository that match all rules and
     * filter
     *
     * @param jobName
     *            Name of the hits' job
     * @param startIndex
     *            the start index of the hits list, including this index
     * @param endIndex
     *            the end index of the hits list, including this index
     * @param rules
     *            current rules, act like additional filter.
     * @param filter
     *            filter of the hits
     * @return all (unique) hits that match all rules and filter in range
     *         [startIndex, endIndex]
     * @throws IOException
     *             if failed to read from memory
     * @throws ParseException
     *             if any parse errors occurs in the data.
     */
    default ListDto<Hit> getHitsList(final String jobName, final Iterable<Rule> rules, final Filter filter,
	    final int startIndex, final int endIndex) throws IOException, ParseException {
	final Iterable<Hit> allHits = getHits(jobName, rules, filter);
	final int totalSize = getHitsNumber(jobName, rules, filter);
	final List<Hit> hits = Utility.subList(allHits, startIndex, endIndex - startIndex);
	return new ListDto<>(hits, Math.min(startIndex, totalSize - 1), Math.min(endIndex, totalSize), totalSize);
    }

}
