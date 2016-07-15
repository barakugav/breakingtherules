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
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.IP;
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
     * {@inheritDoc}
     */
    @Override
    public Rule getOriginalRule(final String jobName) throws IOException, XMLParseException {
	return getOriginalRuleByPath(XMLDaoConfig.getRulesFile(jobName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListDto<Rule> getRules(final String jobName) throws IOException, XMLParseException {
	final List<Rule> rules = getRulesByPath(XMLDaoConfig.getRulesFile(jobName));
	final int size = rules.size();
	return new ListDto<>(rules, 0, size, size);
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
    public List<Rule> getRulesByPath(final String repoPath) throws IOException, XMLParseException {
	return loadRules(repoPath);
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
	if (startIndex < 0)
	    throw new IllegalArgumentException("Start index < 0");
	else if (startIndex > endIndex)
	    throw new IllegalArgumentException("Start index > end index");

	// Extract all rules
	final List<Rule> rules = loadRules(repoPath);

	final int total = rules.size();
	if (startIndex >= total)
	    throw new IndexOutOfBoundsException("Start index bigger that total count");
	final List<Rule> subRulesList = Utility.subList(rules, startIndex, endIndex - startIndex);
	return new ListDto<>(subRulesList, startIndex, endIndex, total);
    }

    /**
     * Get the original rule from a file.
     *
     * @param fileName
     *            the file name.
     * @return the original rule parsed from the file.
     * @throws IOException
     *             if ant I/O errors occurs.
     * @throws XMLParseException
     *             if the data in the file is invalid.
     */
    public static Rule getOriginalRuleByPath(final String fileName) throws IOException, XMLParseException {
	// Load from file
	final Document repositoryDoc;
	try {
	    repositoryDoc = XMLUtilities.readFile(fileName);
	} catch (final SAXException e) {
	    throw new XMLParseException(e);
	}

	// Get all rules from repository
	final NodeList originalRulesList = repositoryDoc.getElementsByTagName(XMLDaoConfig.ORIGINAL_RULE_TAG);
	if (originalRulesList.getLength() != 1)
	    throw new XMLParseException("Expected 1 original rule, actual " + originalRulesList.getLength());
	return new XMLRulesParser().parseRule((Element) originalRulesList.item(0));
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
	} catch (final ParserConfigurationException e) {
	    // Shouldn't happen.
	    throw new InternalError(e);
	}
	final Document doc = builder.newDocument();

	final XMLRulesParser parser = new XMLRulesParser();

	final Element repoElm = doc.createElement(XMLDaoConfig.REPOSITORY_TAG);
	for (final Rule rule : rules)
	    repoElm.appendChild(parser.createElement(doc, rule));

	// Original rule
	final Element originalRuleElm = doc.createElement(XMLDaoConfig.ORIGINAL_RULE_TAG);
	repoElm.appendChild(parser.fillElement(originalRuleElm, originalRule));

	doc.appendChild(repoElm);
	XMLUtilities.writeFile(fileName, doc);
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
	final NodeList rulesList = repositoryDoc.getElementsByTagName(XMLDaoConfig.RULE_TAG);

	final XMLRulesParser parser = new XMLRulesParser();
	final IP.Cache ipsCache = new IP.Cache();
	parser.setSourceCache(new Source.Cache(ipsCache));
	parser.setDestinationCache(new Destination.Cache(ipsCache));
	parser.setServiceCache(new Service.Cache());

	// Parse into rule objects
	final ArrayList<Rule> rules = new ArrayList<>(rulesList.getLength());
	final int length = rulesList.getLength();
	for (int i = 0; i < length; i++) {
	    final Node ruleNode = rulesList.item(i);
	    if (ruleNode.getNodeType() == Node.ELEMENT_NODE) {
		final Element ruleElm = (Element) ruleNode;
		final Rule rule = parser.parseRule(ruleElm);
		rules.add(rule);
	    }
	}
	rules.trimToSize();

	return rules;
    }

}
