package breakingtherules.firewall;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import breakingtherules.dao.HitsDao;

/**
 * Filter of hits
 * 
 * @see Hit
 * @see HitsDao
 */
public class Filter {

    /**
     * 
     */
    private List<Attribute> m_attributes;

    /**
     * 
     */
    private static final Attribute[] ANY_FILTER = new Attribute[] {
	    Source.createAnySourceIPv4(),
	    Destination.createAnySourceIPv4(),
	    Service.createAnyService()
    };

    /*--------------------Methods--------------------*/

    /**
     * Constructor of empty filter
     * 
     * Creates an 'Any' filter
     */
    public Filter() {
	m_attributes = new ArrayList<Attribute>();
	for (Attribute attribute : ANY_FILTER)
	    m_attributes.add(attribute);
    }

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
	for (Attribute attribute : m_attributes)
	    if (attribute.getType().equals(type))
		return attribute;
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

	    if (!filterAttribute.contains(hitAttribute))
		return false;
	}
	return true;
    }

}
