package breakingtherules.services.algorithm;

import java.util.List;

import breakingtherules.firewall.Hit;

/**
 * Algorithm interface to get suggestion to filters and rules
 */
public interface SuggestionsAlgorithm {

    /**
     * Get suggestion of a attribute type for filters and rules
     * 
     * @param hits
     *            iterable object of hits
     * @param attType
     *            requested suggestion type
     * @return suggestions list
     */
    public List<Suggestion> getSuggestions(Iterable<Hit> hits, String attType);

}
