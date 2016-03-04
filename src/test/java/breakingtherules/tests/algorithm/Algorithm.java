package breakingtherules.tests.algorithm;

import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import breakingtherules.dao.HitsDao;
import breakingtherules.dao.RulesDao;
import breakingtherules.dao.xml.HitsDaoXml;
import breakingtherules.dao.xml.RulesDaoXml;
import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;
import breakingtherules.services.algorithm.InformationAlgorithm;
import breakingtherules.services.algorithm.Suggestion;
import breakingtherules.services.algorithm.SuggestionsAlgorithm;

public class Algorithm {

    private static final int JOB_ID = 0;
    private static final String ATTRIBUTE = Attribute.DESTINATION_TYPE;
    private static final boolean PRINT_RESULTS = false;

    @Test
    public void getSuggestionTest() {
	try {
	    RulesDao rulesDao = new RulesDaoXml();
	    HitsDao hitsDao = new HitsDaoXml();
	    Filter filter = Filter.getAnyFilter();
	    ListDto<Rule> rulesDto = rulesDao.getRules(JOB_ID);
	    List<Rule> rules = rulesDto.getData();
	    ListDto<Hit> hitsDto = hitsDao.getHits(JOB_ID, rules, filter);
	    List<Hit> hits = hitsDto.getData();

	    SuggestionsAlgorithm algorithm = new InformationAlgorithm();
	    List<Suggestion> suggestions = algorithm.getSuggestions(hits, ATTRIBUTE);

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
