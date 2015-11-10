package breakingtherules.algorithms;

import java.util.List;

import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;
import breakingtherules.firewall.Attribute.AttType;

/**
 * Algorithm interface to get suggestion to filters and rules
 */
public interface Algorithm {

    /**
     * Get suggestion of a attribute type for filters and rules
     * 
     * @param hits
     *            list of current hits
     * @param rules
     *            list of current rules
     * @param filter
     *            current filter
     * @param attType
     *            type of the suggestion wanted
     * @param startIndex
     *            start index of the suggestion list
     * @param endIndex
     *            end index of the suggestion list
     * @return list of suggestion for rules
     */
    public List<Suggestion> getSuggestions(List<Hit> hits, List<Rule> rules, Filter filter, AttType attType,
	    int startIndex, int endIndex);

}
