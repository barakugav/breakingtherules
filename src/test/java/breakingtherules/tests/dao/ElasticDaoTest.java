package breakingtherules.tests.dao;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import breakingtherules.dao.elastic.HitsElasticDao;
import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;
import breakingtherules.tests.TestBase;

public class ElasticDaoTest extends TestBase {

    private static HitsElasticDao hitsDao;

    private static final int JOB_NUMBER = 572299; // random

    private static boolean jobInitialized = false;


    @BeforeClass
    public static void initDaoAndJob() throws Exception {
	try {
	    hitsDao = new HitsElasticDao();
	    if (hitsDao.doesJobExist(JOB_NUMBER)) {
		throw new Exception("Job number exists. Choose different job number.");
	    }
	    jobInitialized = true;
	} catch (Exception e) {
	    e.printStackTrace();
	    throw e;
	}
    }

    @AfterClass
    public static void deleteFakeJobAndCleanDao() {
	if (jobInitialized) {
	    hitsDao.deleteJob(JOB_NUMBER);
	}
	hitsDao.cleanup();
    }

    private static void checkJob() {
	if (!jobInitialized) {
	    fail("Job wasn't initialized");
	}
    }

    private static Hit createHit() {
	List<Attribute> attributes = new ArrayList<>();
	attributes.add(Source.create("128.76.2.9"));
	attributes.add(Destination.create("128.76.2.9"));
	attributes.add(Service.create("TCP 80"));
	return new Hit(attributes);
    }

    @Test
    public void singleHitExistsAfterAdding() throws IOException {
	checkJob();
	Hit newHit = createHit();
	hitsDao.addHit(newHit, JOB_NUMBER);
	ListDto<Hit> hits = hitsDao.getHits(JOB_NUMBER, new ArrayList<Rule>(), Filter.ANY_FILTER);
	assertTrue(hits.getSize() > 0);
    }

    @Test
    public void manyHitsExistAfterAdding() throws IOException {
	checkJob();
	final int SIZE = 10;
	List<Hit> newHits = new ArrayList<>();
	for (int i = 0; i < SIZE; i++) {
	    newHits.add(createHit());
	}
	hitsDao.addHits(newHits, JOB_NUMBER);
	ListDto<Hit> hits = hitsDao.getHits(JOB_NUMBER, new ArrayList<Rule>(), Filter.ANY_FILTER);
	assertTrue(hits.getSize() >= SIZE);
    }

    @Test
    public void numberOfHitsIsCorrect() throws IOException {
	checkJob();
	final int SIZE = 10;
	List<Hit> newHits = new ArrayList<>();
	for (int i = 0; i < SIZE; i++) {
	    newHits.add(createHit());
	}
	hitsDao.addHits(newHits, JOB_NUMBER);
	int beginIndex = 2;
	int endIndex = 4;
	ListDto<Hit> hits = hitsDao.getHits(JOB_NUMBER, new ArrayList<Rule>(), Filter.ANY_FILTER, beginIndex, endIndex);
	assertEquals(endIndex - beginIndex, hits.getData().size());
    }

}
