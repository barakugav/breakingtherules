package breakingtherules.services.algorithms;

import breakingtherules.firewall.Attribute;
import java.lang.Double;

/**
 * Suggestion of a part of rule - specific attribute
 * 
 * Hold the number of hits that match the attribute.
 * Is comparable, but comparison is inconsistent with equals.
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
     * Join this suggestion - add a new hit to it
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

    
    /**
     * Compare two Suggestions objects.
     * 
     * Is inconsistent with x.equals(y)
     */
    public int compareTo(Suggestion other) {
	return  (new Double(this.m_score)).compareTo(other.m_score);
    }

}
