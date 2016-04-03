package breakingtherules.tests.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import breakingtherules.dao.es.HitsElasticDao;
import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;

public class ElasticDaoTest {

    private static HitsElasticDao hitsDao;

    private static final int JOB_NUMBER = 572299; // random

    private static boolean jobInitialized = false;

    private static int numOfHits = 0;

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

    private void checkJob() {
	if (!jobInitialized) {
	    fail("Job wasn't initialized");
	}
    }

    private Hit createHit() {
	List<Attribute> attributes = new ArrayList<Attribute>();
	int id = numOfHits;
	numOfHits++;
	attributes.add(new Source("128.76.2.9"));
	attributes.add(new Destination("128.76.2.9"));
	attributes.add(new Service("TCP 80"));
	System.out.println(numOfHits);
	return new Hit(id, attributes);
    }

    @Test
    public void singleHitExistsAfterAdding() throws IOException {
	checkJob();
	Hit newHit = createHit();
	hitsDao.addHit(newHit, JOB_NUMBER);
	ListDto<Hit> hits = hitsDao.getHits(JOB_NUMBER, new ArrayList<Rule>(), Filter.getAnyFilter());
	assertTrue(hits.getSize() > 0);
    }

    @Test
    public void manyHitsExistAfterAdding() throws IOException {
	checkJob();
	int SIZE = 10;
	List<Hit> newHits = new ArrayList<Hit>();
	for (int i = 0; i < SIZE; i++) {
	    newHits.add(createHit());
	}
	hitsDao.addHits(newHits, JOB_NUMBER);
	ListDto<Hit> hits = hitsDao.getHits(JOB_NUMBER, new ArrayList<Rule>(), Filter.getAnyFilter());
	assertTrue(hits.getSize() >= SIZE);
    }

    @Test
    public void numberOfHitsIsCorrect() throws IOException {
	checkJob();
	int SIZE = 10;
	List<Hit> newHits = new ArrayList<Hit>();
	for (int i = 0; i < SIZE; i++) {
	    newHits.add(createHit());
	}
	hitsDao.addHits(newHits, JOB_NUMBER);
	int beginIndex = 2;
	int endIndex = 4;
	ListDto<Hit> hits = hitsDao.getHits(JOB_NUMBER, new ArrayList<Rule>(), Filter.getAnyFilter(), beginIndex,
		endIndex);
	assertEquals(endIndex - beginIndex, hits.getData().size());
    }

}
