package breakingtherules.tests.dao;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

import breakingtherules.dao.es.HitsElasticDao;
import breakingtherules.dao.xml.HitsXmlDao;
import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;

public class ElasticDaoTest {

    private static HitsElasticDao hitsDao;
    
    @BeforeClass
    public static void initDao() {
	// hitsDao = new HitsElasticDao();
    }

    @Test
    public void insertHits() throws IOException {
	// HitsXmlDao xmlDao = new HitsXmlDao();
	// ListDto<Hit> allHits = xmlDao.getHits(0, new ArrayList<Rule>(), new
	// Filter(new ArrayList<Attribute>()));
	// for (Hit h : allHits.getData()) {
	// hitsDao.addHit(h);
	// }
	// System.out.println("hi");
    }
}
