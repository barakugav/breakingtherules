package breakingtherules.firewall;

import java.util.List;

/**
 * Hit with attributes
 */
public class Hit extends AttributesContainer {

    /**
     * Constructor
     * 
     * @param attributes
     *            list of this hit's attributes
     * @throws IllegalArgumentException
     *             if id isn't positive or attributes is null
     */
    public Hit(final List<Attribute> attributes) {
	super(attributes);
    }

    /**
     * Construct new hit from id and attributes array.
     * <p>
     * This constructor should be used carefully, see
     * {@link AttributesContainer#AttributesContainer(Attribute[])}.
     * 
     * @param id
     *            id of the new hit
     * @param attributes
     *            array of the hits attributes
     */
    protected Hit(final Attribute[] attributes) {
	super(attributes);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * breakingtherules.firewall.AttributesContainer#equals(java.lang.Object)
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
	mutatedAttribues[attribute.getTypeId()] = attribute;
	return new Hit(mutatedAttribues);
    }

}
