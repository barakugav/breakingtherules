package breakingtherules.dao.elastic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.search.SearchHit;

import breakingtherules.dao.AbstractParser;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Attribute.AttributeType;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;

/**
 * Parser that parses firewall hits from {@link SearchHit}.
 * <p>
 * Caches can be set to reduce object creations and memory use.
 * <p>
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @see Hit
 * @see ElasticHitsDao
 */
class ElasticHitsParser extends AbstractParser {

    /**
     * Construct new parser.
     */
    ElasticHitsParser() {
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
	final Map<String, Object> fields = searchHit.getSourceAsMap();

	final List<Attribute> attrs = new ArrayList<>(3);

	final Object allAtributes = fields.get(ElasticDaoConfig.FIELD_ATTRIBUTES);
	if (!(allAtributes instanceof List))
	    throw new IllegalArgumentException("The searchHit it not in valid hit format");
	for (final Object attributeObj : (List<?>) allAtributes) {
	    if (!(attributeObj instanceof Map))
		throw new IllegalArgumentException("The searchHit it not in valid hit format");
	    final Map<?, ?> attributeHash = (Map<?, ?>) attributeObj;
	    final int attrTypeID = ((Integer) attributeHash.get(ElasticDaoConfig.FIELD_ATTR_TYPEID)).intValue();
	    final String attrValue = (String) attributeHash.get(ElasticDaoConfig.FIELD_ATTR_VALUE);

	    Attribute attribute;
	    switch (AttributeType.values()[attrTypeID]) {
	    case SOURCE:
		attribute = sourceCache != null ? sourceCache.valueOf(attrValue) : Source.valueOf(attrValue);
		break;
	    case DESTINATION:
		attribute = destinationCache != null ? destinationCache.valueOf(attrValue)
			: Destination.valueOf(attrValue);
		break;
	    case SERVICE:
		attribute = serviceCache != null ? serviceCache.valueOf(attrValue) : Service.valueOf(attrValue);
		break;
	    default:
		throw new IllegalArgumentException("Unkown type id: " + attrTypeID);
	    }
	    attrs.add(attribute);
	}
	return new Hit(attrs);
    }

}
