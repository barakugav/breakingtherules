package breakingtherules.dao.xml;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;

class XMLHitsParser {

    private Source.Cache sourceCache;
    private Destination.Cache destinationCache;
    private Service.Cache serviceCache;

    XMLHitsParser() {
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
     * Creates {@link Hit} object from {@link Element} XML object
     * 
     * @param hitElm
     *            XML element with hit attributes
     * @return hit object with the element attributes
     * @throws XMLParseException
     *             if failed to parse element to hit
     */
    Hit parseHit(final Element hitElm) throws XMLParseException {
	// Read attributes from element
	final String source = hitElm.getAttribute(XMLDaoConfig.SOURCE_TAG);
	final String destination = hitElm.getAttribute(XMLDaoConfig.DESTINATION_TAG);
	final String service = hitElm.getAttribute(XMLDaoConfig.SERVICE_TAG);

	if (source == null || source.isEmpty()) {
	    throw new XMLParseException("Source does not exist");
	}
	if (destination == null || destination.isEmpty()) {
	    throw new XMLParseException("Destination does not exist");
	}
	if (service == null || service.isEmpty()) {
	    throw new XMLParseException("Service does not exist");
	}

	// Convert strings to attributes
	final List<Attribute> attributes = new ArrayList<>();
	try {
	    attributes.add(Source.valueOf(source, sourceCache));
	    attributes.add(Destination.valueOf(destination, destinationCache));
	    attributes.add(Service.valueOf(service, serviceCache));

	} catch (final Exception e) {
	    throw new XMLParseException(e);
	}
	return new Hit(attributes);
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
	final Element elm = doc.createElement(XMLDaoConfig.HIT_TAG);
	for (final Attribute attribute : hit) {
	    elm.setAttribute(attribute.getType().toLowerCase(), attribute.toString());
	}
	return elm;
    }

}
