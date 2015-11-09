package breakingtherules.dao;

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
    private static final String STRING_SEPARATOR = ".";

    /**
     * Constructor based on String IP
     * 
     * @param ip
     *            String IP
     */
    public IPv4(String ip) {
	super(ip, STRING_SEPARATOR);
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

    @Override
    public IPv4 getParent() {
	if (!hasParent()) {
	    return null;
	}

	int[] parentAddress = getParentAdress();
	int parentPrefixLength = m_prefixLength - 1;

	return new IPv4(parentAddress, parentPrefixLength);
    }

    @Override
    public IPv4[] getChildren() {
	if (!hasChildren()) {
	    return null;
	}

	IPv4[] children = new IPv4[2];

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

}
