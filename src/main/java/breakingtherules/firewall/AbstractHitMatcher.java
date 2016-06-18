package breakingtherules.firewall;

import java.util.List;

/**
 * Attribute container that match or doesn't match certain hits.
 * <p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 */
abstract class AbstractHitMatcher extends AttributesContainer {

    /**
     * Construct new HitMatcher.
     * 
     * @param attributes
     *            the attributes of this matcher.
     * @throws NullPointerException
     *             if the attribute list is null.
     * @throws IllegalArgumentException
     *             if the list contains two attributes of the same type.
     */
    AbstractHitMatcher(final List<Attribute> attributes) {
	super(attributes);
    }

    /**
     * Copy constructor.
     * 
     * @param c
     *            other container.
     * @throws NullPointerException
     *             if the other container is null.
     */
    AbstractHitMatcher(final AttributesContainer c) {
	super(c);
    }

    /**
     * Check if hit matches all this matcher attributes.
     * 
     * @param hit
     *            check hit.
     * @return true if all attributes in this matcher contains the hit, else
     *         false.
     * @throws NullPointerException
     *             if the hit is null.
     */
    public boolean isMatch(final Hit hit) {
	for (final Attribute filterAttr : m_attributes) {
	    if (filterAttr != null) {
		final Attribute hitAttr = hit.getAttribute(filterAttr.getTypeId());
		if (!filterAttr.contains(hitAttr)) {
		    return false;
		}
	    }
	}
	return true;
    }

}
