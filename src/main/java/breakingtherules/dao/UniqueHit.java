package breakingtherules.dao;

import breakingtherules.firewall.Hit;

/**
 * The UniqueHit class represent a unique hit in bigger span (for example,
 * repository or collection) of hits.
 * <p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 */
public class UniqueHit extends Hit {

    /**
     * The amount the hit occurs in total.
     */
    private final int m_amount;

    /**
     * Construct new unique hit of existing hit.
     * 
     * @param hit
     *            the hit.
     * @param amount
     *            the number of time the hit occurs.
     * @throws IllegalArgumentException
     *             if {@code amount} is non positive.
     */
    public UniqueHit(final Hit hit, final int amount) {
	super(hit);
	if (amount < 0) {
	    throw new IllegalArgumentException("amount < 0: " + amount);
	}
	this.m_amount = amount;
    }

    /**
     * Get the amount this hit occurs.
     * 
     * @return this hit's number of occurs.
     */
    public int getAmount() {
	return m_amount;
    }

}
