package breakingtherules.tests.dao.xml;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import breakingtherules.dao.xml.XMLHitsDao;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.tests.TestBase;
import breakingtherules.tests.firewall.FirewallTestsUtility;

@SuppressWarnings("javadoc")
public class XMLHitsDaoTest extends TestBase {

    @Test
    public void writeHitsTest() throws IOException {
	tempFileTest("writeHitsTest", ".xml", file -> {
	    try {
		final int numberOfHits = rand.nextInt(10_000);

		final List<Hit> hits = new ArrayList<>(numberOfHits);

		for (int i = numberOfHits; i-- != 0;) {
		    hits.add(new Hit(Arrays.asList(FirewallTestsUtility.getRandomSource(),
			    FirewallTestsUtility.getRandomDestination(), FirewallTestsUtility.getRandomService())));
		}

		XMLHitsDao.writeHits(hits, file.getAbsolutePath());
	    } catch (final Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }
	});
    }

    @Test
    public void writeAndParseHitsTest() throws IOException {
	tempFileTest("writeAndParseHitsTest", ".xml", file -> {
	    try {
		final int numberOfHits = rand.nextInt(10_000);

		final List<Hit> expected = new ArrayList<>(numberOfHits);

		for (int i = numberOfHits; i-- != 0;) {
		    expected.add(new Hit(Arrays.asList(FirewallTestsUtility.getRandomSource(),
			    FirewallTestsUtility.getRandomDestination(), FirewallTestsUtility.getRandomService())));
		}

		XMLHitsDao.writeHits(expected, file.getAbsolutePath());

		final List<Hit> actual = XMLHitsDao.parseHits(file.getAbsolutePath(), Collections.emptyList(),
			Filter.ANY_FILTER);

		assertEquals(expected, actual);

	    } catch (final Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }
	});
    }

}
