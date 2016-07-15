package breakingtherules.dao;

import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;

/**
 * The DaoUtilities class is a set of static utils methods for DAO objects.
 * <p>
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @see HitsDao
 */
public class DaoUtilities {

    /**
     * Suppresses default constructor, ensuring non-instantiability.
     */
    private DaoUtilities() {
    }

    /**
     * Check if a hit is match to a list of rules and a filter
     *
     * @param rules
     *            rules to check on the hit
     * @param filter
     *            filter to check on the hit
     * @param hit
     *            the hit that being checked
     * @return true if hit match all rules and filter, else - false
     */
    public static boolean isMatch(final Hit hit, final Iterable<Rule> rules, final Filter filter) {
	if (!filter.isMatch(hit))
	    return false;
	for (final Rule rule : rules)
	    if (rule.isMatch(hit))
		return false;
	return true;
    }

}
