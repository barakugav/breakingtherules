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
    private Attribute m_attribute;

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
    public Suggestion(Attribute att) {
	m_attribute = att;
	m_size = 0;
	m_score = 0;
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
    public Suggestion(Attribute att, int size, double score) {
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
     * Get the size of this suggestion
     */
    public int getSize() {
	return m_size;
    }

    /**
     * Get the score of this suggestion
     */
    public double getScore() {
	return m_score;
    }

    /**
     * Join this suggestion - add a new hit to it
     */
    public void join() {
	m_size++;
    }

    /**
     * Compare two Suggestions objects.
     * 
     * Is inconsistent with x.equals(y)
     */
    @Override
    public int compareTo(Suggestion other) {
	Double thisScore = new Double(this.m_score);
	Double otherScore = new Double(other.m_score);
	return thisScore.compareTo(otherScore);
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
    protected void setScore(double score) {
	m_score = score;
    }

}
