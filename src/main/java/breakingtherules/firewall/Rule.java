package breakingtherules.firewall;

import java.util.List;

/**
 * Rule that apply on hits by {@link Filter}
 */
public class Rule {

    /**
     * Filter of the rule
     */
    private final Filter m_filter;

    /**
     * Id of the rule
     */
    private final int m_id;

    /**
     * Constructor
     * 
     * @param filter
     *            filter of the rule
     */
    public Rule(int id, Filter filter) {
	m_id = id;
	m_filter = filter;
    }

    /**
     * 
     * Constructor
     * 
     * @param attributes
     *            attributes that represent the rule
     */
    public Rule(int id, List<Attribute> attributes) {
	m_id = id;
	m_filter = new Filter(attributes);
    }

    /**
     * Get the id of this rule
     * 
     * @return id of this rule
     */
    public int getId() {
	return m_id;
    }

    /**
     * Get the attributes of this rule
     * 
     * @return list of this rule's attributes
     */
    public List<Attribute> getAttributes() {
	return m_filter.getAttributes();
    }

    /**
     * Check if a hit is matching to the rule
     * 
     * @param hit
     *            hit to compare to filter
     * @return true if hit matched to rule's filter, else - false
     */
    public boolean isMatch(Hit hit) {
	return m_filter.isMatch(hit);
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
	} else if (!(o instanceof Rule)) {
	    return false;
	}

	Rule other = (Rule) o;
	if (m_id != other.m_id) {
	    return false;
	} else if (!m_filter.equals(other.m_filter)) {
	    return false;
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
	return m_id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
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
