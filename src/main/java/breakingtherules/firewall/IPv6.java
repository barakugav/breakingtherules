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
    private static final String STRING_SEPARATOR = ":";

    /**
     * Constructor based on String IP
     * 
     * @param ip
     *            String IP
     */
    public IPv6(String ip) {
	super(ip, STRING_SEPARATOR);
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

    @Override
    public IPv6 getParent() {
	if (!hasParent()) {
	    return null;
	}

	IPv6 newIP = new IPv6(m_address.clone(), m_prefixLength - 1);
	newIP.resetSufix();

	return newIP;
    }

    @Override
    public IPv6[] getChildren() {
	if (!hasChildren()) {
	    return null;
	}

	int[][] childrenAddresses = getChildrenAdress();

	IPv6[] children = new IPv6[2];
	children[0] = new IPv6(childrenAddresses[0], m_prefixLength + 1);
	children[1] = new IPv6(childrenAddresses[1], m_prefixLength + 1);

	return children;
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
    protected int getMaxLength() {
	return BLOCK_SIZE * NUMBER_OF_BLOCKS;
    }

    @Override
    protected String getStringSeparator() {
	return STRING_SEPARATOR;
    }

    @Override
    public boolean equals(Object o) {
	if (o == null)
	    return false;
	if (!(o instanceof IPv6))
	    return false;

	return super.equals((IP) o);
    }

}
