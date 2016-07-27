package breakingtherules.dao.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import breakingtherules.firewall.Destination;
import breakingtherules.firewall.IP;
import breakingtherules.firewall.Rule;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;

/**
 * Parser that parses rules from {@link Element}.
 * <p>
 * Caches can be set to reduce object creations and memory use.
 * <p>
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @see Rule
 * @see XMLRulesDao
 */
public class XMLRulesParser extends AbstractXMLAttributesContainerParser {

    /**
     * Construct new parser.
     */
    XMLRulesParser() {
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
    private Rule parseRule(final Element ruleElm) throws XMLParseException {
	try {
	    return new Rule(parseAttributesContainer(ruleElm));
	} catch (final Exception e) {
	    throw new XMLParseException(e);
	}
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
    public static Rule parseOriginalRule(final String fileName) throws IOException, XMLParseException {
	// Load from file
	final Document repositoryDoc;
	try {
	    repositoryDoc = XMLUtils.readFile(fileName);
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
     * Get all rules from file.
     *
     * @param fileName
     *            string file name.
     * @return all rules parsed from file.
     * @throws IOException
     *             if failed to read from memory.
     * @throws XMLParseException
     *             if the data is invalid.
     */
    public static List<Rule> parseRules(final String fileName) throws IOException, XMLParseException {
	// Check if this repository is already loaded
	// Load from file
	final Document repositoryDoc;
	try {
	    repositoryDoc = XMLUtils.readFile(fileName);
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
	final List<Rule> rules = new ArrayList<>(rulesList.getLength());
	final int length = rulesList.getLength();
	for (int i = 0; i < length; i++) {
	    final Node ruleNode = rulesList.item(i);
	    if (ruleNode.getNodeType() == Node.ELEMENT_NODE) {
		final Element ruleElm = (Element) ruleNode;
		final Rule rule = parser.parseRule(ruleElm);
		rules.add(rule);
	    }
	}
	return rules;
    }

    /**
     * Write a list of rules to a file.
     * <p>
     *
     * @param fileName
     *            the name of the output file.
     * @param rules
     *            all rules to write.
     * @param originalRule
     *            the original rule.
     * @throws IOException
     *             if any I/O errors occurs.
     */
    public static void writeRules(final String fileName, final Iterable<Rule> rules, final Rule originalRule)
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

	// All regular rules.
	final Element repoElm = doc.createElement(XMLDaoConfig.REPOSITORY_TAG);
	for (final Rule rule : rules)
	    repoElm.appendChild(fillElement(doc.createElement(XMLDaoConfig.RULE_TAG), rule));

	// Original rule
	repoElm.appendChild(fillElement(doc.createElement(XMLDaoConfig.ORIGINAL_RULE_TAG), originalRule));

	doc.appendChild(repoElm);
	XMLUtils.writeFile(fileName, doc);
    }

}
