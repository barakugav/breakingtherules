package breakingtherules.services.algorithm;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import breakingtherules.dao.HitsDao;
import breakingtherules.dao.ParseException;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Attribute.AttributeType;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
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
 *
 */
public class SimpleAlgorithm extends AbstractSuggestionsAlgorithm {

    /**
     * Construct new Information algorithm
     *
     * @param hitsDao
     *            The DAO that the algorithm will use in order to read the job's
     *            hits
     */
    public SimpleAlgorithm(final HitsDao hitsDao) {
	super(hitsDao);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Suggestion> getSuggestions(final String jobName, final List<Rule> rules, final Filter filter,
	    final int amount, final AttributeType attType) throws IOException, ParseException {
	return getSuggestions(m_hitsDao.getHits(jobName, rules, filter), amount, Objects.requireNonNull(attType));
    }

    /**
     * Get suggestions for hits (from iterable).
     *
     * @param hits
     *            the hits iterable object.
     * @param amount
     *            the number of suggestions is requested.
     * @param attTypeId
     *            the type of the suggestions.
     * @return suggestions for the hits for the attribute type.
     */
    List<Suggestion> getSuggestions(final Iterable<Hit> hits, final int amount, final AttributeType attTypeId) {
	final SimpleAlgorithmRunner runner = new SimpleAlgorithmRunner(hits, amount, attTypeId);
	runner.run();
	return runner.m_result;
    }

    /**
     * The runnable used by the {@link SimpleAlgorithm}.
     * <p>
     *
     * @author Barak Ugav
     * @author Yishai Gronich
     *
     */
    static class SimpleAlgorithmRunner implements Runnable {

	/**
	 * The input hits.
	 */
	private final Iterable<Hit> m_hits;

	/**
	 * The number of requested suggestions.
	 */
	private final int m_amount;

	/**
	 * The type of requested suggestions.
	 */
	private final AttributeType m_attTypeId;

	/**
	 * The result buffer. This value is relevant only after the runner was
	 * run.
	 */
	private List<Suggestion> m_result;

	/**
	 * Construct new SimpleAlgorithmRunner.
	 *
	 * @param hits
	 *            the input hits.
	 * @param amount
	 *            the number of requested suggestions.
	 * @param attTypeId
	 *            the type of requested suggestions.
	 */
	SimpleAlgorithmRunner(final Iterable<Hit> hits, final int amount, final AttributeType attTypeId) {
	    m_hits = hits;
	    m_amount = amount;
	    m_attTypeId = attTypeId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
	    // Every possible single attribute becomes a suggestion.
	    Map<Attribute, Suggestion> allSuggestionsMap = new HashMap<>();

	    // Create a suggestion for every attribute, count the number of hits
	    // that apply to it
	    int numberOfHits = 0;
	    for (final Hit hit : m_hits) {
		numberOfHits++;
		final Attribute att = hit.getAttribute(m_attTypeId);
		if (att == null)
		    // No attribute
		    continue;

		Suggestion suggestion = allSuggestionsMap.get(att);
		if (suggestion == null) {
		    suggestion = new Suggestion(att);
		    allSuggestionsMap.put(att, suggestion);
		}
		suggestion.join();
	    }

	    // Calculate scores
	    final List<Suggestion> allSuggestionsList = Utility.newArrayList(allSuggestionsMap.values());
	    for (final Suggestion suggestion : allSuggestionsList)
		suggestion.setScore(suggestion.getSize() / (double) numberOfHits);
	    allSuggestionsMap = null; // Free memory

	    // Sort by score
	    allSuggestionsList.sort(Suggestion.SCORE_COMPARATOR_GREATER_TO_SMALLER);

	    m_result = Utility.subList(allSuggestionsList, 0, m_amount);
	}

    }

}
