package breakingtherules.dao.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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

    private static final String REPOSITORY_TAG = "Repository";
    private static final String RULE_TAG = "rule";

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

    public static void writeRules(final String repoPath, final List<Rule> rules)
	    throws ParserConfigurationException, IOException {
	final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	final DocumentBuilder builder = factory.newDocumentBuilder();
	final Document document = builder.newDocument();
	final Element repoElm = document.createElement(REPOSITORY_TAG);
	for (final Rule rule : rules) {
	    final Element ruleElm = document.createElement(RULE_TAG);
	    for (final Attribute attribute : rule) {
		ruleElm.setAttribute(attribute.getType(), attribute.toString());
	    }
	    repoElm.appendChild(ruleElm);
	}
	document.appendChild(repoElm);
	UtilityXmlDao.writeFile(repoPath, document);
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
	final NodeList rulesList = repositoryDoc.getElementsByTagName(RULE_TAG);

	// Parse into rule objects
	final ArrayList<Rule> rules = new ArrayList<>(rulesList.getLength());
	final int length = rulesList.getLength();
	for (int i = 0; i < length; i++) {
	    final Node ruleNode = rulesList.item(i);
	    if (ruleNode.getNodeType() == Node.ELEMENT_NODE) {
		final Element ruleElm = (Element) ruleNode;
		final Rule rule = createRule(ruleElm);
		rules.add(rule);
	    }
	}
	rules.trimToSize();

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
	final String source = ruleElm.getAttribute(Attribute.SOURCE_TYPE);
	final String destination = ruleElm.getAttribute(Attribute.DESTINATION_TYPE);
	final String service = ruleElm.getAttribute(Attribute.SERVICE_TYPE);

	// Convert strings to attributes
	final Source newSource = Source.create(source);
	final Destination newDestination = Destination.create(destination);
	final Service newService = Service.createFromString(service);

	// Create attributes vector
	final List<Attribute> attributes = new ArrayList<>();
	attributes.add(newSource);
	attributes.add(newDestination);
	attributes.add(newService);

	return new Rule(attributes);
    }

}
