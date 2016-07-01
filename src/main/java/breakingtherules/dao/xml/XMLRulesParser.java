package breakingtherules.dao.xml;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Rule;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;

public class XMLRulesParser {

    private Source.Cache sourceCache;
    private Destination.Cache destinationCache;
    private Service.Cache serviceCache;

    XMLRulesParser() {
    }

    Source.Cache getSourceCache() {
	return sourceCache;
    }

    Destination.Cache getDestinationCache() {
	return destinationCache;
    }

    Service.Cache getServiceCache() {
	return serviceCache;
    }

    void setSourceCache(final Source.Cache cache) {
	sourceCache = cache;
    }

    void setDestinationCache(final Destination.Cache cache) {
	destinationCache = cache;
    }

    void setServiceCache(final Service.Cache cache) {
	serviceCache = cache;
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
	// Read attributes from element
	final String source = ruleElm.getAttribute(Attribute.SOURCE_TYPE);
	final String destination = ruleElm.getAttribute(Attribute.DESTINATION_TYPE);
	final String service = ruleElm.getAttribute(Attribute.SERVICE_TYPE);

	// Convert strings to attributes

	// TODO - almost exact code as XMLHitsParser.parseHit(Element).

	final List<Attribute> attributes = new ArrayList<>();
	try {
	    attributes.add(Source.valueOf(source, sourceCache));
	    attributes.add(Destination.valueOf(destination, destinationCache));
	    attributes.add(Service.valueOf(service, serviceCache));
	} catch (Exception e) {
	    throw new XMLParseException(e);
	}
	return new Rule(attributes);
    }

    Element createElement(final Document doc, final Rule rule) {
	final Element ruleElm = doc.createElement(XMLDaoConfig.RULE_TAG);
	for (final Attribute attribute : rule) {
	    ruleElm.setAttribute(attribute.getType(), attribute.toString());
	}
	return ruleElm;
    }

}
