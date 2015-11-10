package breakingtherules.algorithms;

import breakingtherules.firewall.Attribute;

/**
 * Suggestion of a part of rule - specific attribute
 * 
 * Hold the number of hits that match the attribute
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
     * Score of this suggestion
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
     * Join this suggestion
     */
    public void join() {
	m_size++;
    }

    /**
     * Set the score of this suggestion
     * 
     * @param score
     *            score of this suggestion
     */
    protected void setScore(double score) {
	this.m_score = score;
    }

    public int compareTo(Suggestion other) {
	return (int) (this.m_score - other.m_score);
    }

}
