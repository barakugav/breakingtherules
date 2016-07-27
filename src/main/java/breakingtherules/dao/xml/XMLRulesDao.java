package breakingtherules.dao.xml;

import java.io.IOException;
import java.util.List;

import breakingtherules.dao.AbstractCachedRulesDao;
import breakingtherules.dao.ParseException;
import breakingtherules.dao.RulesDao;
import breakingtherules.firewall.Rule;

/**
 * Implementation of {@link RulesDao} by XML repository.
 * <p>
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @see XMLRulesParser
 */
public class XMLRulesDao extends AbstractCachedRulesDao {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Rule getOriginalRuleInternal(final String jobName) throws IOException, ParseException {
	return XMLRulesParser.parseOriginalRule(XMLDaoConfig.getRulesFile(jobName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Rule> getRulesInternal(final String jobName) throws IOException, ParseException {
	return XMLRulesParser.parseRules(XMLDaoConfig.getRulesFile(jobName));
    }

}
