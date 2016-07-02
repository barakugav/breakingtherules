package breakingtherules.dao.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import breakingtherules.firewall.Hit;

/**
 * Parser that parses hits from {@link Element}.
 * <p>
 * Caches can be set to reduce object creations and memory use.
 * <p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @see Hit
 * @see XMLHitsDao
 */
class XMLHitsParser extends AbstractXMLAttributesContainerParser {

    /**
     * Construct new parser.
     */
    XMLHitsParser() {
    }

    /**
     * Creates {@link Hit} object from {@link Element} XML object
     * 
     * @param hitElm
     *            XML element with hit attributes
     * @return hit object with the element attributes
     * @throws XMLParseException
     *             if failed to parse element to hit
     */
    Hit parseHit(final Element hitElm) throws XMLParseException {
	try {
	    return new Hit(parseAttributesContainer(hitElm));
	} catch (final Exception e) {
	    throw new XMLParseException(e);
	}
    }

    /**
     * Create new XML element of hit.
     * <p>
     * 
     * @param doc
     *            the parent document.
     * @param hit
     *            the parsed hit.
     * @return XML element that represent the hit.
     */
    Element createElement(final Document doc, final Hit hit) {
	return fillElement(doc.createElement(XMLDaoConfig.HIT_TAG), hit);
    }

}
