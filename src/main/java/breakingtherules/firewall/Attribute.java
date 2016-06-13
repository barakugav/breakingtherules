package breakingtherules.firewall;

import java.util.Comparator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Attribute interface represent an attribute of a hit or in a
 * AttributesContainer.
 * <p>
 * A string invariant the {@link AttributesContainer} assume is that all
 * attributes are <strong>immutable</strong>, so a subclass of an attribute
 * should obey this rule.
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * @see Hit
 * @see AttributesContainer
 */
public abstract class Attribute {

    /**
     * Checks if this attribute contain another.
     * <p>
     * 
     * @param other
     *            another attribute to compare to
     * @return true if this attribute contains other, else - false
     */
    public abstract boolean contains(Attribute other);

    /**
     * Get the type of the attribute
     * 
     * @return the attribute's type
     */
    public abstract String getType();

    /**
     * Get the id of the attribute's type
     * 
     * @return the attribute's type id
     */
    @JsonIgnore
    public abstract int getTypeId();

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     * 
     * This method was added to this interface so JSON could read it
     */
    @Override
    @JsonProperty("str")
    public abstract String toString();

    /**
     * Number of attributes.
     * <p>
     * Used by {@link AttributesContainer}.
     */
    public static final int TYPES_COUNT = 3;

    /**
     * Type id of unknown attribute.
     */
    public static final int UNKOWN_ATTRIBUTE_ID = -1;

    /**
     * Type id of source attribute.
     */
    public static final int SOURCE_TYPE_ID = 0;

    /**
     * Type id of destination attribute.
     */
    public static final int DESTINATION_TYPE_ID = 1;

    /**
     * Type id of service attribute.
     */
    public static final int SERVICE_TYPE_ID = 2;

    /**
     * Name of unknown attribute.
     */
    public static final String UNKOWN_ATTRIBUTE = null;

    /**
     * Name of destination attribute.
     */
    public static final String DESTINATION_TYPE = "destination";

    /**
     * Name of source attribute.
     */
    public static final String SOURCE_TYPE = "source";

    /**
     * Name of service attribute.
     */
    public static final String SERVICE_TYPE = "service";

    /**
     * Comparator of attributes by their type id
     */
    public static Comparator<Attribute> ATTRIBUTES_COMPARATOR = new Comparator<Attribute>() {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(final Attribute o1, final Attribute o2) {
	    return o1.getTypeId() - o2.getTypeId();
	}

    };

    /**
     * Convert a type string to type id
     * 
     * @param typeStr
     *            the type string
     * @return the type id
     */
    public static int typeStrToTypeId(final String typeStr) {
	switch (typeStr) {
	case SOURCE_TYPE:
	    return SOURCE_TYPE_ID;
	case DESTINATION_TYPE:
	    return DESTINATION_TYPE_ID;
	case SERVICE_TYPE:
	    return SERVICE_TYPE_ID;
	default:
	    return UNKOWN_ATTRIBUTE_ID;
	}
    }

    /**
     * Factory method to create an attribute, of the given type, using a String
     * constructor
     * 
     * @param typeId
     *            The type of the attribute, for example
     *            Attribute.DESTINATION_TYPE
     * @param str
     *            The String representing the attribute, for example "127.0.0.1"
     * @return An attribute, of the wanted class, with the given value. If the
     *         class is unknown, returns null.
     */
    public static Attribute createFromString(final int typeId, final String str) {
	switch (typeId) {
	case SOURCE_TYPE_ID:
	    return Source.create(str);
	case DESTINATION_TYPE_ID:
	    return Destination.create(str);
	case SERVICE_TYPE_ID:
	    return Service.createFromString(str);
	default:
	    throw new IllegalArgumentException("Unkown type id: " + typeId);
	}
    }

}
