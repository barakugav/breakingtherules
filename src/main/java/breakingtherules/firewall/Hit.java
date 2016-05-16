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

    public static final Comparator<Hit> IDS_COMPARATOR;

    static {
	IDS_COMPARATOR = new Comparator<Hit>() {

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
	return o instanceof Hit && super.equals(o) && m_id == ((Hit) o).m_id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.AttributesContainer#hashCode()
     */
    @Override
    public int hashCode() {
	return super.hashCode() + m_id << 16;
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

    public static Hit mutate(final Hit hit, final Attribute attribute) {
	final Attribute[] mutatedAttribues = hit.m_attributes.clone();
	mutatedAttribues[attribute.getTypeId()] = attribute;
	return new Hit(hit.m_id, mutatedAttribues);
    }

}
