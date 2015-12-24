package breakingtherules.firewall;

/**
 * IP on protocol IPv6
 */
public class IPv6 extends IP {

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
    public IPv6(String ip) throws IllegalArgumentException {
	super(ip, STRING_SEPARATOR);
    }

    /**
     * Constructor of full IP address
     * 
     * @param address
     *            address of the IP
     */
    public IPv6(int[] address) throws IllegalArgumentException {
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
    public IPv6(int[] address, int prefixLength) throws IllegalArgumentException {
	super(address, prefixLength);
    }

    @Override
    public IPv6 getParent() {
	if (!hasParent()) {
	    return null;
	}

	IPv6 newIP = new IPv6(getAddress().clone(), getConstPrefixLength() - 1);
	return newIP;
    }

    @Override
    public IPv6[] getChildren() {
	if (!hasChildren()) {
	    return null;
	}

	int[][] childrenAddresses = getChildrenAdresses();

	IPv6[] children = new IPv6[2];
	children[0] = new IPv6(childrenAddresses[0], getConstPrefixLength() + 1);
	children[1] = new IPv6(childrenAddresses[1], getConstPrefixLength() + 1);

	return children;
    }

    @Override
    public boolean equals(Object o) {
	if (o == null)
	    return false;
	if (!(o instanceof IPv6))
	    return false;

	return super.equals((IP) o);
    }

    public static IPv6 createAnyIPv6() {
	return new IPv6(new int[] { 0, 0, 0, 0, 0, 0, 0, 0 }, 0);
    }

    @Override
    protected int getNumberOfBlocks() {
	return NUMBER_OF_BLOCKS;
    }

    @Override
    protected int getBlockSize() {
	return BLOCK_SIZE;
    }

    @Override
    protected String getStringSeparator() {
	return STRING_SEPARATOR;
    }

}
