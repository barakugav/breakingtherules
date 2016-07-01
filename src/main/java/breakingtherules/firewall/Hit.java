package breakingtherules.firewall;

import java.util.List;

/**
 * Single firewall hit.
 * <p>
 * A firewall hit is a record of communication between different objects in a
 * network. The record usually contains the address (IP) of the source of the
 * communication the the destination. The hit can record more data, each
 * represented as an attribute of the hit.
 * <p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * 
 * @see Source
 * @see Destination
 */
public class Hit extends AttributesContainer {

    /**
     * Construct new Hit from a list of attributes.
     * 
     * @param attributes
     *            list of this hit's attributes.
     * @throws NullPointerException
     *             if the attribute list is null.
     * @throws IllegalArgumentException
     *             if the list contains two attributes of the same type.
     */
    public Hit(final List<Attribute> attributes) {
	super(attributes);
    }

    /**
     * Copy constructor.
     * <p>
     * This method has no use other then for subclasses because, there is no
     * need to copy a hit - all it's field are finals.
     * 
     * @param hit
     *            existing hit.
     * @throws NullPointerException
     *             if the copied hit is null.
     */
    protected Hit(final Hit hit) {
	super(hit);
    }

    /**
     * Construct new hit from attributes array.
     * <p>
     * This constructor should be used carefully, see
     * {@link AttributesContainer#AttributesContainer(Attribute[])}.
     * <p>
     * 
     * @param attributes
     *            array of the hits attributes.
     * @throws NullPointerException
     *             if the attributes array is null.
     */
    Hit(final Attribute[] attributes) {
	super(attributes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
	return o instanceof Hit && super.equals(o);
    }

    /**
     * Create a mutation of this hit by changing one of the attributes.
     * 
     * @param attribute
     *            the new attribute the mutation should contain.
     * @return new hit, which is mutation of this hit that contains the desire
     *         attribute
     */
    public Hit createMutation(final Attribute attribute) {
	final Attribute[] mutatedAttribues = m_attributes.clone();
	mutatedAttribues[attribute.getType().ordinal()] = attribute;
	return new Hit(mutatedAttribues);
    }

}
