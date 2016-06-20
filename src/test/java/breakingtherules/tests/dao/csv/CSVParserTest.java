package breakingtherules.tests.dao.csv;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import breakingtherules.dao.csv.CSVDaoConfig;
import breakingtherules.dao.csv.CSVParseException;
import breakingtherules.dao.csv.CSVParser;
import breakingtherules.firewall.Hit;
import breakingtherules.tests.TestBase;

public class CSVParserTest extends TestBase {

    private static final String JOB_NAME = "0";
    private static final List<Integer> COLOMNS_TYPES = CSVParser.DEFAULT_COLUMNS_TYPES;
    private static final boolean PRINT = false;

    @Test
    public void parseTest() {
	try {
	    List<Hit> hits = CSVParser.parseHits(COLOMNS_TYPES, CSVDaoConfig.getHitsFile(JOB_NAME));
	    if (PRINT) {
		for (Hit hit : hits) {
		    System.out.println(hit);
		}
	    }
	} catch (IOException | CSVParseException e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	}
    }

}
