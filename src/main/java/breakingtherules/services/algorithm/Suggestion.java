package breakingtherules.services.algorithm;

import breakingtherules.firewall.Attribute;

/**
 * Suggestion of a part of rule - specific attribute
 * 
 * Hold the number of hits that match the attribute. Is comparable, but
 * comparison is inconsistent with equals.
 */
public class Suggestion implements Comparable<Suggestion> {

    /**
     * The attribute of this suggestion
     */
    private final Attribute m_attribute;

    /**
     * Size of this suggestion - the number of hits that match it
     */
    private int m_size;

    /**
     * Score of this suggestion, given by the suggestion algorithm
     */
    private double m_score;

    /**
     * Constructor
     * 
     * @param att
     *            attribute of this suggestion
     */
    public Suggestion(final Attribute att) {
	this(att, 0, 0);
    }

    /**
     * Constructor
     * 
     * @param att
     *            The attribute of this suggestion
     * @param size
     *            Size of this suggestion - the number of hits that match it
     * @param score
     *            Score of this suggestion, given by the suggestion algorithm
     */
    public Suggestion(final Attribute att, final int size, final double score) {
	m_attribute = att;
	m_size = size;
	m_score = score;
    }

    /**
     * Get the attribute of this suggestion
     * 
     * @return attribute of this suggestion
     */
    public Attribute getAttribute() {
	return m_attribute;
    }

    /**
     * @return the size of this suggestion
     */
    public int getSize() {
	return m_size;
    }

    /**
     * @return the score of this suggestion
     */
    public double getScore() {
	return m_score;
    }

    /**
     * Join this suggestion - add a new hit to it
     */
    void join(int amount) {
	m_size += amount;
    }

    /**
     * Compare two Suggestions objects.
     * 
     * Is inconsistent with x.equals(y)
     */
    @Override
    public int compareTo(final Suggestion other) {
	return Double.compare(m_score, other.m_score);
    }

    @Override
    public String toString() {
	return m_attribute.toString() + " size=" + m_size + " score:" + m_score;
    }

    /**
     * Set the score of this suggestion
     * 
     * @param score
     *            score of this suggestion
     */
    protected void setScore(final double score) {
	m_score = score;
    }

}
