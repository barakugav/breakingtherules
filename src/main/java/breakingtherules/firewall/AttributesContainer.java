package breakingtherules.firewall;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import breakingtherules.utilities.ArraysUtilities.ArrayIterator;
import breakingtherules.utilities.Utility;

/**
 * The AttributesContainer class is a container of attributes
 */
abstract class AttributesContainer implements Iterable<Attribute> {

    /**
     * The attributes this container contains
     */
    protected final Attribute[] m_attributes;

    /**
     * Constructor
     * 
     * @param attributes
     *            the attributes of this container
     */
    public AttributesContainer(final List<Attribute> attributes) {
	m_attributes = toArray(attributes);
    }

    /**
     * Construct new attributes container with attributes array.
     * <p>
     * This constructor should be used carefully: the constructor doesn't checks
     * if the attributes are valid or if they are in the right order in the
     * array.
     * <p>
     * This constructor is meant to be used by subclasses to create a mutation
     * copy of their own.
     * 
     * @param attributes
     *            the attributes array of the container
     */
    protected AttributesContainer(final Attribute[] attributes) {
	m_attributes = Objects.requireNonNull(attributes);
    }

    /**
     * Constructor
     * 
     * @param c
     *            container with attributes to construct this container
     */
    public AttributesContainer(final AttributesContainer c) {
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
    public Attribute getAttribute(final String type) {
	return getAttribute(Attribute.typeStrToTypeId(type));
    }

    /**
     * Get specific attribute by type id
     * 
     * @param typeId
     *            wanted attribute type id
     * @return the wanted attribute
     */
    public Attribute getAttribute(final int typeId) {
	return (0 <= typeId && typeId < Attribute.TYPES_COUNT) ? m_attributes[typeId] : null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<Attribute> iterator() {
	return new ArrayIterator<>(m_attributes);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object o) {
	if (o == this) {
	    return true;
	} else if (!(o instanceof AttributesContainer)) {
	    return false;
	}

	final AttributesContainer other = (AttributesContainer) o;
	final Attribute[] thisAttributes = m_attributes;
	final Attribute[] otherAttributes = other.m_attributes;
	for (int i = 0; i < Attribute.TYPES_COUNT; i++) {
	    final Attribute thisAttr = thisAttributes[i];
	    final Attribute otherAttr = otherAttributes[i];
	    if (thisAttr != null ? !thisAttr.equals(otherAttr) : otherAttr != null) {
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
	for (final Attribute attribute : m_attributes) {
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
    private static Attribute[] toArray(final List<Attribute> attributesList) {
	final Attribute[] attributesArr = new Attribute[Attribute.TYPES_COUNT];
	for (final Attribute attribute : attributesList) {
	    final int attId = attribute.getTypeId();
	    if (attributesArr[attId] != null) {
		throw new IllegalArgumentException(
			"More then one attribute of the same type (" + attribute.getType() + ")");
	    }
	    attributesArr[attId] = attribute;
	}
	return attributesArr;
    }

}
