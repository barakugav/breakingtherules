package breakingtherules.firewall;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Rule that apply on hits by {@link Filter}
 */
public class Rule {

    /**
     * Filter of the rule
     */
    private Filter m_filter;

    /**
     * Id of the rule
     */
    @JsonProperty("id")
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

}
