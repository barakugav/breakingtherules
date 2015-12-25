package breakingtherules.firewall;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Attribute of a hit
 */
public interface Attribute {

    /**
     * Checks if this attribute contain another
     * 
     * @param other
     *            another attribute to compare to
     * @return true if this attribute contains other, else - false
     */
    public boolean contains(Attribute other);

    /**
     * Get the type of the attribute
     * 
     * @return type of the attribute
     */
    public String getType();
    
    @Override 
    public boolean equals(Object o);
    
    @Override
    public int hashCode();

    @JsonProperty("str")
    public String toString();

}
