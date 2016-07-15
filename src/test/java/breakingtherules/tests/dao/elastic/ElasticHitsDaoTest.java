package breakingtherules.tests.dao.elastic;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import breakingtherules.dao.ParseException;
import breakingtherules.dao.elastic.ElasticHitsDao;
import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;
import breakingtherules.tests.TestBase;
import breakingtherules.tests.firewall.FirewallTestsUtility;

@SuppressWarnings("javadoc")
public class ElasticHitsDaoTest extends TestBase {

    private static ElasticHitsDao hitsDao;

    private static final String JOB_NAME = String.valueOf(new Random().nextInt());

    private static boolean jobInitialized = false;

    @Test
    public void manyHitsExistAfterAdding() throws IOException {
	checkJob();
	final int SIZE = 10;
	final List<Hit> newHits = new ArrayList<>();
	for (int i = 0; i < SIZE; i++)
	    newHits.add(createHit());
	hitsDao.addHits(newHits, JOB_NAME);
	final ListDto<Hit> hits = hitsDao.getHitsList(JOB_NAME, new ArrayList<Rule>(), Filter.ANY_FILTER);
	assertTrue(hits.getSize() >= SIZE);
    }

    @Test
    public void numberOfHitsIsCorrect() throws IOException, ParseException {
	checkJob();
	final int SIZE = 10;
	final List<Hit> newHits = new ArrayList<>();
	for (int i = 0; i < SIZE; i++)
	    newHits.add(createHit());
	hitsDao.addHits(newHits, JOB_NAME);
	final int beginIndex = 2;
	final int endIndex = 4;
	final ListDto<Hit> hits = hitsDao.getHitsList(JOB_NAME, new ArrayList<Rule>(), Filter.ANY_FILTER, beginIndex,
		endIndex);
	assertEquals(endIndex - beginIndex, hits.getData().size());
    }

    @Test
    public void singleHitExistsAfterAdding() throws IOException {
	checkJob();
	final Hit newHit = createHit();
	hitsDao.addHit(newHit, JOB_NAME);
	final ListDto<Hit> hits = hitsDao.getHitsList(JOB_NAME, new ArrayList<Rule>(), Filter.ANY_FILTER);
	assertTrue(hits.getSize() > 0);
    }

    @AfterClass
    public static void deleteFakeJobAndCleanDao() throws IOException {
	if (jobInitialized)
	    hitsDao.deleteJob(JOB_NAME);
	hitsDao.cleanup();
    }

    @BeforeClass
    public static void initDaoAndJob() throws Exception {
	try {
	    hitsDao = new ElasticHitsDao();
	    if (hitsDao.doesJobExist(JOB_NAME))
		throw new Exception("Job number exists. Choose different job number.");
	    jobInitialized = true;
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw e;
	}
    }

    private static void checkJob() {
	if (!jobInitialized)
	    fail("Job wasn't initialized");
    }

    private static Hit createHit() {
	final List<Attribute> attributes = new ArrayList<>();
	attributes.add(Source.valueOf(FirewallTestsUtility.getRandomIP()));
	attributes.add(Destination.valueOf(FirewallTestsUtility.getRandomIP()));
	attributes.add(Service.valueOf((short) rand.nextInt(256), rand.nextInt(1 << 16)));
	return new Hit(attributes);
    }

}
