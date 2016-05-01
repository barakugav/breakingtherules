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

    /**
     * Constructor
     * 
     * @param filter
     *            filter of the rule
     */
    public Rule(int id, Filter filter) {
	super(filter);
	m_id = id;
    }

    /**
     * 
     * Constructor
     * 
     * @param attributes
     *            attributes that represent the rule
     */
    public Rule(int id, List<Attribute> attributes) {
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
    public boolean equals(Object o) {
	return super.equals(o) && o instanceof Rule && m_id == ((Rule) o).m_id;
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
