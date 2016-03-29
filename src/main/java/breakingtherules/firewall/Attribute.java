package breakingtherules.firewall;

import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Attribute of a hit
 */
public interface Attribute {

    /**
     * Checks if this attribute contain another
     * 
     * @param other
     *            another attribute to compare to
     * @return true if this attribute contains other, else - false
     */
    public boolean contains(Attribute other);

    /**
     * Get the type of the attribute
     * 
     * @return the attribute's type
     */
    public String getType();

    /**
     * Get the id of the attribute's type
     * 
     * @return the attribute's type id
     */
    public int getTypeId();

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     * 
     * This method was added to this interface so JSON could read it
     */
    @JsonProperty("str")
    public String toString();

    public static final int TYPES_COUNT = 3;
    public static final int NULL_TYPE_ID = -1;

    public static final int SOURCE_TYPE_ID = 0;
    public static final int DESTINATION_TYPE_ID = 1;
    public static final int SERVICE_TYPE_ID = 2;

    public static final String DESTINATION_TYPE = "Destination";
    public static final String SOURCE_TYPE = "Source";
    public static final String SERVICE_TYPE = "Service";

    /**
     * Comparator of attributes by their type id
     */
    public static Comparator<Attribute> ATTRIBUTES_COMPARATOR = new Comparator<Attribute>() {

	@Override
	public int compare(Attribute o1, Attribute o2) {
	    return o1.getTypeId() - o2.getTypeId();
	}
    };

    /**
     * Sort attributes by their type id
     * 
     * @param attributes
     *            the attributes to sort
     */
    public static void sort(List<Attribute> attributes) {
	attributes.sort(ATTRIBUTES_COMPARATOR);
    }

    /**
     * Convert a type string to type id
     * 
     * @param typeStr
     *            the type string
     * @return the type id
     */
    public static int typeStrToTypeId(String typeStr) {
	switch (typeStr) {
	case SOURCE_TYPE:
	    return SOURCE_TYPE_ID;
	case DESTINATION_TYPE:
	    return DESTINATION_TYPE_ID;
	case SERVICE_TYPE:
	    return SERVICE_TYPE_ID;
	default:
	    return -1;
	}
    }

    /**
     * Factory method to create an attribute, of the given type, using a String
     * constructor
     * 
     * @param typeId
     *            The type of the attribute, for example
     *            Attribute.DESTINATION_TYPE
     * @param value
     *            The String representing the attribute, for example "127.0.0.1"
     * @return An attribute, of the wanted class, with the given value. If the
     *         class is unknown, returns null.
     */
    public static Attribute createFromString(int typeId, String value) {
	switch (typeId) {
	case SOURCE_TYPE_ID:
	    return new Source(value);
	case DESTINATION_TYPE_ID:
	    return new Destination(value);
	case SERVICE_TYPE_ID:
	    return new Service(value);
	default:
	    return null;
	}
    }

}
