package breakingtherules.services.algorithm;

import java.util.List;

import breakingtherules.dao.HitsDao;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Rule;

/**
 * Algorithm interface to get suggestion to filters and rules
 */
public interface SuggestionsAlgorithm {

    /**
     * Get suggestion for an attribute type
     * 
     * @param dao
     *            hits data access object
     * @param jobId
     *            id of the job
     * @param rules
     *            current rules
     * @param filter
     *            current filter
     * @param attType
     *            requested suggestion type
     * @param amount
     *            number of requested suggestion
     * @return suggestions list of suggestion of the desire attribute relevant
     *         to the hits provided by the DAO.
     * @throws Exception
     *             if any errors occurs
     */
    public List<Suggestion> getSuggestions(HitsDao dao, int jobId, List<Rule> rules, Filter filter, String attType,
	    int amount) throws Exception;

}
