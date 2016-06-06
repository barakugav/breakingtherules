package breakingtherules.firewall;

import java.util.List;

/**
 * Rule that apply on hits by {@link Filter}
 */
public class Rule extends Filter {

    /**
     * Id of the rule
     */
    private final int m_id;

    public Rule(final int id, final Filter filter) {
	super(filter);
	m_id = id;
    }

    public Rule(final int id, final List<Attribute> attributes) {
	super(attributes);
	m_id = id;
    }

    /**
     * Get the id of this rule
     * 
     * @return id of this rule
     */
    public int getId() {
	return m_id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object o) {
	return o instanceof Rule && super.equals(o);
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

}
