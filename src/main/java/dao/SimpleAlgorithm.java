package dao;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import dao.Attribute.AttType;

/**
 * Simple algorithm the implements {@link Algorithm} methods
 */
public class SimpleAlgorithm implements Algorithm {

    public List<Suggestion> getSuggestions(List<Hit> hits, List<Rule> rules, Filter filter, AttType attType,
	    int startIndex, int endIndex) {
	// Map all hits to suggestion
	Vector<Suggestion> allSuggestionsVector = new Vector<Suggestion>();
	HashMap<Attribute, Suggestion> allSuggestionsMap = new HashMap<Attribute, Suggestion>();
	for (Hit hit : hits) {
	    Attribute att = hit.getAttribute(attType);
	    if (att == null)
		continue;

	    Suggestion suggestion = allSuggestionsMap.get(att);
	    if (suggestion == null) {
		suggestion = new Suggestion(att);
		allSuggestionsMap.put(att, suggestion);
		allSuggestionsVector.add(suggestion);
	    }

	    suggestion.join();
	}

	// Calculate scores
	for (Suggestion suggestion : allSuggestionsVector)
	    suggestion.setScore(suggestion.getSize());

	// Put suggestions in array for sorting
	allSuggestionsVector.sort(new Comparator<Suggestion>() {

	    public int compare(Suggestion o1, Suggestion o2) {
		return o1.compareTo(o2);
	    }

	});

	// If request is over suggestion cup
	if (allSuggestionsVector.size() < startIndex) {
	    return new ArrayList<Suggestion>();
	}

	// Extract wanted interval from general suggestion list
	ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
	for (int i = startIndex; i < allSuggestionsVector.size() && i < endIndex; i++) {
	    Suggestion suggestion = allSuggestionsVector.get(i);
	    suggestions.add(suggestion);
	}

	return suggestions;
    }

}
