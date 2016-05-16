package breakingtherules.services.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Hit;
import breakingtherules.utilities.Utility;

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
    public List<Suggestion> getSuggestions(final Iterable<Hit> hits, final String attType) {
	// Every possible single attribute becomes a suggestion.
	Map<Attribute, Suggestion> allSuggestionsMap = new HashMap<>();

	int numberOfHits = 0;
	final int attId = Attribute.typeStrToTypeId(attType);
	if (attId == Attribute.UNKOWN_ATTRIBUTE_ID) {
	    throw new IllegalArgumentException("Unknown attribute: " + attType);
	}

	// Create a suggestion for every attribute, count the number of hits
	// that apply to it
	for (final Hit hit : hits) {
	    numberOfHits++;
	    final Attribute att = hit.getAttribute(attId);
	    if (att == null) {
		// No attribute
		continue;
	    }

	    Suggestion suggestion = allSuggestionsMap.get(att);
	    if (suggestion == null) {
		suggestion = new Suggestion(att);
		allSuggestionsMap.put(att, suggestion);
	    }
	    suggestion.join();
	}

	// Calculate scores
	List<Suggestion> allSuggestionsList = new ArrayList<>(allSuggestionsMap.values());
	for (final Suggestion suggestion : allSuggestionsList) {
	    suggestion.setScore(suggestion.getSize() / (double) numberOfHits);
	}
	allSuggestionsMap = null; // Free memory

	// Sort by score
	allSuggestionsList.sort(null); // Sort with null comparator for regular
				       // compareTo sort.
	Collections.reverse(allSuggestionsList);

	return Utility.subList(allSuggestionsList, 0, NUMBER_OF_SUGGESTIONS);
    }

}
