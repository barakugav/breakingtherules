package breakingtherules.dao;

import java.util.Vector;

import breakingtherules.dao.Attribute.AttType;

/**
 * Hit with attributes
 */
public class Hit {

	/**
	 * Id of the hit
	 */
	private int m_id;

	/**
	 * Vector of this hit's attributes
	 */
	private Vector<Attribute> m_attributes;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            id of this hit
	 * @param attributes
	 *            vector of this hit's attributes
	 */
	public Hit(int id, Vector<Attribute> attributes) {
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
	 * Get specific attribute of this hit
	 * 
	 * @param type
	 *            wanted attribute type
	 * @return the hit's wanted attribute
	 */
	public Attribute getAttribute(AttType type) {
		for (Attribute attribute : m_attributes)
			if (attribute.getType() == type)
				return attribute;
		return null;
	}

}
