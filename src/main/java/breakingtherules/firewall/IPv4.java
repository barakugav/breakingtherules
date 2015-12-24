package breakingtherules.firewall;

/**
 * IP on protocol IPv4
 */
public class IPv4 extends IP {

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
    public IPv4(String ip) throws IllegalArgumentException {
	super(ip, STRING_SEPARATOR);
    }

    /**
     * Constructor of full IP address
     * 
     * @param address
     *            address of the IP
     */
    public IPv4(int[] address) throws IllegalArgumentException {
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
    public IPv4(int[] address, int prefixLength) throws IllegalArgumentException {
	super(address, prefixLength);
    }

    @Override
    public IPv4 getParent() {
	if (!hasParent()) {
	    return null;
	}

	IPv4 newIP = new IPv4(getAddress().clone(), getConstPrefixLength() - 1);
	return newIP;
    }

    @Override
    public IPv4[] getChildren() {
	if (!hasChildren()) {
	    return null;
	}

	int[][] childrenAddresses = getChildrenAdresses();

	IPv4[] children = new IPv4[2];
	children[0] = new IPv4(childrenAddresses[0], getConstPrefixLength() + 1);
	children[1] = new IPv4(childrenAddresses[1], getConstPrefixLength() + 1);

	return children;
    }

    @Override
    public boolean equals(Object o) {
	if (o == null)
	    return false;
	if (!(o instanceof IPv4))
	    return false;

	return super.equals((IP) o);
    }

    public static IPv4 createAnyIPv4() {
	return new IPv4(new int[] { 0, 0, 0, 0 }, 0);
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
