package breakingtherules.services.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Hit;

/**
 * Simple algorithm the implements {@link SuggestionsAlgorithm}
 * 
 * Simply count the number of hits for each suggestion
 */
public class SimpleAlgorithm implements SuggestionsAlgorithm {

    /**
     * Number of suggestions that returned in each suggestions request
     */
    private static final int NUMBER_OF_SUGGESTIONS = 10;

    /*
     * (non-Javadoc)
     * 
     * @see
     * breakingtherules.services.algorithm.SuggestionsAlgorithm#getSuggestions(
     * java.util.List, java.lang.String)
     */
    @Override
    public List<Suggestion> getSuggestions(List<Hit> hits, String attType) {
	// The answer list
	List<Suggestion> allSuggestionsList = new ArrayList<>();

	// Every possible single attribute becomes a suggestion.
	HashMap<Attribute, Suggestion> allSuggestionsMap = new HashMap<>();

	// Create a suggestion for every attribute, count the number of hits
	// that apply to it
	for (Hit hit : hits) {
	    Attribute att = hit.getAttribute(attType);
	    if (att == null)
		continue;

	    Suggestion suggestion = allSuggestionsMap.get(att);
	    if (suggestion == null) {
		suggestion = new Suggestion(att);
		allSuggestionsMap.put(att, suggestion);
		allSuggestionsList.add(suggestion);
	    }

	    suggestion.join();
	}

	// Calculate scores
	for (Suggestion suggestion : allSuggestionsList) {
	    suggestion.setScore(suggestion.getSize() / (double) hits.size());
	}

	// Sort by score
	Collections.sort(allSuggestionsList);
	Collections.reverse(allSuggestionsList);

	int endIndex = Math.min(NUMBER_OF_SUGGESTIONS, allSuggestionsList.size());
	return allSuggestionsList.subList(0, endIndex);
    }

}
