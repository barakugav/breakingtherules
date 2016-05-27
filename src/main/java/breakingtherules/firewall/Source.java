package breakingtherules.firewall;

/**
 * Source attribute, represent a source IP of a hit
 */
public class Source extends IPAttribute {

    /**
     * Source attribute that represent 'Any' source (contains all others)
     */
    public static final Source ANY_SOURCE = new AnySource();

    /**
     * Constructor
     * 
     * @param ip
     *            IP of the source
     */
    public Source(final IP ip) {
	super(ip);
    }

    /**
     * Constructor with String IP
     * 
     * @param ip
     *            String IP of the source
     */
    public Source(final String ip) {
	this(IP.fromString(ip));
    }

    /**
     * Use the <code>IPAttribute.contains</code> and a check that the other
     * attribute is a source attribute
     */
    @Override
    public boolean contains(final Attribute other) {
	return other instanceof Source && super.contains(other);
    }

    /**
     * Use the <code>IPAttribute.equals</code> and a check that the other
     * attribute is a source attribute
     */
    @Override
    public boolean equals(final Object o) {
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
    @Override
    public int getTypeId() {
	return SOURCE_TYPE_ID;
    }

    @Override
    public Source createMutation(final IP ip) {
	return new Source(ip);
    }

    private static class AnySource extends Source {

	private AnySource() {
	    super(IP.ANY_IP);
	}

	@Override
	public boolean contains(final Attribute other) {
	    return other instanceof Source;
	}

	@Override
	public boolean equals(Object o) {
	    return o instanceof AnySource || super.equals(o);
	}

	@Override
	public String toString() {
	    return "Any";
	}

    }

}
