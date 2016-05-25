package breakingtherules.firewall;

import java.util.ArrayList;
import java.util.List;

import breakingtherules.dao.HitsDao;

/**
 * Filter of hits, base on given attributes
 * 
 * @see Hit
 * @see HitsDao
 */
public class Filter extends AttributesContainer {

    /**
     * Filter that allow any hit.
     */
    public static final Filter ANY_FILTER;

    static {
	final List<Attribute> attributes = new ArrayList<>();
	attributes.add(Source.ANY_SOURCE);
	attributes.add(Destination.ANY_DESTINATION);
	attributes.add(Service.ANY_SERVICE);
	ANY_FILTER = new Filter(attributes);
    }

    /**
     * Construct new filter from list of attributes
     * 
     * Creates filter based on given attributes
     * 
     * @param attributes
     *            list of wanted attributes
     */
    public Filter(final List<Attribute> attributes) {
	super(attributes);
    }

    /**
     * Construct new filter from other attribute container
     * 
     * @param c
     *            other container
     */
    public Filter(final AttributesContainer c) {
	super(c);
    }

    /**
     * Checks if hit is matching the filter
     * 
     * @param hit
     *            hit to compare to the filter
     * @return true if all hit's attribute contained in the filter, else - false
     */
    public boolean isMatch(final Hit hit) {
	for (final Attribute filterAttribute : this) {
	    final int attributeType = filterAttribute.getTypeId();
	    final Attribute hitAttribute = hit.getAttribute(attributeType);
	    if (!filterAttribute.contains(hitAttribute)) {
		return false;
	    }
	}
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * breakingtherules.firewall.AttributesContainer#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object o) {
	return o instanceof Filter && super.equals(o);
    }

}
