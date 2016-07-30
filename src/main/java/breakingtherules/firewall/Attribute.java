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
	SERVICE;

	/**
	 * The lower case form of the enum name.
	 */
	private final String lowerCaseName;

	/**
	 * Construct new AttributeType.
	 */
	AttributeType() {
	    lowerCaseName = name().toLowerCase();
	}

	/**
	 * Get the lower case form of the enum name.
	 *
	 * @return the enum's name's lower case form.
	 */
	public String lowerCaseName() {
	    return lowerCaseName;
	}

	/**
	 * Get an {@link AttributeType} with the the specified name, when
	 * comparing without case sensitivity.
	 *
	 * @param name
	 *            the name of the {@link AttributeType}.
	 * @return {@link AttributeType} with the specified name when comparing
	 *         without case sensitivity.
	 * @throws NullPointerException
	 *             if the name is null.
	 * @throws IllegalArgumentException
	 *             if there is no such {@link AttributeType} with the
	 */
	public static AttributeType valueOfIgnoreCase(final String name) {
	    if (name.equalsIgnoreCase(SOURCE.name()))
		return SOURCE;
	    if (name.equalsIgnoreCase(DESTINATION.name()))
		return DESTINATION;
	    if (name.equalsIgnoreCase(SERVICE.name()))
		return SERVICE;
	    throw new IllegalArgumentException("Unkown attribute type: " + name);
	}

    }

}
