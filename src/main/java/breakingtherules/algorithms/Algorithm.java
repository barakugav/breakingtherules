package breakingtherules.algorithms;

import java.util.List;

import breakingtherules.firewall.Attribute.AttType;
import breakingtherules.session.Job;

/**
 * Algorithm interface to get suggestion to filters and rules
 */
public interface Algorithm {

    /**
     * Get suggestion of a attribute type for filters and rules
     * 
     * @param job
     * 		The job that is needs suggesting. Used to extract the relevant hits.
     * @return list of suggestion for rules
     */
    public List<Suggestion> getSuggestions(Job job, AttType attType);

}
