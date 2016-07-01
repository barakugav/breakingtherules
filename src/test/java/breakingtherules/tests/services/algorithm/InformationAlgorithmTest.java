package breakingtherules.tests.services.algorithm;

import java.util.List;

import org.junit.Test;

import breakingtherules.dao.HitsDao;
import breakingtherules.dao.RulesDao;
import breakingtherules.dao.xml.XMLHitsDao;
import breakingtherules.dao.xml.XMLRulesDao;
import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Rule;
import breakingtherules.firewall.Attribute.AttributeType;
import breakingtherules.services.algorithm.InformationAlgorithm;
import breakingtherules.services.algorithm.Suggestion;
import breakingtherules.tests.TestBase;

@SuppressWarnings("javadoc")
public class InformationAlgorithmTest extends TestBase {

    private static final int RULE_WEIGHT = 25;
    private static final String JOB_NAME = "4";
    private static final int NUMBER_OF_SUGGESTIONS = 10;
    private static final AttributeType ATTRIBUTE = AttributeType.Destination;
    private static final boolean PRINT_RESULTS = false;

    @Test
    public void getSuggestionTest() throws Exception {
	RulesDao rulesDao = new XMLRulesDao();
	HitsDao hitsDao = new XMLHitsDao();
	Filter filter = Filter.ANY_FILTER;
	ListDto<Rule> rulesDto = rulesDao.getRules(JOB_NAME);
	List<Rule> rules = rulesDto.getData();

	InformationAlgorithm algorithm = new InformationAlgorithm();
	algorithm.setRuleWeight(RULE_WEIGHT);
	List<Suggestion> suggestions = algorithm.getSuggestions(hitsDao, JOB_NAME, rules, filter, NUMBER_OF_SUGGESTIONS,
		ATTRIBUTE);

	if (PRINT_RESULTS) {
	    System.out.println("\nResults:");
	    for (Suggestion sug : suggestions) {
		Attribute att = sug.getAttribute();
		System.out.println(att.toString());
	    }
	}
    }

}
