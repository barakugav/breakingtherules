package breakingtherules.firewall;

import java.util.Comparator;
import java.util.List;

/**
 * Hit with attributes
 */
public class Hit extends AttributesContainer {

    /**
     * Id of the hit
     */
    private final int m_id;

    /**
     * Comparator of hits comparing hits by ids
     */
    public static final Comparator<Hit> IDS_COMPARATOR;

    static {
	IDS_COMPARATOR = new Comparator<Hit>() {

	    /*
	     * (non-Javadoc)
	     * 
	     * @see java.util.Comparator#compare(java.lang.Object,
	     * java.lang.Object)
	     */
	    @Override
	    public int compare(final Hit o1, final Hit o2) {
		return o1.m_id - o2.m_id;
	    }

	};
    }

    /**
     * Constructor
     * 
     * @param id
     *            id of this hit
     * @param attributes
     *            list of this hit's attributes
     * @throws IllegalArgumentException
     *             if id isn't positive or attributes is null
     */
    public Hit(final int id, final List<Attribute> attributes) {
	super(attributes);
	if (id < 0) {
	    throw new IllegalArgumentException("Id should be positive number");
	}
	m_id = id;
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
    protected Hit(final int id, final Attribute[] attributes) {
	super(attributes);
	m_id = id;
    }

    /**
     * Get the id of the hit
     * 
     * @return if of the hit
     */
    public int getId() {
	return m_id;
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

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.AttributesContainer#hashCode()
     */
    @Override
    public int hashCode() {
	return super.hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.AttributesContainer#toString()
     */
    @Override
    public String toString() {
	return "{ID = " + m_id + ", " + super.toString() + "}";
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
	// TODO-FIXME - use other id
	return new Hit(m_id, mutatedAttribues);
    }

}
