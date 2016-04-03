package breakingtherules.firewall;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The AttributesContainer class is a container of attributes
 */
public abstract class AttributesContainer {

    /**
     * The attributes this container contains
     */
    private final Attribute[] m_attributes;

    /**
     * Constructor
     * 
     * @param attributes
     *            the attributes of this container
     */
    public AttributesContainer(List<Attribute> attributes) {
	if (attributes == null) {
	    throw new IllegalArgumentException("Tried to create hit with null attributes");
	}
	m_attributes = toArray(attributes);
    }

    /**
     * Get the attributes of the hit
     * 
     * @return list of the hit's attributes
     */
    public List<Attribute> getAttributes() {
	return Arrays.asList(m_attributes);
    }

    /**
     * Get specific attribute by type
     * 
     * @param type
     *            wanted attribute type
     * @return the wanted attribute
     */
    public Attribute getAttribute(String type) {
	return getAttribute(Attribute.typeStrToTypeId(type));
    }

    /**
     * Get specific attribute by type id
     * 
     * @param typeId
     *            wanted attribute type id
     * @return the wanted attribute
     */
    public Attribute getAttribute(int typeId) {
	return (0 <= typeId || typeId < m_attributes.length) ? m_attributes[typeId] : null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
	if (o == null) {
	    return false;
	} else if (o == this) {
	    return true;
	} else if (!(o instanceof AttributesContainer)) {
	    return false;
	}

	AttributesContainer other = (AttributesContainer) o;
	for (int i = 0; i < Attribute.TYPES_COUNT; i++) {
	    Attribute thisAttr = m_attributes[i];
	    Attribute otherAttr = other.m_attributes[i];
	    if (!Objects.equals(thisAttr, otherAttr)) {
		return false;
	    }
	}
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	int h = 1;
	for (Attribute attribute : m_attributes) {
	    h = h * 31 + (attribute == null ? 0 : attribute.hashCode());
	}
	return h;
    }

    public String toString() {
	String str = "";
	for (Attribute att : m_attributes) {
	    str += att + ", ";
	}
	return str;
    }

    /**
     * Convert attributes list to array of attributes
     * 
     * @param attributesList
     *            list of attributes
     * @return array of the same attributes
     * @throws IllegalArgumentException
     *             if there are more than one attributes of the same type
     */
    private static Attribute[] toArray(List<Attribute> attributesList) {
	Attribute[] attributesArr = new Attribute[Attribute.TYPES_COUNT];
	for (Attribute attribute : attributesList) {
	    int attId = attribute.getTypeId();
	    if (attributesArr[attId] != null) {
		throw new IllegalArgumentException(
			"More then one attribute of the same type (" + attribute.getType() + ")");
	    }
	    attributesArr[attId] = attribute;
	}
	return attributesArr;
    }

}
