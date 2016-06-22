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
import org.xml.sax.SAXException;

import breakingtherules.dao.RulesDao;
import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Rule;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;
import breakingtherules.utilities.Utility;

/**
 * Implementation of {@link RulesDao} by XML repository.
 * <p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * 
 */
@Component
public class XMLRulesDao implements RulesDao {

    /**
     * Tag of the whole repository in XML format.
     */
    private static final String REPOSITORY_TAG = "Repository";

    /**
     * Tag of a rule in XML format.
     */
    private static final String RULE_TAG = "rule";

    /**
     * Tag of the original rule in XML format.
     */
    private static final String ORIGINAL_RULE_TAG = "original-rule";


    /**
     * {@inheritDoc}
     */
    @Override
    public Rule getOriginalRule(final String jobName) throws IOException, XMLParseException {
	final String path = XMLDaoConfig.getRulesFile(jobName);

	// Load from file
	final Document repositoryDoc;
	try {
	    repositoryDoc = XMLUtilities.readFile(path);
	} catch (final SAXException e) {
	    throw new XMLParseException(e);
	}

	// Get all rules from repository
	final Element ruleElem = (Element) repositoryDoc.getElementsByTagName(ORIGINAL_RULE_TAG).item(0);
	final Rule rule = createRule(ruleElem);
	return rule;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListDto<Rule> getRules(final String jobName) throws IOException, XMLParseException {
	final String path = XMLDaoConfig.getRulesFile(jobName);
	return getRulesByPath(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListDto<Rule> getRules(final String jobName, final int startIndex, final int endIndex)
	    throws IOException, XMLParseException {
	final String path = XMLDaoConfig.getRulesFile(jobName);
	return getRulesByPath(path, startIndex, endIndex);
    }

    /**
     * Get all rules from repository by repository string path.
     * 
     * @param repoPath
     *            string path to repository.
     * @return all rules.
     * @throws IOException
     *             if failed to read from memory.
     * @throws XMLParseException
     *             if the data is invalid.
     */
    public ListDto<Rule> getRulesByPath(final String repoPath) throws IOException, XMLParseException {
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
     * @throws XMLParseException
     *             if the data is invalid.
     */
    public ListDto<Rule> getRulesByPath(final String repoPath, final int startIndex, final int endIndex)
	    throws IOException, XMLParseException {
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
     * Write a list of rules to a file.
     * <p>
     * 
     * @param fileName
     *            the name of the output file.
     * @param rules
     *            list of the rules to write.
     * @param originalRule
     *            the original rule.
     * @throws IOException
     *             if any I/O errors occurs.
     */
    public static void writeRules(final String fileName, final List<Rule> rules, final Rule originalRule)
	    throws IOException {
	final DocumentBuilder builder;
	try {
	    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    builder = factory.newDocumentBuilder();
	} catch (ParserConfigurationException e) {
	    // Shouldn't happen.
	    throw new InternalError(e);
	}
	final Document document = builder.newDocument();

	final Element repoElm = document.createElement(REPOSITORY_TAG);
	for (final Rule rule : rules) {
	    final Element ruleElm = document.createElement(RULE_TAG);
	    for (final Attribute attribute : rule) {
		ruleElm.setAttribute(attribute.getType(), attribute.toString());
	    }
	    repoElm.appendChild(ruleElm);
	}
	final Element ruleElm = document.createElement(ORIGINAL_RULE_TAG);
	for (final Attribute attribute : originalRule) {
	    ruleElm.setAttribute(attribute.getType(), attribute.toString());
	}
	repoElm.appendChild(ruleElm);

	document.appendChild(repoElm);
	XMLUtilities.writeFile(fileName, document);
    }

    /**
     * Load all the rules from repository
     * 
     * @param repoPath
     *            string path to repository file
     * @return list of loaded rules from repository
     * @throws IOException
     *             if failed to read from file
     * @throws XMLParseException
     *             if the data is invalid.
     */
    private static List<Rule> loadRules(final String repoPath) throws IOException, XMLParseException {
	// Check if this repository is already loaded
	// Load from file
	final Document repositoryDoc;
	try {
	    repositoryDoc = XMLUtilities.readFile(repoPath);
	} catch (final SAXException e) {
	    throw new XMLParseException(e);
	}

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
     * Creates {@link Rule} object from {@link Element} XML object.
     * <p>
     * 
     * @param ruleElm
     *            XML element with rule attributes
     * @return rule object with the element attributes
     * @throws XMLParseException
     *             if the data is invalid.
     */
    private static Rule createRule(final Element ruleElm) throws XMLParseException {
	// Read attributes from element
	final String source = ruleElm.getAttribute(Attribute.SOURCE_TYPE);
	final String destination = ruleElm.getAttribute(Attribute.DESTINATION_TYPE);
	final String service = ruleElm.getAttribute(Attribute.SERVICE_TYPE);

	// Convert strings to attributes

	final List<Attribute> attributes = new ArrayList<>();
	try {
	    attributes.add(Source.valueOf(source));
	    attributes.add(Destination.valueOf(destination));
	    attributes.add(Service.valueOf(service));
	} catch (Exception e) {
	    throw new XMLParseException(e);
	}
	return new Rule(attributes);
    }

}
