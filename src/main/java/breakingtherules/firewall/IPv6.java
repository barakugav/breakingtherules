package breakingtherules.firewall;

/**
 * IP on protocol IPv6
 */
public class IPv6 extends IP {

    /**
     * IPv6 that represents 'Any' IPv6 (contains all others)
     */
    private static final IPv6 ANY_IPv6;

    static {
	ANY_IPv6 = new IPv6(new int[] { 0, 0, 0, 0, 0, 0, 0, 0 }, 0);
    }

    /**
     * Size of this IP block
     */
    protected static final int BLOCK_SIZE = 16;

    /**
     * Number of blocks in this IP
     */
    protected static final int NUMBER_OF_BLOCKS = 8;

    /**
     * String separator used when converting this IP to string
     */
    protected static final String STRING_SEPARATOR = ":";

    /**
     * Constructor based on String IP
     * 
     * @param ip
     *            String IP
     */
    public IPv6(String ip) {
	super(ip);
    }

    /**
     * Constructor based on boolean array that represent the bits in IP
     * 
     * @param ip
     *            the IP's bits
     */
    public IPv6(boolean[] ip) {
	super(ip);
    }

    /**
     * Constructor of full IP address
     * 
     * @param address
     *            address of the IP
     */
    public IPv6(int[] address) {
	this(address, BLOCK_SIZE * NUMBER_OF_BLOCKS);
    }

    /**
     * Constructor of IP with constant prefix
     * 
     * @param address
     *            address of the IP
     * @param prefixLength
     *            length of the constant prefix
     */
    public IPv6(int[] address, int prefixLength) {
	super(address, prefixLength);
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getParent()
     */
    @Override
    public IPv6 getParent() {
	if (!hasParent()) {
	    return null;
	}

	IPv6 newIP = new IPv6(m_address.clone(), m_prefixLength - 1);
	return newIP;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getChildren()
     */
    @Override
    public IPv6[] getChildren() {
	if (!hasChildren()) {
	    return null;
	}

	int[][] childrenAddresses = getChildrenAdresses();

	IPv6[] children = new IPv6[2];
	children[0] = new IPv6(childrenAddresses[0], m_prefixLength + 1);
	children[1] = new IPv6(childrenAddresses[1], m_prefixLength + 1);

	return children;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
	return super.equals(o) && o instanceof IPv6;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#clone()
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

    /**
     * Get IPv6 that represents 'Any' IPv6 (contains all others)
     * 
     * @return 'Any' IPv6
     */
    public static IPv6 getAnyIPv6() {
	return ANY_IPv6;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getNumberOfBlocks()
     */
    @Override
    protected int getNumberOfBlocks() {
	return NUMBER_OF_BLOCKS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getBlockSize()
     */
    @Override
    protected int getBlockSize() {
	return BLOCK_SIZE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getStringSeparator()
     */
    @Override
    protected String getStringSeparator() {
	return STRING_SEPARATOR;
    }

}
