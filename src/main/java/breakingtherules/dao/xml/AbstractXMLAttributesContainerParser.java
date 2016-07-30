package breakingtherules.dao.xml;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import breakingtherules.dao.AbstractParser;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Attribute.AttributeType;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;

/**
 * XML parser that parses attributes containers.
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @see Attribute
 */
class AbstractXMLAttributesContainerParser extends AbstractParser {

    /**
     * Parses all attributes from an XML element.
     *
     * @param elm
     *            the parent element of all attributes.
     * @return list of all attributes parsed from element.
     * @throws NullPointerException
     *             if the element is null.
     * @throws XMLParseException
     *             if failed to parse the attributes.
     */
    List<Attribute> parseAttributesContainer(final Element elm) throws XMLParseException {
	final NodeList children = elm.getChildNodes();
	final List<Attribute> attributes = new ArrayList<>(children.getLength());
	for (int i = children.getLength(); i-- != 0;) {
	    final Node child = children.item(i);
	    if (child.getNodeType() == Node.ELEMENT_NODE) {
		final Element childElm = (Element) child;
		final String name = childElm.getTagName();
		final String value = childElm.getTextContent();
		if (name == null || value == null)
		    throw new XMLParseException("Unkown format");

		final Attribute attribute;
		try {
		    switch (AttributeType.valueOfIgnoreCase(name)) {
		    case SOURCE:
			attribute = Source.valueOf(value, sourceCache);
			break;
		    case DESTINATION:
			attribute = Destination.valueOf(value, destinationCache);
			break;
		    case SERVICE:
			attribute = Service.valueOf(value, serviceCache);
			break;
		    default:
			throw new XMLParseException("Unkown attribute");
		    }
		} catch (final IllegalArgumentException e) {
		    throw new XMLParseException(e);
		}

		attributes.add(attribute);
	    }
	}
	return attributes;
    }

    /**
     * Add all attributes of an attributes container to parent element.
     *
     * @param elm
     *            the parent element.
     * @param attributesContainer
     *            the attributes source.
     * @return the constructed element. The input element.
     * @throws NullPointerException
     *             if the element is null or the container is null.
     */
    static Element fillElement(final Element elm, final Iterable<Attribute> attributesContainer) {
	final Document doc = elm.getOwnerDocument();
	for (final Attribute attribute : attributesContainer) {
	    final Element attElm = doc.createElement(attribute.getType().lowerCaseName());
	    attElm.setTextContent(attribute.toString());
	    elm.appendChild(attElm);
	}
	return elm;
    }

}
