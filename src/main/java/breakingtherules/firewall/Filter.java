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

    public static final Filter ANY_FILTER;

    static {
	List<Attribute> attributes = new ArrayList<>();
	attributes.add(Source.ANY_SOURCE);
	attributes.add(Destination.ANY_DESTINATION);
	attributes.add(Service.ANY_SERVICE);
	ANY_FILTER = new Filter(attributes);
    }

    /**
     * Constructor
     * 
     * Creates filter based on given attributes
     * 
     * @param attributes
     *            list of wanted attributes
     */
    public Filter(List<Attribute> attributes) {
	super(attributes);
    }

    public Filter(AttributesContainer c) {
	super(c);
    }

    /**
     * Checks if hit is matching the filter
     * 
     * @param hit
     *            hit to compare to the filter
     * @return true if all hit's attribute contained in the filter, else - false
     */
    public boolean isMatch(Hit hit) {
	for (Attribute filterAttribute : this) {
	    int attributeType = filterAttribute.getTypeId();
	    Attribute hitAttribute = hit.getAttribute(attributeType);
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
    public boolean equals(Object o) {
	return super.equals(o) && o instanceof Filter;
    }

}
