package breakingtherules.tests.dao.xml;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import breakingtherules.dao.xml.XMLHitsParser;
import breakingtherules.firewall.Hit;
import breakingtherules.tests.firewall.FirewallTestsUtility;

@SuppressWarnings("javadoc")
public class XMLHitsDaoTest extends AbstractXMLTest {

    @Test
    public void writeAndParseHitsTest() throws Exception {
	runTempFileTest(getCurrentMethodName(), XML_SUFFIX, file -> {
	    try {
		final int numberOfHits = rand.nextInt(10_000);

		final List<Hit> expected = new ArrayList<>(numberOfHits);

		for (int i = numberOfHits; i-- != 0;)
		    expected.add(new Hit(Arrays.asList(FirewallTestsUtility.getRandomSource(),
			    FirewallTestsUtility.getRandomDestination(), FirewallTestsUtility.getRandomService())));

		XMLHitsParser.writeHits(expected, file.getAbsolutePath());

		final List<Hit> actual = XMLHitsParser.parseAllHits(file.getAbsolutePath());

		assertEquals(expected, actual);

	    } catch (final Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }
	});
    }

    @Test
    public void writeHitsTest() throws Exception {
	runTempFileTest(getCurrentMethodName(), XML_SUFFIX, file -> {
	    try {
		final int numberOfHits = rand.nextInt(10_000);

		final List<Hit> hits = new ArrayList<>(numberOfHits);

		for (int i = numberOfHits; i-- != 0;)
		    hits.add(new Hit(Arrays.asList(FirewallTestsUtility.getRandomSource(),
			    FirewallTestsUtility.getRandomDestination(), FirewallTestsUtility.getRandomService())));

		XMLHitsParser.writeHits(hits, file.getAbsolutePath());
	    } catch (final Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }
	});
    }

}
