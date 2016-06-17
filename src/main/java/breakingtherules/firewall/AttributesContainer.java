package breakingtherules.firewall;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import breakingtherules.utilities.ArrayIterator;

/**
 * The AttributesContainer class is a container of attributes
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * @see Attribute
 */
abstract class AttributesContainer implements Iterable<Attribute> {

    /**
     * The attributes this container contains
     */
    final Attribute[] m_attributes;

    /**
     * Construct new container
     * 
     * @param attributes
     *            the attributes of this container
     * @throws NullPointerException
     *             if the attribute list is null
     * @throws IllegalArgumentException
     *             if the list contains two attributes of the same type
     */
    public AttributesContainer(final List<Attribute> attributes) {
	m_attributes = toArray(attributes);
    }

    /**
     * Copy constructor
     * 
     * @param c
     *            other container
     * @throws NullPointerException
     *             if the other container is null
     */
    public AttributesContainer(final AttributesContainer c) {
	// Clone is not needed because everything is final
	m_attributes = c.m_attributes;
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
     * @throws NullPointerException
     *             if the attributes array is null
     */
    AttributesContainer(final Attribute[] attributes) {
	m_attributes = Objects.requireNonNull(attributes);
    }

    /**
     * Get the attributes of the hit
     * 
     * @return list of the hit's attributes
     */
    @JsonProperty("attributes")
    public List<Attribute> getAttributes() {
	final List<Attribute> attributeList = new ArrayList<>(m_attributes.length);
	for (final Attribute attribute : m_attributes) {
	    if (attribute != null) {
		attributeList.add(attribute);
	    }
	}
	return attributeList;
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
	return new ArrayIterator<>(m_attributes, true);
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
	return Arrays.equals(m_attributes, other.m_attributes);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	return Arrays.hashCode(m_attributes);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	final StringBuilder builder = new StringBuilder();
	builder.append('[');
	int index;
	for (index = 0; index < m_attributes.length; index++) {
	    final Attribute attribute = m_attributes[index];
	    if (attribute != null) {
		builder.append(attribute.toString());
		break;
	    }
	}
	for (; index < m_attributes.length; index++) {
	    final Attribute attribute = m_attributes[index];
	    if (attribute != null) {
		builder.append(", ");
		builder.append(attribute.toString());
	    }
	}
	builder.append(']');
	return builder.toString();
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
	    if (attribute != null) {
		final int attId = attribute.getTypeId();
		if (attributesArr[attId] != null) {
		    throw new IllegalArgumentException(
			    "More then one attribute of the same type (" + attribute.getType() + ")");
		}
		attributesArr[attId] = attribute;
	    }
	}
	return attributesArr;
    }

}
