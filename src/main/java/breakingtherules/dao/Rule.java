package breakingtherules.dao;

import java.util.Vector;

/**
 * Rule that apply on hits by {@link Filter}
 */
public class Rule {

    /**
     * Filter of the rule
     */
    private Filter m_filter;

    /**
     * Constructor
     * 
     * @param filter
     *            filter of the rule
     */
    public Rule(Filter filter) {
	m_filter = filter;
    }

    /**
     * 
     * Constructor
     * 
     * @param attributes
     *            attributes that represent the rule
     */
    public Rule(Vector<Attribute> attributes) {
	m_filter = new Filter(attributes);
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

}
