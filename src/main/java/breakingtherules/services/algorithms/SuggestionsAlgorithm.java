package breakingtherules.services.algorithms;

import java.util.List;

import breakingtherules.session.Job;

/**
 * Algorithm interface to get suggestion to filters and rules
 */
public interface SuggestionsAlgorithm {

    /**
     * Get suggestion of a attribute type for filters and rules
     * 
     * @param job
     * 		The job that is needs suggesting. Used to extract the relevant hits.
     * @return list of suggestion for rules
     */
    public List<Suggestion> getSuggestions(Job job, String attType);

}
