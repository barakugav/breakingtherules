package breakingtherules.firewall;

import com.fasterxml.jackson.annotation.JsonProperty;

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
    private AttType m_AttType;

    /**
     * Constructor
     * 
     * @param type
     *            type of the attribute
     */
    public Attribute(AttType type) {
	m_AttType = type;
    }

    /**
     * Get the type of the attribute
     * 
     * @return type of the attribute
     */
    public AttType getAttType() {
	return m_AttType;
    }

    /**
     * Checks if this attribute contain another
     * 
     * @param other
     *            another attribute to compare to
     * @return true if this attribute contains other, else - false
     */
    public abstract boolean contains(Attribute other);
    
    @JsonProperty("str")
    public abstract String toString();

}
