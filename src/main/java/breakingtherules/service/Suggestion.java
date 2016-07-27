package breakingtherules.service;

import java.util.Comparator;
import java.util.Objects;

import breakingtherules.firewall.Attribute;

/**
 * Suggestion of a part of rule - specific attribute
 * <p>
 * Hold the number of hits that match the attribute. Is comparable, but
 * comparison is inconsistent with equals.
 * <p>
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 */
public class Suggestion {

    /**
     * The attribute of this suggestion
     */
    private final Attribute m_attribute;

    /**
     * Size of this suggestion - the number of hits that match it
     */
    private final int m_size;

    /**
     * Score of this suggestion, given by the suggestion algorithm
     */
    private final double m_score;

    /**
     * Comparator of suggestions by their score.
     * <p>
     * Sore by greater to smaller.
     */
    public static final Comparator<Suggestion> SCORE_COMPARATOR_GREATER_TO_SMALLER = (s1, s2) -> Double
	    .compare(s2.m_score, s1.m_score);

    /**
     * Constructor
     *
     * @param attribute
     *            The attribute of this suggestion
     * @param size
     *            Size of this suggestion - the number of hits that match it
     * @param score
     *            Score of this suggestion, given by the suggestion algorithm.
     * @throws NullPointerException
     *             if the attribute is null.
     */
    public Suggestion(final Attribute attribute, final int size, final double score) {
	m_attribute = Objects.requireNonNull(attribute);
	m_size = size;
	m_score = score;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
	if (o == this)
	    return true;
	if (!(o instanceof Suggestion))
	    return false;

	final Suggestion other = (Suggestion) o;
	return m_attribute.equals(other.m_attribute) && m_size == other.m_size && m_score == other.m_score;
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
     * @return the score of this suggestion
     */
    public double getScore() {
	return m_score;
    }

    /**
     * @return the size of this suggestion
     */
    public int getSize() {
	return m_size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
	return m_attribute.hashCode() ^ m_size ^ Double.hashCode(m_score);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
	return m_attribute.toString() + " size=" + m_size + " score=" + m_score;
    }

}
