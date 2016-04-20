package breakingtherules.firewall;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import breakingtherules.dao.HitsDao;

/**
 * Filter of hits, base on given attributes
 * 
 * @see Hit
 * @see HitsDao
 */
public class Filter extends AttributesContainer {

    /**
     * Constructor
     * 
     * Creates filter based on given attributes
     * 
     * @param attributes
     *            list of wanted attributes
     */
    public Filter(List<Attribute> attributes) {
	super(attributes);
    }

    /**
     * Checks if hit is matching the filter
     * 
     * @param hit
     *            hit to compare to the filter
     * @return true if all hit's attribute contained in the filter, else - false
     */
    public boolean isMatch(Hit hit) {
	for (Attribute filterAttribute : getAttributes()) {
	    int attributeType = filterAttribute.getTypeId();
	    Attribute hitAttribute = hit.getAttribute(attributeType);
	    if (!filterAttribute.contains(hitAttribute)) {
		return false;
	    }
	}
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.AttributesContainer#getAttributes()
     */
    @Override
    @JsonProperty("attributes")
    public List<Attribute> getAttributes() {
	return Arrays.asList(m_attributes);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * breakingtherules.firewall.AttributesContainer#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
	if (!super.equals(o)) {
	    return false;
	}
	return o instanceof Filter;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.AttributesContainer#hashCode()
     */
    @Override
    public int hashCode() {
	return super.hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.AttributesContainer#clone()
     */
    @Override
    public Object clone() {
	try {
	    return super.clone();
	} catch (CloneNotSupportedException e) {
	    // this shouldn't happen, since we are Cloneable
	    throw new InternalError(e);
	}
    }

    /**
     * Get a filter that allow any hit ('Any' filter)
     * 
     * @return 'Any' filter
     */
    public static Filter getAnyFilter() {
	List<Attribute> attributes = new ArrayList<Attribute>();
	attributes.add(Source.ANY_SOURCE);
	attributes.add(Destination.ANY_DESTINATION);
	attributes.add(Service.ANY_SERVICE);
	return new Filter(attributes);
    }

}
