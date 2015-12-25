package breakingtherules.firewall;

import java.util.List;

/**
 * Hit with attributes
 */
public class Hit {

    /**
     * Id of the hit
     */
    private final int m_id;

    /**
     * List of this hit's attributes
     */
    private final List<Attribute> m_attributes;

    /**
     * Constructor
     * 
     * @param id
     *            id of this hit
     * @param attributes
     *            list of this hit's attributes
     * @throws IllegalArgumentException
     *             if id isn't positive or attributes is null
     */
    public Hit(int id, List<Attribute> attributes) throws IllegalArgumentException {
	if (id < 0) {
	    throw new IllegalArgumentException("Id shouldbe positive number");
	} else if (attributes == null) {
	    throw new IllegalArgumentException("Tried to create hit with null attributes");
	}
	m_id = id;
	m_attributes = attributes;
    }

    /**
     * Get the id of the hit
     * 
     * @return if of the hit
     */
    public int getId() {
	return m_id;
    }

    /**
     * Get the attributes of the hit
     * 
     * @return list of the hit's attributes
     */
    public List<Attribute> getAttributes() {
	return m_attributes;
    }

    /**
     * Get specific attribute of this hit
     * 
     * @param type
     *            wanted attribute type
     * @return the hit's wanted attribute
     */
    public Attribute getAttribute(String type) {
	for (Attribute attribute : m_attributes) {
	    if (attribute.getType().equals(type)) {
		return attribute;
	    }
	}
	return null;
    }

}
