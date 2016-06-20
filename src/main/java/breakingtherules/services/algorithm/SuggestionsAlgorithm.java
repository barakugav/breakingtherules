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
     * @param jobName
     *            name of the job
     * @param rules
     *            current rules
     * @param filter
     *            current filter
     * @param amount
     *            number of requested suggestion
     * @param attType
     *            requested suggestion type
     * @return suggestions list of suggestion of the desire attribute relevant
     *         to the hits provided by the DAO.
     * @throws Exception
     *             if any errors occurs
     */
    public List<Suggestion> getSuggestions(HitsDao dao, String jobName, List<Rule> rules, Filter filter, int amount,
	    String attType) throws Exception;

    /**
     * TODO
     * 
     * @param dao
     * @param jobName
     * @param rules
     * @param filter
     * @param amount
     * @param attTypes
     * @return
     * @throws Exception
     */
    default List<Suggestion>[] getSuggestions(HitsDao dao, String jobName, List<Rule> rules, Filter filter, int amount,
	    String[] attTypes) throws Exception {
	@SuppressWarnings("unchecked")
	List<Suggestion>[] suggestions = new List[attTypes.length];
	for (int i = 0; i < attTypes.length; i++) {
	    suggestions[i] = getSuggestions(dao, jobName, rules, filter, amount, attTypes[i]);
	}
	return suggestions;
    }

}
