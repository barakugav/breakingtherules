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
	    public int compare(Hit o1, Hit o2) {
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
    public Hit(int id, List<Attribute> attributes) {
	super(attributes);
	if (id < 0) {
	    throw new IllegalArgumentException("Id should be positive number");
	}
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
    public boolean equals(Object o) {
	return super.equals(o) && o instanceof Hit && m_id == ((Hit) o).m_id;
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

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.AttributesContainer#clone()
     */
    @Override
    public Object clone() {
	try {
	    return super.clone();
	} catch (CloneNotSupportedException e) {
	    // this shouldn't happen, since we are Cloneable
	    throw new InternalError(e);
	}
    }

}
