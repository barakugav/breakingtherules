package breakingtherules.dao;

import java.util.List;

import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Hit;

public class UniqueHit extends Hit {

    private final int amount;

    public UniqueHit(final List<Attribute> attributes, final int amount) {
	super(attributes);
	if (amount < 0)
	    throw new IllegalArgumentException("amount < 0: " + amount);
	this.amount = amount;
    }

    public int getAmount() {
	return amount;
    }

}
