package breakingtherules.dao;

import java.util.List;

import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;

public class UtilityDao {

    /**
     * Check if a hit is match to a list of rules and a filter
     * 
     * @param rules
     *            list of rules to check on the hit
     * @param filter
     *            filter to check on the hit
     * @param hit
     *            the hit that being checked
     * @return true if hit match all rules and filter, else - false
     */
    public static boolean isMatch(final Hit hit, final List<Rule> rules, final Filter filter) {
	if (!filter.isMatch(hit)) {
	    return false;
	}
	for (final Rule rule : rules) {
	    if (rule.isMatch(hit)) {
		return false;
	    }
	}
	return true;
    }

}
