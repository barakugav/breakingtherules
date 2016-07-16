package breakingtherules.firewall;

import java.util.Comparator;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Attribute interface represent an attribute of a hit or in a
 * AttributesContainer.
 * <p>
 * A string invariant the {@link AttributesContainer} assume is that all
 * attributes are <strong>immutable</strong>, so a subclass of an attribute
 * should obey this rule.
 * <p>
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @see Hit
 * @see AttributesContainer
 */
public abstract class Attribute {

    /**
     * The number of attributes type.
     */
    public static int TYPE_COUNT = AttributeType.values().length;

    /**
     * Representation of 'Any' attribute, used by subclasses.
     * <p>
     * This field is implementation detail and can be changed.
     */
    static final String ANY = "Any";

    /**
     * Comparator of attributes by their type.
     */
    public static Comparator<Attribute> ATTRIBUTES_TYPE_COMPARATOR = (o1, o2) -> o1.getType().compareTo(o2.getType());

    /**
     * Checks if this attribute contain another.
     * <p>
     *
     * @param other
     *            another attribute to compare to.
     * @return true if this attribute contains other, else - false.
     */
    public abstract boolean contains(Attribute other);

    /**
     * Get the type of the attribute.
     *
     * @return the attribute's type.
     */
    public abstract AttributeType getType();

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonProperty("str")
    public abstract String toString();

    /**
     * Type of an attribute.
     *
     * @author Barak Ugav
     * @author Yishai Gronich
     *
     * @see Attribute#getType()
     */
    public static enum AttributeType {

	/**
	 * Source type.
	 */
	SOURCE,

	/**
	 * Destination type.
	 */
	DESTINATION,

	/**
	 * Service type.
	 */
	SERVICE
    }

}
