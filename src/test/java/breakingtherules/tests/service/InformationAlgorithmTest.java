package breakingtherules.tests.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import breakingtherules.dao.DaoUtils;
import breakingtherules.dao.HitsDao;
import breakingtherules.dao.ParseException;
import breakingtherules.firewall.Attribute.AttributeType;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.IPAttribute;
import breakingtherules.firewall.Rule;
import breakingtherules.service.InformationAlgorithm;
import breakingtherules.service.Suggestion;
import breakingtherules.tests.TestBase;
import breakingtherules.tests.firewall.FirewallTestsUtility;

@SuppressWarnings("javadoc")
public class InformationAlgorithmTest extends TestBase {

    private static final Comparator<Suggestion> SUGGESTIONS_IP_ATTRIBUTE_COMP = (a,
	    b) -> ((IPAttribute) a.getAttribute()).compareTo((IPAttribute) b.getAttribute());

    @Test
    public void getSeggestionsTestRuleWeight1() throws IOException, ParseException {
	final String jobName = "testJobName";

	// Generate hits
	final Destination des0 = Destination.valueOf("0.0.0.0");
	final Destination des1 = Destination.valueOf("0.0.0.1");
	final Destination des2 = Destination.valueOf("0.0.0.2");
	final Destination des3 = Destination.valueOf("0.0.0.3");
	final List<Hit> hits = new ArrayList<>();
	hits.addAll(generateHitsWithSameDestination(des0, 100));
	hits.addAll(generateHitsWithSameDestination(des1, 50));
	hits.addAll(generateHitsWithSameDestination(des2, 17));
	hits.addAll(generateHitsWithSameDestination(des3, 8));

	final HitsDao dao = new DummySingleJobHitsDao(jobName, hits);
	final InformationAlgorithm algo = new InformationAlgorithm(dao);

	algo.setRuleWeight(1);

	final Set<Suggestion> expected = new TreeSet<>(SUGGESTIONS_IP_ATTRIBUTE_COMP);
	expected.add(new Suggestion(Destination.valueOf("0.0.0.0"), 100, 1));
	expected.add(new Suggestion(Destination.valueOf("0.0.0.1"), 50, 1));
	expected.add(new Suggestion(Destination.valueOf("0.0.0.2"), 17, 1));
	expected.add(new Suggestion(Destination.valueOf("0.0.0.3"), 8, 1));

	final Set<Suggestion> actual = new TreeSet<>(SUGGESTIONS_IP_ATTRIBUTE_COMP);
	actual.addAll(algo.getSuggestions(jobName, Collections.emptyList(), Filter.ANY_FILTER, 10,
		AttributeType.DESTINATION));

	assertEqualsIPsSuggestion(expected, actual);
    }

    @Test
    public void getSeggestionsTestRuleWeight127() throws IOException, ParseException {
	final String jobName = "testJobName";

	// Generate hits
	final Destination des0 = Destination.valueOf("0.0.0.0");
	final Destination des1 = Destination.valueOf("0.0.0.1");
	final Destination des2 = Destination.valueOf("0.0.0.2");
	final Destination des3 = Destination.valueOf("0.0.0.3");
	final List<Hit> hits = new ArrayList<>();
	hits.addAll(generateHitsWithSameDestination(des0, 100));
	hits.addAll(generateHitsWithSameDestination(des1, 50));
	hits.addAll(generateHitsWithSameDestination(des2, 17));
	hits.addAll(generateHitsWithSameDestination(des3, 8));

	final HitsDao dao = new DummySingleJobHitsDao(jobName, hits);
	final InformationAlgorithm algo = new InformationAlgorithm(dao);

	algo.setRuleWeight(127);

	final Set<Suggestion> expected = new TreeSet<>(SUGGESTIONS_IP_ATTRIBUTE_COMP);
	expected.add(new Suggestion(Destination.valueOf("0.0.0.0"), 100, 1.0 / 127));
	expected.add(new Suggestion(Destination.valueOf("0.0.0.1"), 50, 1.0 / 127));
	expected.add(new Suggestion(Destination.valueOf("0.0.0.2/31"), 25, 1.0 / 222.1838));

	final Set<Suggestion> actual = new TreeSet<>(SUGGESTIONS_IP_ATTRIBUTE_COMP);
	actual.addAll(algo.getSuggestions(jobName, Collections.emptyList(), Filter.ANY_FILTER, 10,
		AttributeType.DESTINATION));

	assertEqualsIPsSuggestion(expected, actual);
    }

    @Test
    public void getSeggestionsTestRuleWeight128() throws IOException, ParseException {
	final String jobName = "testJobName";

	// Generate hits
	final Destination des0 = Destination.valueOf("0.0.0.0");
	final Destination des1 = Destination.valueOf("0.0.0.1");
	final Destination des2 = Destination.valueOf("0.0.0.2");
	final Destination des3 = Destination.valueOf("0.0.0.3");
	final List<Hit> hits = new ArrayList<>();
	hits.addAll(generateHitsWithSameDestination(des0, 100));
	hits.addAll(generateHitsWithSameDestination(des1, 50));
	hits.addAll(generateHitsWithSameDestination(des2, 17));
	hits.addAll(generateHitsWithSameDestination(des3, 8));

	final HitsDao dao = new DummySingleJobHitsDao(jobName, hits);
	final InformationAlgorithm algo = new InformationAlgorithm(dao);

	algo.setRuleWeight(128);

	final Set<Suggestion> expected = new TreeSet<>(SUGGESTIONS_IP_ATTRIBUTE_COMP);
	expected.add(new Suggestion(Destination.valueOf("0.0.0.0/30"), 175, 1.0 / 478));

	final Set<Suggestion> actual = new TreeSet<>(SUGGESTIONS_IP_ATTRIBUTE_COMP);
	actual.addAll(algo.getSuggestions(jobName, Collections.emptyList(), Filter.ANY_FILTER, 10,
		AttributeType.DESTINATION));

	assertEqualsIPsSuggestion(expected, actual);
    }

    @Test
    public void getSeggestionsTestRuleWeight183() throws IOException, ParseException {
	final String jobName = "testJobName";

	// Generate hits
	final Destination des0 = Destination.valueOf("0.0.0.0");
	final Destination des1 = Destination.valueOf("0.0.0.1");
	final Destination des2 = Destination.valueOf("0.0.0.2");
	final Destination des3 = Destination.valueOf("0.0.0.3");
	final List<Hit> hits = new ArrayList<>();
	hits.addAll(generateHitsWithSameDestination(des0, 100));
	hits.addAll(generateHitsWithSameDestination(des1, 50));
	hits.addAll(generateHitsWithSameDestination(des2, 17));
	hits.addAll(generateHitsWithSameDestination(des3, 8));

	final HitsDao dao = new DummySingleJobHitsDao(jobName, hits);
	final InformationAlgorithm algo = new InformationAlgorithm(dao);

	algo.setRuleWeight(183);

	final Set<Suggestion> expected = new TreeSet<>(SUGGESTIONS_IP_ATTRIBUTE_COMP);
	expected.add(new Suggestion(Destination.valueOf("0.0.0.0/30"), 175, 1.0 / 533));

	final Set<Suggestion> actual = new TreeSet<>(SUGGESTIONS_IP_ATTRIBUTE_COMP);
	actual.addAll(algo.getSuggestions(jobName, Collections.emptyList(), Filter.ANY_FILTER, 10,
		AttributeType.DESTINATION));

	assertEqualsIPsSuggestion(expected, actual);
    }

    @Test
    public void getSeggestionsTestRuleWeight184() throws IOException, ParseException {
	final String jobName = "testJobName";

	// Generate hits
	final Destination des0 = Destination.valueOf("0.0.0.0");
	final Destination des1 = Destination.valueOf("0.0.0.1");
	final Destination des2 = Destination.valueOf("0.0.0.2");
	final Destination des3 = Destination.valueOf("0.0.0.3");
	final List<Hit> hits = new ArrayList<>();
	hits.addAll(generateHitsWithSameDestination(des0, 100));
	hits.addAll(generateHitsWithSameDestination(des1, 50));
	hits.addAll(generateHitsWithSameDestination(des2, 17));
	hits.addAll(generateHitsWithSameDestination(des3, 8));

	final HitsDao dao = new DummySingleJobHitsDao(jobName, hits);
	final InformationAlgorithm algo = new InformationAlgorithm(dao);

	algo.setRuleWeight(184);

	final Set<Suggestion> expected = new TreeSet<>(SUGGESTIONS_IP_ATTRIBUTE_COMP);
	expected.add(new Suggestion(Destination.valueOf("0.0.0.0/30"), 175, 1.0 / 534));

	final Set<Suggestion> actual = new TreeSet<>(SUGGESTIONS_IP_ATTRIBUTE_COMP);
	actual.addAll(algo.getSuggestions(jobName, Collections.emptyList(), Filter.ANY_FILTER, 10,
		AttributeType.DESTINATION));

	assertEqualsIPsSuggestion(expected, actual);
    }

    @Test
    public void getSeggestionsTestRuleWeight95() throws IOException, ParseException {
	final String jobName = "testJobName";

	// Generate hits
	final Destination des0 = Destination.valueOf("0.0.0.0");
	final Destination des1 = Destination.valueOf("0.0.0.1");
	final Destination des2 = Destination.valueOf("0.0.0.2");
	final Destination des3 = Destination.valueOf("0.0.0.3");
	final List<Hit> hits = new ArrayList<>();
	hits.addAll(generateHitsWithSameDestination(des0, 100));
	hits.addAll(generateHitsWithSameDestination(des1, 50));
	hits.addAll(generateHitsWithSameDestination(des2, 17));
	hits.addAll(generateHitsWithSameDestination(des3, 8));

	final HitsDao dao = new DummySingleJobHitsDao(jobName, hits);
	final InformationAlgorithm algo = new InformationAlgorithm(dao);

	algo.setRuleWeight(95);

	final Set<Suggestion> expected = new TreeSet<>(SUGGESTIONS_IP_ATTRIBUTE_COMP);
	expected.add(new Suggestion(Destination.valueOf("0.0.0.0"), 100, 1.0 / 95));
	expected.add(new Suggestion(Destination.valueOf("0.0.0.1"), 50, 1.0 / 95));
	expected.add(new Suggestion(Destination.valueOf("0.0.0.2"), 17, 1.0 / 95));
	expected.add(new Suggestion(Destination.valueOf("0.0.0.3"), 8, 1.0 / 95));

	final Set<Suggestion> actual = new TreeSet<>(SUGGESTIONS_IP_ATTRIBUTE_COMP);
	actual.addAll(algo.getSuggestions(jobName, Collections.emptyList(), Filter.ANY_FILTER, 10,
		AttributeType.DESTINATION));

	assertEqualsIPsSuggestion(expected, actual);
    }

    @Test
    public void getSeggestionsTestRuleWeight96() throws IOException, ParseException {
	final String jobName = "testJobName";

	// Generate hits
	final Destination des0 = Destination.valueOf("0.0.0.0");
	final Destination des1 = Destination.valueOf("0.0.0.1");
	final Destination des2 = Destination.valueOf("0.0.0.2");
	final Destination des3 = Destination.valueOf("0.0.0.3");
	final List<Hit> hits = new ArrayList<>();
	hits.addAll(generateHitsWithSameDestination(des0, 100));
	hits.addAll(generateHitsWithSameDestination(des1, 50));
	hits.addAll(generateHitsWithSameDestination(des2, 17));
	hits.addAll(generateHitsWithSameDestination(des3, 8));

	final HitsDao dao = new DummySingleJobHitsDao(jobName, hits);
	final InformationAlgorithm algo = new InformationAlgorithm(dao);

	algo.setRuleWeight(96);

	final Set<Suggestion> expected = new TreeSet<>(SUGGESTIONS_IP_ATTRIBUTE_COMP);
	expected.add(new Suggestion(Destination.valueOf("0.0.0.0"), 100, 1.0 / 96));
	expected.add(new Suggestion(Destination.valueOf("0.0.0.1"), 50, 1.0 / 96));
	expected.add(new Suggestion(Destination.valueOf("0.0.0.2/31"), 25, 1.0 / 191.1838));

	final Set<Suggestion> actual = new TreeSet<>(SUGGESTIONS_IP_ATTRIBUTE_COMP);
	actual.addAll(algo.getSuggestions(jobName, Collections.emptyList(), Filter.ANY_FILTER, 10,
		AttributeType.DESTINATION));

	assertEqualsIPsSuggestion(expected, actual);
    }

    private static void assertEqualsIPsSuggestion(final Collection<Suggestion> expected,
	    final Collection<Suggestion> actual) {
	final Set<Suggestion> expectedSet = new TreeSet<>(SUGGESTIONS_IP_ATTRIBUTE_COMP);
	final Set<Suggestion> actualSet = new TreeSet<>(SUGGESTIONS_IP_ATTRIBUTE_COMP);
	expectedSet.addAll(expected);
	actualSet.addAll(actual);

	for (final Iterator<Suggestion> expectedIterator = expectedSet.iterator(), actualIterator = actualSet
		.iterator(); expectedIterator.hasNext() && actualIterator.hasNext();) {
	    final Suggestion expectedSuggestion = expectedIterator.next();
	    final Suggestion actualSuggestion = actualIterator.next();
	    if (!Objects.equals(expectedSuggestion.getAttribute(), actualSuggestion.getAttribute())
		    || expectedSuggestion.getSize() != actualSuggestion.getSize()
		    || !equals(expectedSuggestion.getScore(), actualSuggestion.getScore(), 0.001)
		    || expectedIterator.hasNext() ^ actualIterator.hasNext())
		// Always fails
		assertEquals(expectedSet, actualSet);
	}
    }

    private static Set<Hit> generateHitsWithSameDestination(final Destination destination, final int amount) {
	final Set<Hit> hits = new HashSet<>();
	while (hits.size() < amount)
	    hits.add(new Hit(Arrays.asList(destination, FirewallTestsUtility.getRandomSource())));
	return hits;
    }

    @SuppressWarnings("unused")
    private static class DummySingleJobHitsDao implements HitsDao {

	private final String m_jobName;
	private final Set<Hit> m_hits;

	public DummySingleJobHitsDao(final String jobName, final Iterable<Hit> hits) {
	    m_jobName = Objects.requireNonNull(jobName);
	    m_hits = new HashSet<>();
	    for (final Hit hit : hits)
		m_hits.add(hit);
	}

	@Override
	public Iterable<Hit> getHits(final String jobName, final Iterable<Rule> rules, final Filter filter)
		throws IOException, ParseException {
	    return getHitsInternal(jobName, rules, filter);
	}

	@Override
	public int getHitsNumber(final String jobName, final Iterable<Rule> rules, final Filter filter)
		throws IOException, ParseException {
	    return getHitsInternal(jobName, rules, filter).size();
	}

	@Override
	public void initJob(final String jobName, final Iterable<Hit> hits) throws IOException {
	    // To nothing
	}

	private Set<Hit> getHitsInternal(final String jobName, final Iterable<Rule> rules, final Filter filter) {
	    if (!m_jobName.equals(jobName))
		return Collections.emptySet();

	    final Set<Hit> filteredHits = new HashSet<>();
	    for (final Hit hit : m_hits)
		if (DaoUtils.isMatch(hit, rules, filter))
		    filteredHits.add(hit);
	    return filteredHits;
	}

    }

}
