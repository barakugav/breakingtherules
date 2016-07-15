package breakingtherules.dao.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import breakingtherules.firewall.Rule;

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
class XMLRulesParser extends AbstractXMLAttributesContainerParser {

    /**
     * Construct new parser.
     */
    XMLRulesParser() {
    }

    /**
     * Create an XML element from rule.
     *
     * @param doc
     *            the root document of the element.
     * @param rule
     *            the rules to parse.
     * @return element with the rules attributes.
     */
    Element createElement(final Document doc, final Rule rule) {
	return fillElement(doc.createElement(XMLDaoConfig.RULE_TAG), rule);
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
    Rule parseRule(final Element ruleElm) throws XMLParseException {
	try {
	    return new Rule(parseAttributesContainer(ruleElm));
	} catch (final Exception e) {
	    throw new XMLParseException(e);
	}
    }

}
