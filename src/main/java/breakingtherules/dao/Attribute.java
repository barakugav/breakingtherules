package breakingtherules.dao;

/**
 * Attribute of a hit
 */
public abstract class Attribute {

    /**
     * All types of attributes
     */
    public enum AttType {
	Source, Destination, Service
    }

    /**
     * Type of the attribute
     */
    private AttType m_type;

    /**
     * Constructor
     * 
     * @param type
     *            type of the attribute
     */
    public Attribute(AttType type) {
	m_type = type;
    }

    /**
     * Get the type of the attribute
     * 
     * @return type of the attribute
     */
    public AttType getType() {
	return m_type;
    }

    /**
     * Checks if this attribute contain another
     * 
     * @param other
     *            another attribute to compare to
     * @return true if this attribute contains other, else - false
     */
    public abstract boolean contain(Attribute other);

}
