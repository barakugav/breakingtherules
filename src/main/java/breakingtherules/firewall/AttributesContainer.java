package breakingtherules.firewall;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import breakingtherules.firewall.Attribute.AttributeType;
import breakingtherules.utilities.ArrayIterator;

/**
 * The AttributesContainer class is a container of attributes.
 * <p>
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @see Attribute
 */
abstract class AttributesContainer implements Iterable<Attribute> {

    /**
     * The attributes this container contains.
     */
    final Attribute[] m_attributes;

    /**
     * Copy constructor.
     *
     * @param c
     *            other container.
     * @throws NullPointerException
     *             if the other container is null.
     */
    public AttributesContainer(final AttributesContainer c) {
	// Clone is not needed because everything is final
	m_attributes = c.m_attributes;
    }

    /**
     * Construct new container
     *
     * @param attributes
     *            the attributes of this container.
     * @throws NullPointerException
     *             if the attribute list is null.
     * @throws IllegalArgumentException
     *             if the list contains two attributes of the same type.
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
     * <p>
     *
     * @param attributes
     *            the attributes array of the container.
     * @throws NullPointerException
     *             if the attributes array is null.
     */
    AttributesContainer(final Attribute[] attributes) {
	m_attributes = Objects.requireNonNull(attributes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
	if (o == this)
	    return true;
	else if (!(o instanceof AttributesContainer))
	    return false;

	// Could use Arrays.equals but there are redundant null checks.
	final AttributesContainer other = (AttributesContainer) o;
	for (int i = Attribute.TYPE_COUNT; i-- != 0;)
	    if (!Objects.equals(m_attributes[i], other.m_attributes[i]))
		return false;
	return true;
    }

    /**
     * Get specific attribute by type.
     *
     * @param type
     *            wanted attribute type.
     * @return the wanted attribute.
     */
    public Attribute getAttribute(final AttributeType type) {
	return m_attributes[type.ordinal()];
    }

    /**
     * Get the attributes of the hit.
     *
     * @return list of the hit's attributes.
     */
    @JsonProperty("attributes")
    public List<Attribute> getAttributes() {
	final List<Attribute> attributeList = new ArrayList<>(Attribute.TYPE_COUNT);
	for (final Attribute attribute : m_attributes)
	    if (attribute != null)
		attributeList.add(attribute);
	return attributeList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
	// Could use Arrays.hashCode but there are redundant null checks.
	int h = 17;
	for (int i = Attribute.TYPE_COUNT; i-- != 0;)
	    h = h * 31 + Objects.hashCode(m_attributes[i]);
	return h;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Attribute> iterator() {
	return new ArrayIterator<>(m_attributes, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
	final StringBuilder builder = new StringBuilder();
	builder.append('[');
	int index;
	for (index = 0; index < Attribute.TYPE_COUNT; index++) {
	    final Attribute attribute = m_attributes[index];
	    if (attribute != null) {
		builder.append(attribute.toString());
		break;
	    }
	}
	for (; ++index < Attribute.TYPE_COUNT;) {
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
	final Attribute[] attributesArr = new Attribute[Attribute.TYPE_COUNT];
	for (final Attribute attribute : attributesList)
	    if (attribute != null) {
		final int index = attribute.getType().ordinal();
		if (attributesArr[index] != null)
		    throw new IllegalArgumentException(
			    "More then one attribute of the same type (" + attribute.getType() + ")");
		attributesArr[index] = attribute;
	    }
	return attributesArr;
    }

}
