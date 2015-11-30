package breakingtherules.firewall;

import java.util.Vector;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Rule that apply on hits by {@link Filter}
 */
public class Rule {

    /**
     * Filter of the rule
     */
    private Filter m_filter;
    
    @JsonProperty("id")    
    private int m_id;

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
    public Rule(int id, Vector<Attribute> attributes) {
	m_id = id;
	m_filter = new Filter(attributes);
    }

    /**
     * Get the attributes of this rule
     * 
     * @return vector of this rule's attributes
     */
    public Vector<Attribute> getAttributes() {
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

}
