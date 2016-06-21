package breakingtherules.services.algorithm;

import java.util.Comparator;

import breakingtherules.firewall.Attribute;

/**
 * Suggestion of a part of rule - specific attribute
 * 
 * Hold the number of hits that match the attribute. Is comparable, but
 * comparison is inconsistent with equals.
 */
public class Suggestion {

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
     * Comparator of suggestions by their score.
     * <p>
     * Sore by greater to smaller.
     */
    public static final Comparator<Suggestion> SCORE_COMPARATOR_GREATER_TO_SMALLER = (final Suggestion o1,
	    final Suggestion o2) -> {
	return Double.compare(o1.m_score, o2.m_score);
    };

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
     * {@inheritDoc}
     */
    @Override
    public String toString() {
	return m_attribute.toString() + " size=" + m_size + " score:" + m_score;
    }

    /**
     * Join this suggestion - add a new hit to it
     */
    void join() {
	m_size++;
    }

    /**
     * Set the score of this suggestion
     * 
     * @param score
     *            score of this suggestion
     */
    void setScore(final double score) {
	m_score = score;
    }

}
