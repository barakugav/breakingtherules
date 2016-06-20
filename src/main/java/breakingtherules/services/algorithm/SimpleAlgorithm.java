package breakingtherules.services.algorithm;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import breakingtherules.dao.HitsDao;
import breakingtherules.dao.UniqueHit;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Rule;
import breakingtherules.utilities.Utility;

/**
 * Simple algorithm that implements {@link SuggestionsAlgorithm}.
 * <p>
 * Simply count the number of hits for each attribute, and suggests the ones the
 * ones that occurs more than others.
 * <p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 */
public class SimpleAlgorithm implements SuggestionsAlgorithm {

    /*
     * (non-Javadoc)
     * 
     * @see
     * breakingtherules.services.algorithm.SuggestionsAlgorithm#getSuggestions(
     * breakingtherules.dao.HitsDao, java.lang.String, java.util.List,
     * breakingtherules.firewall.Filter, int, java.lang.String)
     */
    @Override
    public List<Suggestion> getSuggestions(final HitsDao dao, final String jobName, final List<Rule> rules,
	    final Filter filter, final int amount, final String attType) throws Exception {
	final int attTypeId = Attribute.typeStrToTypeId(attType);
	if (attTypeId == Attribute.UNKOWN_ATTRIBUTE_ID) {
	    throw new IllegalArgumentException("Unknown attribute: " + attType);
	}
	return getSuggestions(dao.getUniqueHits(jobName, rules, filter), amount, attTypeId);
    }

    List<Suggestion> getSuggestions(final Iterable<UniqueHit> hits, final int amount, final int attTypeId) {
	SimpleAlgorithmRunner runner = new SimpleAlgorithmRunner(hits, amount, attTypeId);
	runner.run();
	return runner.result;
    }

    static class SimpleAlgorithmRunner implements Runnable {

	private final Iterable<UniqueHit> hits;
	private final int amount;
	private final int attTypeId;
	private List<Suggestion> result;

	SimpleAlgorithmRunner(final Iterable<UniqueHit> hits, final int amount, final int attTypeId) {
	    this.hits = hits;
	    this.amount = amount;
	    this.attTypeId = attTypeId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
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
	    final List<Suggestion> allSuggestionsList = Utility.newArrayList(allSuggestionsMap.values());
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

    }

}
