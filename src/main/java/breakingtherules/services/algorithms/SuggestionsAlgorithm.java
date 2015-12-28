package breakingtherules.services.algorithms;

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
     * 		All the hits under current filter of the job
     * @return list of suggestion for rules
     */
    public List<Suggestion> getSuggestions(List<Hit> hits, String attType);

}
