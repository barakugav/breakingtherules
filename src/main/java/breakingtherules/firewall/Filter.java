package breakingtherules.firewall;

import java.util.Vector;

import breakingtherules.dao.HitsDao;
import breakingtherules.firewall.Attribute.AttType;

/**
 * Filter of hits
 * 
 * @see Hit
 * @see HitsDao
 */
public class Filter {

    private Vector<Attribute> m_attributes;

    /*--------------------Methods--------------------*/

    /**
     * Constructor of empty filter
     * 
     * Creates an empty filter
     */
    public Filter() {
	m_attributes = new Vector<Attribute>();
    }

    /**
     * Constructor
     * 
     * Creates filter based on given attributes
     * 
     * @param attributes
     *            vector of wanted attributes
     */
    public Filter(Vector<Attribute> attributes) {
	m_attributes = attributes;
    }

    /**
     * Get the attributes of this filter
     * 
     * @return vector of this filter's attributes
     */
    public Vector<Attribute> getAttributes() {
	return m_attributes;
    }

    /**
     * Get specific attribute of this filter
     * 
     * @param type
     *            wanted attribute type
     * @return the filter wanted attribute
     */
    public Attribute getAttribute(AttType type) {
	for (Attribute attribute : m_attributes)
	    if (attribute.getAttType() == type)
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
	    AttType attributeType = filterAttribute.getAttType();
	    Attribute hitAttribute = hit.getAttribute(attributeType);

	    if (!filterAttribute.contains(hitAttribute))
		return false;
	}
	return true;
    }

}
