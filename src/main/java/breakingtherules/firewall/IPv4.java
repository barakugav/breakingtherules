package breakingtherules.firewall;

/**
 * IP on protocol IPv4
 */
public class IPv4 extends IP {

    /**
     * IPv4 that represents 'Any' IPv4 (contains all others)
     */
    private static final IPv4 ANY_IPv4;

    static {
	ANY_IPv4 = new IPv4(new int[] { 0, 0, 0, 0 }, 0);
    }

    /**
     * Size of this IP block
     */
    protected static final int BLOCK_SIZE = 8;

    /**
     * Number of blocks in this IP
     */
    protected static final int NUMBER_OF_BLOCKS = 4;

    /**
     * String separator used when converting this IP to string
     */
    protected static final String STRING_SEPARATOR = ".";

    /**
     * Constructor based on String IP
     * 
     * @param ip
     *            String IP, i.e. 127.0.0.0/8 or 127.168.0.1
     */
    public IPv4(String ip) {
	super(ip);
    }

    /**
     * Constructor based on boolean array that represent the bits in IP
     * 
     * @param ip
     *            the IP's bits
     */
    public IPv4(boolean[] ip) {
	super(ip);
    }

    /**
     * Constructor of full IP address
     * 
     * @param address
     *            address of the IP
     */
    public IPv4(int[] address) {
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
    public IPv4(int[] address, int prefixLength) {
	super(address, prefixLength);
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getParent()
     */
    @Override
    public IPv4 getParent() {
	if (!hasParent()) {
	    return null;
	}

	IPv4 newIP = new IPv4(m_address.clone(), m_prefixLength - 1);
	return newIP;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getChildren()
     */
    @Override
    public IPv4[] getChildren() {
	if (!hasChildren()) {
	    return null;
	}

	int[][] childrenAddresses = getChildrenAdresses();

	IPv4[] children = new IPv4[2];
	children[0] = new IPv4(childrenAddresses[0], m_prefixLength + 1);
	children[1] = new IPv4(childrenAddresses[1], m_prefixLength + 1);

	return children;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
	return super.equals(o) && o instanceof IPv4;
    }

    /**
     * Get IPv4 that represents 'Any' IPv4 (contains all others)
     * 
     * @return 'Any' IPv4
     */
    public static IPv4 getAnyIPv4() {
	return ANY_IPv4;
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
