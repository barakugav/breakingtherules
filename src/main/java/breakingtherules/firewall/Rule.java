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
     * The number of hits that apply to this rule
     */
    private int m_volume;

    /**
     * Constructor
     * 
     * @param id
     *            The rule serial number
     * @param filter
     *            filter of the rule
     */
    public Rule(final int id, final Filter filter) {
	this(id, filter, 0);
    }

    /**
     * 
     * Constructor
     * 
     * @param id
     *            The rule serial number
     * @param attributes
     *            attributes that represent the rule
     */
    public Rule(final int id, final List<Attribute> attributes) {
	this(id, attributes, 0);
    }

    /**
     * 
     * @param id
     *            The rule serial number
     * @param filter
     *            filter of the rule
     * @param volume
     *            The number of hits that apply to this rule
     */
    public Rule(final int id, final Filter filter, final int volume) {
	super(filter);
	m_id = id;
	m_volume = volume;
    }

    /**
     * @param id
     *            The rule serial number
     * @param attributes
     *            attributes that represent the rule
     * @param volume
     *            The number of hits that apply to this rule
     */
    public Rule(final int id, final List<Attribute> attributes, final int volume) {
	super(attributes);
	m_id = id;
	m_volume = volume;
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
     * @return The number of hits that apply to this rule
     */
    public int getVolume() {
	return m_volume;
    }

    /**
     * @param vol
     *            The number of hits that apply to this rule
     */
    public void setVolume(final int vol) {
	m_volume = vol;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object o) {
	return o instanceof Rule && super.equals(o) && m_id == ((Rule) o).m_id;
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
