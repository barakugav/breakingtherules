package breakingtherules.tests.dao.xml;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import breakingtherules.dao.xml.XMLRulesDao;
import breakingtherules.firewall.Rule;
import breakingtherules.tests.TestBase;
import breakingtherules.tests.firewall.FirewallTestsUtility;

@SuppressWarnings("javadoc")
public class XMLRulesDaoTest extends TestBase {

    @Test
    public void writeOriginalRuleTest() throws IOException {
	tempFileTest("writeOriginalRuleTest", ".xml", file -> {
	    try {
		final Rule originalRule = new Rule(Arrays.asList(FirewallTestsUtility.getRandomSource(),
			FirewallTestsUtility.getRandomDestination(), FirewallTestsUtility.getRandomService()));

		XMLRulesDao.writeRules(file.getAbsolutePath(), Collections.emptyList(), originalRule);

	    } catch (final Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }
	});
    }

    @Test
    public void writeAndReadOriginalRuleTest() throws IOException {
	tempFileTest("writeAndReadOriginalRuleTest", ".xml", file -> {
	    try {
		final Rule expected = new Rule(Arrays.asList(FirewallTestsUtility.getRandomSource(),
			FirewallTestsUtility.getRandomDestination(), FirewallTestsUtility.getRandomService()));

		XMLRulesDao.writeRules(file.getAbsolutePath(), Collections.emptyList(), expected);

		final Rule actual = XMLRulesDao.getOriginalRuleByPath(file.getAbsolutePath());

		assertEquals(expected, actual);

	    } catch (final Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }
	});
    }

    @Test
    public void writeRulesTest() throws IOException {
	tempFileTest("writeRulesTest", ".xml", file -> {
	    try {
		final int numberOfRules = 10_000;

		final Rule originalRule = new Rule(Arrays.asList(FirewallTestsUtility.getRandomSource(),
			FirewallTestsUtility.getRandomDestination(), FirewallTestsUtility.getRandomService()));
		final List<Rule> rules = new ArrayList<>(numberOfRules);

		for (int i = numberOfRules; i-- != 0;) {
		    rules.add(new Rule(Arrays.asList(FirewallTestsUtility.getRandomSource(),
			    FirewallTestsUtility.getRandomDestination(), FirewallTestsUtility.getRandomService())));
		}

		XMLRulesDao.writeRules(file.getAbsolutePath(), rules, originalRule);

	    } catch (final Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }
	});
    }

    @Test
    public void writeAndReadRulesTest() throws IOException {
	tempFileTest("writeAndReadRulesTest", ".xml", file -> {
	    try {
		final int numberOfRules = 10_000;

		final Rule expectedOriginalRule = new Rule(Arrays.asList(FirewallTestsUtility.getRandomSource(),
			FirewallTestsUtility.getRandomDestination(), FirewallTestsUtility.getRandomService()));
		final List<Rule> expectedRules = new ArrayList<>(numberOfRules);

		for (int i = numberOfRules; i-- != 0;) {
		    expectedRules.add(new Rule(Arrays.asList(FirewallTestsUtility.getRandomSource(),
			    FirewallTestsUtility.getRandomDestination(), FirewallTestsUtility.getRandomService())));
		}

		XMLRulesDao.writeRules(file.getAbsolutePath(), expectedRules, expectedOriginalRule);

		final Rule actualOriginalRule = XMLRulesDao.getOriginalRuleByPath(file.getAbsolutePath());
		final List<Rule> actualRules = new XMLRulesDao().getRulesByPath(file.getAbsolutePath());

		assertEquals(expectedOriginalRule, actualOriginalRule);
		assertEquals(expectedRules, actualRules);

	    } catch (final Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }
	});
    }

}
