package breakingtherules.tests.services.algorithm;

import java.util.List;

import breakingtherules.dao.HitsDao;
import breakingtherules.dao.RulesDao;
import breakingtherules.dao.xml.XMLHitsDao;
import breakingtherules.dao.xml.XMLRulesDao;
import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Attribute.AttributeType;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Rule;
import breakingtherules.services.algorithm.InformationAlgorithm;
import breakingtherules.services.algorithm.Suggestion;
import breakingtherules.tests.TestBase;

@SuppressWarnings("javadoc")
public class InformationAlgorithmTest extends TestBase {

    private static final int RULE_WEIGHT = 25;
    private static final String JOB_NAME = "4";
    private static final int NUMBER_OF_SUGGESTIONS = 10;
    private static final AttributeType ATTRIBUTE = AttributeType.DESTINATION;
    private static final boolean PRINT_RESULTS = false;

    public void getSuggestionTest() throws Exception {
	final RulesDao rulesDao = new XMLRulesDao();
	final HitsDao hitsDao = new XMLHitsDao();
	final Filter filter = Filter.ANY_FILTER;
	final ListDto<Rule> rulesDto = rulesDao.getRules(JOB_NAME);
	final List<Rule> rules = rulesDto.getData();

	final InformationAlgorithm algorithm = new InformationAlgorithm(hitsDao);
	algorithm.setRuleWeight(RULE_WEIGHT);
	final List<Suggestion> suggestions = algorithm.getSuggestions(JOB_NAME, rules, filter, NUMBER_OF_SUGGESTIONS,
		ATTRIBUTE);

	if (PRINT_RESULTS) {
	    System.out.println("\nResults:");
	    for (final Suggestion sug : suggestions) {
		final Attribute att = sug.getAttribute();
		System.out.println(att.toString());
	    }
	}
    }

}
