package breakingtherules.dao.elastic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.search.SearchHit;

import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;

class ElasticHitsParser {

    private Source.Cache sourceCache;
    private Destination.Cache destinationCache;
    private Service.Cache serviceCache;

    ElasticHitsParser() {
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
     * Translates a SearchHit that represents a firewall Hit, into a firewall
     * Hit object
     * 
     * @param searchHit
     *            The outcome of an ElasticSearch search - a Hit, representing a
     *            firewall hit
     * @throws IllegalArgumentException
     *             If the searchHit is not in a valid hit format
     * @return A firewall Hit object with the values from the search
     */
    Hit parseHit(final SearchHit searchHit) {
	final Map<String, Object> fields = searchHit.getSource();

	final List<Attribute> attrs = new ArrayList<>(3);

	final Object allAtributes = fields.get(ElasticDaoConfig.FIELD_ATTRIBUTES);
	if (!(allAtributes instanceof List)) {
	    throw new IllegalArgumentException("The searchHit it not in valid hit format");
	}
	for (final Object attributeObj : (List<?>) allAtributes) {
	    if (!(attributeObj instanceof Map)) {
		throw new IllegalArgumentException("The searchHit it not in valid hit format");
	    }
	    final Map<?, ?> attributeHash = (Map<?, ?>) attributeObj;
	    final int attrTypeID = ((Integer) attributeHash.get(ElasticDaoConfig.FIELD_ATTR_TYPEID)).intValue();
	    final String attrValue = (String) attributeHash.get(ElasticDaoConfig.FIELD_ATTR_VALUE);

	    Attribute attribute;
	    switch (attrTypeID) {
	    case Attribute.SOURCE_TYPE_ID:
		attribute = Source.valueOf(attrValue, sourceCache);
		break;
	    case Attribute.DESTINATION_TYPE_ID:
		attribute = Destination.valueOf(attrValue, destinationCache);
		break;
	    case Attribute.SERVICE_TYPE_ID:
		attribute = Service.valueOf(attrValue, serviceCache);
		break;
	    default:
		throw new IllegalArgumentException("Unkown type id: " + attrTypeID);
	    }
	    attrs.add(attribute);
	}
	return new Hit(attrs);
    }

}
