package breakingtherules.services.algorithm;

import java.io.IOException;
import java.util.List;

import breakingtherules.dao.HitsDao;
import breakingtherules.dao.ParseException;
import breakingtherules.firewall.Attribute.AttributeType;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Rule;

/**
 * Algorithm interface to get suggestion to filters and rules
 */
public abstract class SuggestionsAlgorithm {

    /**
     * The DAO that the algorithm will use in order to read the job's hits
     */
    final HitsDao m_hitsDao;

    /**
     * Initiate the SuggestionsAlgorithm with a DAO it will use
     * 
     * @param hitsDao
     *            The DAO that the algorithm will use in order to read the job's
     *            hits
     */
    public SuggestionsAlgorithm(HitsDao hitsDao) {
	m_hitsDao = hitsDao;
    }

    /**
     * Get suggestion for an attribute type
     * 
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
     * @throws IOException
     *             if any I/O errors occurs in DAO.
     * @throws ParseException
     *             if any parse errors occurs in DAO.
     */
    public abstract List<Suggestion> getSuggestions(String jobName, List<Rule> rules, Filter filter, int amount,
	    AttributeType attType) throws IOException, ParseException;

    /**
     * Get suggestions for more then one type at once.
     * <p>
     * This method allowed advance algorithms to perform faster then multiple
     * called to
     * {@link #getSuggestions(String, List, Filter, int, AttributeType)}
     * .
     * 
     * @param jobName
     *            name of the job
     * @param rules
     *            current rules
     * @param filter
     *            current filter
     * @param amount
     *            number of requested suggestion
     * @param attTypes
     *            all requested suggestions type.
     * @return array of suggestions lists, each suggestions list for requested
     *         suggestions type, in the same order as the input suggestions type
     *         array is.
     * @throws IOException
     *             if any I/O errors occurs in DAO.
     * @throws ParseException
     *             if any parse errors occurs in DAO.
     */
    public List<Suggestion>[] getSuggestions(final String jobName, final List<Rule> rules, final Filter filter,
	    final int amount, final AttributeType[] attTypes) throws IOException, ParseException {
	@SuppressWarnings("unchecked")
	final List<Suggestion>[] suggestions = new List[attTypes.length];

	for (int i = 0; i < attTypes.length; i++) {
	    suggestions[i] = getSuggestions(jobName, rules, filter, amount, attTypes[i]);
	}
	return suggestions;
    }

}
