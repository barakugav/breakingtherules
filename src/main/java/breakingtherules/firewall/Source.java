package breakingtherules.firewall;

/**
 * Source attribute, represent a source IP of a hit
 */
public class Source extends IPAttribute {

    /**
     * Source attribute that represent 'Any' source (contains all others)
     */
    private static final Source ANY_SOURCE;

    static {
	ANY_SOURCE = new Source(IP.getAnyIP());
    }

    /**
     * Constructor
     * 
     * @param ip
     *            IP of the source
     */
    public Source(IP ip) throws IllegalArgumentException {
	super(ip);
    }

    /**
     * Constructor with String IP
     * 
     * @param ip
     *            String IP of the source
     */
    public Source(String ip) throws IllegalArgumentException {
	this(IP.fromString(ip));
    }

    /**
     * Use the <code>IPAttribute.contains</code> and a check that the other
     * attribute is a source attribute
     */
    @Override
    public boolean contains(Attribute other) {
	return other instanceof Source && super.contains(other);
    }

    /**
     * Use the <code>IPAttribute.equals</code> and a check that the other
     * attribute is a source attribute
     */
    @Override
    public boolean equals(Object o) {
	return o instanceof Source && super.equals(o);
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.Attribute#getType()
     */
    @Override
    public String getType() {
	return SOURCE_TYPE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.Attribute#getTypeId()
     */
    public int getTypeId() {
	return SOURCE_TYPE_ID;
    }

    /**
     * Get a source that represent 'Any' source instance (contains all others)
     * 
     * @return 'Any' source
     */
    public static Source getAnySource() {
	return ANY_SOURCE;
    }

}
