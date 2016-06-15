package breakingtherules.services.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import breakingtherules.dao.HitsDao;
import breakingtherules.dao.UniqueHit;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Rule;
import breakingtherules.utilities.Utility;

/**
 * Simple algorithm the implements {@link SuggestionsAlgorithm}
 * 
 * Simply count the number of hits for each suggestion
 */
public class SimpleAlgorithm implements SuggestionsAlgorithm {

    @Override
    public List<Suggestion> getSuggestions(final HitsDao dao, final String jobName, final List<Rule> rules,
	    final Filter filter, final int amount, final String attType) throws Exception {
	final int attTypeId = Attribute.typeStrToTypeId(attType);
	if (attTypeId == Attribute.UNKOWN_ATTRIBUTE_ID) {
	    throw new IllegalArgumentException("Unknown attribute: " + attType);
	}
	return getSuggestions(dao.getUnique(jobName, rules, filter), amount, attTypeId);
    }

    List<Suggestion> getSuggestions(final Set<UniqueHit> hits, final int amount, final int attTypeId) {

	SimpleAlgorithmRunner runner = new SimpleAlgorithmRunner(hits, amount, attTypeId);
	runner.run();
	return runner.result;
    }

    static class SimpleAlgorithmRunner implements SuggestionsAlgorithmRunner {

	private final Set<UniqueHit> hits;
	private final int amount;
	private final int attTypeId;
	private List<Suggestion> result;

	SimpleAlgorithmRunner(final Set<UniqueHit> hits, final int amount, final int attTypeId) {
	    this.hits = hits;
	    this.amount = amount;
	    this.attTypeId = attTypeId;
	}

	@Override
	public void run() {
	    // Every possible single attribute becomes a suggestion.
	    Map<Attribute, Suggestion> allSuggestionsMap = new HashMap<>();

	    // Create a suggestion for every attribute, count the number of hits
	    // that apply to it
	    int numberOfHits = 0;
	    for (final UniqueHit hit : hits) {
		numberOfHits++;
		final Attribute att = hit.getAttribute(attTypeId);
		if (att == null) {
		    // No attribute
		    continue;
		}

		Suggestion suggestion = allSuggestionsMap.get(att);
		if (suggestion == null) {
		    suggestion = new Suggestion(att);
		    allSuggestionsMap.put(att, suggestion);
		}
		suggestion.join(hit.getAmount());
	    }

	    // Calculate scores
	    List<Suggestion> allSuggestionsList = new ArrayList<>(allSuggestionsMap.values());
	    for (final Suggestion suggestion : allSuggestionsList) {
		suggestion.setScore(suggestion.getSize() / (double) numberOfHits);
	    }
	    allSuggestionsMap = null; // Free memory

	    // Sort by score
	    allSuggestionsList.sort(null); // Sort with null comparator for
					   // regular compareTo sort.
	    Collections.reverse(allSuggestionsList);

	    result = Utility.subList(allSuggestionsList, 0, amount);
	}

	@Override
	public List<Suggestion> getResults() {
	    return result;
	}

    }

}
