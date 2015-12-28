package breakingtherules.services.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Component;

import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Hit;

/**
 * Simple algorithm the implements {@link Algorithm} methods
 */
@Component
public class SimpleAlgorithm implements SuggestionsAlgorithm {
    
    static int NUM_OF_SUGGESTIONS = 10;

    public List<Suggestion> getSuggestions(List<Hit> hits, String attType) {

	// The answer list
	List<Suggestion> allSuggestionsList = new ArrayList<Suggestion>();

	// Every possible single attribute becomes a suggestion.
	HashMap<Attribute, Suggestion> allSuggestionsMap = new HashMap<Attribute, Suggestion>();

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
	for (Suggestion suggestion : allSuggestionsList)
	    suggestion.setScore(suggestion.getSize() / (double)hits.size());

	// Put suggestions in array for sorting
	Collections.sort(allSuggestionsList);
	Collections.reverse(allSuggestionsList);
	
	if (allSuggestionsList.size() > NUM_OF_SUGGESTIONS)
	    return allSuggestionsList.subList(0, NUM_OF_SUGGESTIONS - 1);
	return allSuggestionsList;

	// Partial list code :

	// If request is over suggestion cup
	// if (allSuggestionsList.size() < startIndex) {
	// return new ArrayList<Suggestion>();
	// }

	// Extract wanted interval from general suggestion list
	// ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
	// for (int i = startIndex; i < allSuggestionsList.size() && i <
	// endIndex; i++) {
	// Suggestion suggestion = allSuggestionsList.get(i);
	// suggestions.add(suggestion);
	// }

	// return suggestions;
    }

}
