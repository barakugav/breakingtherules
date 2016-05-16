package breakingtherules.firewall;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import breakingtherules.utilities.ArraysUtilities;
import breakingtherules.utilities.Utility;

/**
 * The AttributesContainer class is a container of attributes
 */
abstract class AttributesContainer implements Iterable<Attribute> {

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
    protected AttributesContainer(List<Attribute> attributes) {
	m_attributes = toArray(attributes);
    }

    /**
     * Constructor
     * 
     * @param c
     *            container with attributes to construct this container
     */
    protected AttributesContainer(AttributesContainer c) {
	// Clone is not needed because everything is final
	m_attributes = c.m_attributes;
    }

    /**
     * Get the attributes of the hit
     * 
     * @return list of the hit's attributes
     */
    @JsonProperty("attributes")
    public List<Attribute> getAttributes() {
	return Collections.unmodifiableList(Arrays.asList(m_attributes));
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
	Attribute[] attributes = m_attributes;
	return (0 <= typeId && typeId < attributes.length) ? attributes[typeId] : null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<Attribute> iterator() {
	return ArraysUtilities.iterator(m_attributes);
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
	Attribute[] thisAttributes = m_attributes;
	Attribute[] otherAttributes = other.m_attributes;
	for (int i = 0; i < Attribute.TYPES_COUNT; i++) {
	    Attribute thisAttr = thisAttributes[i];
	    Attribute otherAttr = otherAttributes[i];
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return Utility.toString(m_attributes);
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
