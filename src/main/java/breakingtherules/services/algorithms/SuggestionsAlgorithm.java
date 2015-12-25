package breakingtherules.services.algorithms;

import java.util.List;

import breakingtherules.dto.SuggestionsDto;
import breakingtherules.firewall.Hit;

/**
 * Algorithm interface to get suggestion to filters and rules
 */
public interface SuggestionsAlgorithm {

    /**
     * Get suggestion of a attribute type for filters and rules
     * 
     * @param hits
     *            list of hits the algorithm should operate on
     * @param attType
     *            requested suggestion type
     * @return suggestions DTO of attType
     */
    public SuggestionsDto getSuggestions(List<Hit> hits, String attType);

}
