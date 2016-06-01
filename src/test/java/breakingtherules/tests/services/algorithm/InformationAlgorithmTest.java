package breakingtherules.tests.services.algorithm;

import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import breakingtherules.dao.HitsDao;
import breakingtherules.dao.RulesDao;
import breakingtherules.dao.xml.HitsXmlDao;
import breakingtherules.dao.xml.RulesXmlDao;
import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Rule;
import breakingtherules.services.algorithm.InformationAlgorithm;
import breakingtherules.services.algorithm.Suggestion;
import breakingtherules.tests.TestBase;

public class InformationAlgorithmTest extends TestBase {

    private static final int RULE_WEIGHT = 25;
    private static final int JOB_ID = 4;
    private static final int NUMBER_OF_SUGGESTIONS = 10;
    private static final String ATTRIBUTE = Attribute.DESTINATION_TYPE;
    private static final boolean PRINT_RESULTS = false;

    @Test
    public void getSuggestionTest() {
	System.out.println("# InformationAlgorithmTest getSuggestionTest");
	try {
	    RulesDao rulesDao = new RulesXmlDao();
	    HitsDao hitsDao = new HitsXmlDao();
	    Filter filter = Filter.ANY_FILTER;
	    ListDto<Rule> rulesDto = rulesDao.getRules(JOB_ID);
	    List<Rule> rules = rulesDto.getData();

	    InformationAlgorithm algorithm = new InformationAlgorithm();
	    algorithm.setRuleWeight(RULE_WEIGHT);
	    List<Suggestion> suggestions = algorithm.getSuggestions(hitsDao, JOB_ID, rules, filter, ATTRIBUTE,
		    NUMBER_OF_SUGGESTIONS);

	    if (PRINT_RESULTS) {
		System.out.println("\nResults:");
		for (Suggestion sug : suggestions) {
		    Attribute att = sug.getAttribute();
		    System.out.println(att.toString());
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	}
    }

}
