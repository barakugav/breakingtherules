package breakingtherules.firewall;

import java.util.List;

/**
 * Rule that apply on hits by {@link Filter}
 */
public class Rule extends Filter {

    public Rule(final Filter filter) {
	super(filter);
    }

    public Rule(final List<Attribute> attributes) {
	super(attributes);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object o) {
	return o instanceof Rule && super.equals(o);
    }

}
