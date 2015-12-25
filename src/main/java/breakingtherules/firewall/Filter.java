package breakingtherules.firewall;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import breakingtherules.dao.HitsDao;

/**
 * Filter of hits, base on given attributes
 * 
 * @see Hit
 * @see HitsDao
 */
public class Filter {

    /**
     * List of the attributes this filter use to filter hits
     */
    private final List<Attribute> m_attributes;

    /*--------------------Methods--------------------*/

    /**
     * Constructor
     * 
     * Creates filter based on given attributes
     * 
     * @param attributes
     *            list of wanted attributes
     */
    public Filter(List<Attribute> attributes) {
	m_attributes = attributes;
    }

    /**
     * Get the attributes of this filter
     * 
     * @return list of this filter's attributes
     */
    public List<Attribute> getAttributes() {
	return m_attributes;
    }

    /**
     * Get specific attribute of this filter
     * 
     * @param type
     *            wanted attribute type
     * @return the filter wanted attribute
     */
    @JsonIgnore
    public Attribute getAttribute(String type) {
	for (Attribute attribute : m_attributes) {
	    if (attribute.getType().equals(type)) {
		return attribute;
	    }
	}
	return null;
    }

    /**
     * Checks if hit is matching the filter
     * 
     * @param hit
     *            hit to compare to the filter
     * @return true if all hit's attribute contained in the filter, else - false
     */
    public boolean isMatch(Hit hit) {
	for (Attribute filterAttribute : m_attributes) {
	    String attributeType = filterAttribute.getType();
	    Attribute hitAttribute = hit.getAttribute(attributeType);

	    if (!filterAttribute.contains(hitAttribute)) {
		return false;
	    }
	}
	return true;
    }

    /**
     * Get a filter that allow any hit ('Any' filter)
     * 
     * @return 'Any' filter
     */
    public static Filter getAnyFilter() {
	List<Attribute> attributes = new ArrayList<Attribute>();
	attributes.add(Source.createAnySource());
	attributes.add(Destination.createAnyDestination());
	attributes.add(Service.createAnyService());
	return new Filter(attributes);
    }

}
