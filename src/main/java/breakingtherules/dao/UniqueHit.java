package breakingtherules.dao;

import breakingtherules.firewall.Hit;

public class UniqueHit extends Hit {

    private final int amount;

    public UniqueHit(final Hit hit, final int amount) {
	super(hit);
	if (amount < 0)
	    throw new IllegalArgumentException("amount < 0: " + amount);
	this.amount = amount;
    }

    public int getAmount() {
	return amount;
    }

}
