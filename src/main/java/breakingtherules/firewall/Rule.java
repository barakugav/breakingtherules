package breakingtherules.firewall;

import java.util.List;

/**
 * Rule that apply on hits by the super matcher.
 * <p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * 
 */
public class Rule extends AbstractHitMatcher {

    /**
     * Construct new rule from existing filter.
     * 
     * @param filter
     *            existing filter to copy his attribute and to create rule from.
     * @throws NullPointerException
     *             if the filter is null.
     */
    public Rule(final Filter filter) {
	super(filter);
    }

    /**
     * Construct new rule from list of attributes
     * 
     * @param attributes
     *            list of attributes to construct a rule from.
     * @throws NullPointerException
     *             if the attribute list is null.
     * @throws IllegalArgumentException
     *             if the list contains two attributes of the same type.
     */
    public Rule(final List<Attribute> attributes) {
	super(attributes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
	return o instanceof Rule && super.equals(o);
    }

}
