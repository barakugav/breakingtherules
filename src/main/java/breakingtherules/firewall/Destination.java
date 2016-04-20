package breakingtherules.firewall;

/**
 * Destination attribute
 */
public class Destination extends IPAttribute {

    /**
     * Destination attribute that represent 'Any' destination (contains all
     * others)
     */
    public static final Destination ANY_DESTINATION;

    static {
	ANY_DESTINATION = new Destination(IP.getAnyIP());
    }

    /**
     * Constructor
     * 
     * @param ip
     *            IP of the destination
     */
    public Destination(IP ip) throws IllegalArgumentException {
	super(ip);
    }

    /**
     * Constructor from string IP
     * 
     * @param ip
     *            string IP of the destination
     */
    public Destination(String ip) {
	this(IP.fromString(ip));
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.Attribute#getType()
     */
    @Override
    public String getType() {
	return DESTINATION_TYPE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.Attribute#getTypeId()
     */
    public int getTypeId() {
	return DESTINATION_TYPE_ID;
    }

    /**
     * Use the <code>IPAttribute.contains</code> and a check that the other
     * attribute is a destination attribute
     */
    @Override
    public boolean contains(Attribute other) {
	return other instanceof Destination && super.contains(other);
    }

    /**
     * Use the <code>IPAttribute.equals</code> and a check that the other
     * attribute is a destination attribute
     */
    @Override
    public boolean equals(Object o) {
	return o instanceof Destination && super.equals(o);
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IPAttribute#clone()
     */
    @Override
    public Object clone() {
	try {
	    return super.clone();
	} catch (CloneNotSupportedException e) {
	    // this shouldn't happen, since we are Cloneable
	    throw new InternalError(e);
	}
    }

}
