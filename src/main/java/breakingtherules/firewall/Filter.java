package breakingtherules.firewall;

import java.util.ArrayList;
import java.util.List;

import breakingtherules.dao.HitsDao;

/**
 * Filter of hits, base on given attributes.
 * <p>
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @see Hit
 * @see HitsDao
 */
public class Filter extends AbstractHitMatcher {

    /**
     * Filter that allow any hit.
     */
    public static final Filter ANY_FILTER = new AnyFilter();

    /**
     * Construct new filter from list of attributes
     *
     * @param attributes
     *            list of attributes
     * @throws NullPointerException
     *             if the attribute list is null
     * @throws IllegalArgumentException
     *             if the list contains two attributes of the same type
     */
    public Filter(final List<Attribute> attributes) {
	super(attributes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
	return o instanceof Filter && super.equals(o);
    }

    /**
     * Filter that allow any hit.
     *
     * @author Barak Ugav
     * @author Yishai Gronich
     *
     */
    private static class AnyFilter extends Filter {

	/**
	 * Construct new AnyFilter. Called once.
	 */
	AnyFilter() {
	    super(getAnyFilterAttributes());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object o) {
	    return o instanceof AnyFilter || super.equals(o);
	}

	/**
	 * Match all hits.
	 */
	@Override
	public boolean isMatch(final Hit hit) {
	    return true;
	}

	/**
	 * Get a list of 'Any' attributes.
	 * <p>
	 * Each attribute in the list should contains all other attributes of
	 * the same type, creating 'Any' filter.
	 *
	 * @return list of all 'Any' attributes
	 */
	private static List<Attribute> getAnyFilterAttributes() {
	    final List<Attribute> anyAttributes = new ArrayList<>();
	    anyAttributes.add(Source.ANY_SOURCE);
	    anyAttributes.add(Destination.ANY_DESTINATION);
	    anyAttributes.add(Service.ANY_SERVICE);
	    return anyAttributes;
	}

    }

}
