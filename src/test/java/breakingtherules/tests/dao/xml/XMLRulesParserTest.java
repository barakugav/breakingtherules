package breakingtherules.tests.dao.xml;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import breakingtherules.dao.xml.XMLRulesParser;
import breakingtherules.firewall.Rule;
import breakingtherules.tests.firewall.FirewallTestsUtility;

@SuppressWarnings("javadoc")
public class XMLRulesParserTest extends AbstractXMLTest {

    @Test
    public void writeAndReadOriginalRuleTest() throws Exception {
	runTempFileTest(getCurrentMethodName(), XML_SUFFIX, file -> {
	    try {
		final Rule expected = new Rule(Arrays.asList(FirewallTestsUtility.getRandomSource(),
			FirewallTestsUtility.getRandomDestination(), FirewallTestsUtility.getRandomService()));

		XMLRulesParser.writeRules(file.getAbsolutePath(), Collections.emptyList(), expected);

		final Rule actual = XMLRulesParser.parseOriginalRule(file.getAbsolutePath());

		assertEquals(expected, actual);

	    } catch (final Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }
	});
    }

    @Test
    public void writeAndReadRulesTest() throws Exception {
	runTempFileTest(getCurrentMethodName(), XML_SUFFIX, file -> {
	    try {
		final int numberOfRules = 10_000;

		final Rule expectedOriginalRule = new Rule(Arrays.asList(FirewallTestsUtility.getRandomSource(),
			FirewallTestsUtility.getRandomDestination(), FirewallTestsUtility.getRandomService()));
		final List<Rule> expectedRules = new ArrayList<>(numberOfRules);

		for (int i = numberOfRules; i-- != 0;)
		    expectedRules.add(new Rule(Arrays.asList(FirewallTestsUtility.getRandomSource(),
			    FirewallTestsUtility.getRandomDestination(), FirewallTestsUtility.getRandomService())));

		XMLRulesParser.writeRules(file.getAbsolutePath(), expectedRules, expectedOriginalRule);

		final Rule actualOriginalRule = XMLRulesParser.parseOriginalRule(file.getAbsolutePath());
		final List<Rule> actualRules = XMLRulesParser.parseRules(file.getAbsolutePath());

		assertEquals(expectedOriginalRule, actualOriginalRule);
		assertEquals(expectedRules, actualRules);

	    } catch (final Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }
	});
    }

    @Test
    public void writeOriginalRuleTest() throws Exception {
	runTempFileTest(getCurrentMethodName(), XML_SUFFIX, file -> {
	    try {
		final Rule originalRule = new Rule(Arrays.asList(FirewallTestsUtility.getRandomSource(),
			FirewallTestsUtility.getRandomDestination(), FirewallTestsUtility.getRandomService()));

		XMLRulesParser.writeRules(file.getAbsolutePath(), Collections.emptyList(), originalRule);

	    } catch (final Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }
	});
    }

    @Test
    public void writeRulesTest() throws Exception {
	runTempFileTest(getCurrentMethodName(), XML_SUFFIX, file -> {
	    try {
		final int numberOfRules = 10_000;

		final Rule originalRule = new Rule(Arrays.asList(FirewallTestsUtility.getRandomSource(),
			FirewallTestsUtility.getRandomDestination(), FirewallTestsUtility.getRandomService()));
		final List<Rule> rules = new ArrayList<>(numberOfRules);

		for (int i = numberOfRules; i-- != 0;)
		    rules.add(new Rule(Arrays.asList(FirewallTestsUtility.getRandomSource(),
			    FirewallTestsUtility.getRandomDestination(), FirewallTestsUtility.getRandomService())));

		XMLRulesParser.writeRules(file.getAbsolutePath(), rules, originalRule);

	    } catch (final Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }
	});
    }

}
