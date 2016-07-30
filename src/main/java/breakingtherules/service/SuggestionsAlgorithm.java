package breakingtherules.service;

import java.io.IOException;
import java.util.List;

import breakingtherules.dao.ParseException;
import breakingtherules.firewall.Attribute.AttributeType;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Rule;

/**
 * Algorithm interface to get suggestion to filters and rules.
 * <p>
 * TODO javadoc
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 */
public interface SuggestionsAlgorithm {

    /**
     * The default permissiveness for an algorithm.
     */
    public static double DEFAULT_PERMISSIVENESS = 0.5;

    /**
     * The minimum allowed permissiveness for an algorithm.
     */
    public static double MIN_PERMISSIVENESS = 0;

    /**
     * The maximum allowed permissiveness for an algorithm.
     */
    public static double MAX_PERMISSIVENESS = 1;

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
     * @throws NullPointerException
     *             if the rules list or the filter is null.
     */
    public List<Suggestion> getSuggestions(String jobName, List<Rule> rules, Filter filter, int amount,
	    AttributeType attType) throws IOException, ParseException;

    /**
     * Set the permissiveness of the algorithm.
     * <p>
     * The algorithm may consider this value when choosing between more secure
     * rules versus more general ones.
     * <p>
     *
     * @param permissiveness
     *            the new permissiveness value. Should be in range [
     *            {@value #MIN_PERMISSIVENESS}, {@value #MAX_PERMISSIVENESS}].
     * @throws IllegalArgumentException
     *             if the permissiveness is not in range [
     *             {@value #MIN_PERMISSIVENESS}, {@value #MAX_PERMISSIVENESS}].
     */
    public void setPermissiveness(double permissiveness);

    /**
     * Get suggestions for more then one type at once.
     * <p>
     * This method allowed advance algorithms to perform faster then multiple
     * called to
     * {@link #getSuggestions(String, List, Filter, int, AttributeType)}.
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
     * @throws NullPointerException
     *             if the rules list, the filter or the attribute types array is
     *             null.
     */
    default List<Suggestion>[] getSuggestions(final String jobName, final List<Rule> rules, final Filter filter,
	    final int amount, final AttributeType[] attTypes) throws IOException, ParseException {
	@SuppressWarnings("unchecked")
	final List<Suggestion>[] suggestions = new List[attTypes.length];

	for (int i = 0; i < attTypes.length; i++)
	    suggestions[i] = getSuggestions(jobName, rules, filter, amount, attTypes[i]);
	return suggestions;
    }

}
