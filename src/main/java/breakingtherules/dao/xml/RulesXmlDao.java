package breakingtherules.dao.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import breakingtherules.dao.RulesDao;
import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Rule;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;
import breakingtherules.utilities.Utility;

/**
 * Implementation of {@link RulesDao} by XML repository
 */
@Component
public class RulesXmlDao implements RulesDao {

    /**
     * Constructor
     */
    public RulesXmlDao() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.dao.RulesDao#getOriginalRule(int)
     */
    @Override
    public Rule getOriginalRule(final int jobId) throws IOException {
	final String path = XmlDaoConfig.getRulesFile(jobId);
	// Load from file
	final Document repositoryDoc = UtilityXmlDao.readFile(path);

	// Get all rules from repository
	final Element ruleElem = (Element) repositoryDoc.getElementsByTagName("original-rule").item(0);
	final Rule rule = createRule(ruleElem);
	return rule;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.dao.RulesDao#getRules(int)
     */
    @Override
    public ListDto<Rule> getRules(final int jobId) throws IOException {
	final String path = XmlDaoConfig.getRulesFile(jobId);
	return getRulesByPath(path);
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.dao.RulesDao#getRules(int, int, int)
     */
    @Override
    public ListDto<Rule> getRules(final int jobId, final int startIndex, final int endIndex) throws IOException {
	final String path = XmlDaoConfig.getRulesFile(jobId);
	return getRulesByPath(path, startIndex, endIndex);
    }

    /**
     * Get all rules from repository by repository string path
     * 
     * @param repoPath
     *            string path to repository
     * @return all rules
     * @throws IOException
     *             if failed to read from memory
     */
    public ListDto<Rule> getRulesByPath(final String repoPath) throws IOException {
	final List<Rule> rules = loadRules(repoPath);
	final int size = rules.size();
	return new ListDto<>(rules, 0, size, size);
    }

    /**
     * Get rules from repository in range [startIndex, endIndex] by repository
     * string path
     * 
     * @param repoPath
     *            string path to repository
     * @param startIndex
     *            the end index of the rules list, including this index
     * @param endIndex
     *            the end index of the rules list, including this index
     * @return rules in range [startIndex, endIndex]
     * @throws IOException
     *             if failed to read from memory
     */
    public ListDto<Rule> getRulesByPath(final String repoPath, final int startIndex, final int endIndex)
	    throws IOException {
	if (startIndex < 0) {
	    throw new IllegalArgumentException("Start index < 0");
	} else if (startIndex > endIndex) {
	    throw new IllegalArgumentException("Start index > end index");
	}
	// Don't need to check that arguments aren't null, if they do, usual
	// null exception will be thrown later

	// Extract all rules
	final List<Rule> rules = loadRules(repoPath);

	final int total = rules.size();
	if (startIndex >= total) {
	    throw new IndexOutOfBoundsException("Start index bigger that total count");
	}
	final List<Rule> subRulesList = Utility.subList(rules, startIndex, endIndex - startIndex);
	return new ListDto<>(subRulesList, startIndex, endIndex, total);
    }

    /**
     * Load all the rules from repository
     * 
     * @param repoPath
     *            string path to repository file
     * @return list of loaded rules from repository
     * @throws IOException
     *             if failed to read from file
     */
    private static List<Rule> loadRules(final String repoPath) throws IOException {
	// Check if this repository is already loaded
	// Load from file
	final Document repositoryDoc = UtilityXmlDao.readFile(repoPath);

	// Get all rules from repository
	final NodeList rulesList = repositoryDoc.getElementsByTagName("rule");

	// Parse into rule objects
	final List<Rule> rules = new ArrayList<>();
	final int length = rulesList.getLength();
	for (int i = 0; i < length; i++) {
	    final Node ruleNode = rulesList.item(i);
	    if (ruleNode.getNodeType() == Node.ELEMENT_NODE) {
		final Element ruleElm = (Element) ruleNode;
		final Rule rule = createRule(ruleElm);
		rules.add(rule);
	    }
	}

	return rules;
    }

    /**
     * Creates {@link Rule} object from {@link Element} XML object
     * 
     * @param ruleElm
     *            XML element with rule attributes
     * @return rule object with the element attributes
     */
    private static Rule createRule(final Element ruleElm) {
	// Read attributes from element
	final String id = ruleElm.getAttribute("id");
	final String source = ruleElm.getAttribute("source");
	final String destination = ruleElm.getAttribute("destination");
	final String service = ruleElm.getAttribute("service");

	// Convert strings to attributes
	final int newId = Integer.parseInt(id);
	final Source newSource = Source.create(source);
	final Destination newDestination = Destination.create(destination);
	final Service newService = Service.create(service);

	// Create attributes vector
	final List<Attribute> attributes = new ArrayList<>();
	attributes.add(newSource);
	attributes.add(newDestination);
	attributes.add(newService);

	return new Rule(newId, attributes);
    }

}
