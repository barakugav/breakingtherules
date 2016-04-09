package breakingtherules.tests.dao.csv;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import breakingtherules.dao.csv.CSVParser;
import breakingtherules.firewall.Hit;

public class CSVParserTest {

    private static final int JOB_ID = 0;
    private static final List<Integer> COLOMNS_TYPES = CSVParser.DEFAULT_COLUMNS_TYPES;
    private static final boolean PRINT = true;

    @Test
    public void parseTest() {
	System.out.println("# CSVParserTest parseTest");
	try {
	    List<Hit> hits = CSVParser.fromCSV(COLOMNS_TYPES, JOB_ID);
	    if (PRINT) {
		for (Hit hit : hits) {
		    System.out.println(hit);
		}
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	}
    }

}
