package breakingtherules.dao;

import java.io.IOException;
import java.util.List;

import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;

/**
 * Component that supply hits from repository
 */
public interface HitsDao {

    /**
     * Get hits from repository that match all rules and filter
     * 
     * @param jobId
     *            id of the hits' job
     * @param rules
     *            list of the current rules, act like additional filter
     * @param filter
     *            filter of the hits
     * @return all hits that match all rules and filter
     * @throws IOException
     *             if failed to read from memory
     */
    public ListDto<Hit> getHits(int jobId, List<Rule> rules, Filter filter) throws IOException;

    /**
     * Get all hits from repository that match all rules and filter
     * 
     * @param jobId
     *            id of the hits' job
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
     */
    public ListDto<Hit> getHits(int jobId, List<Rule> rules, Filter filter, int startIndex, int endIndex)
	    throws IOException;

    /**
     * Get the number of hits in the job, that pass all the rules and are under
     * filter
     * 
     * @param jobId
     *            id of the hits' job
     * @param rules
     *            list of the current rules, filter out
     * @param filter
     *            filter of the hits
     * @return The number of hits that are in the job, pass the rules but not
     *         the filter
     * @throws IOException 
     */
    public int getHitsNumber(int jobId, List<Rule> rules, Filter filter) throws IOException;

}
