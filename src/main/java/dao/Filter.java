package dao;

import java.util.Vector;

import dao.Attribute.AttType;

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
     * Constructor
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
     * Get specific attribute of this filter
     * 
     * @param type
     *            wanted attribute type
     * @return the filter wanted attribute
     */
    public Attribute getAttribute(AttType type) {
	for (Attribute attribute : m_attributes)
	    if (attribute.getType() == type)
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
	    AttType attributeType = filterAttribute.getType();
	    Attribute hitAttribute = hit.getAttribute(attributeType);

	    if (!filterAttribute.contain(hitAttribute))
		return false;
	}
	return true;
    }

}
