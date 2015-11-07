package dao;

public class IPv4 extends IP {

    protected static final int BLOCK_SIZE = 8;

    protected static final int NUMBER_OF_BLOCKS = 4;

    private static final String STRING_SEPERATOR = ".";

    public IPv4(String ip) {
	super(ip, STRING_SEPERATOR);
    }
    
    public IPv4(int[] address) {
	this(address, BLOCK_SIZE * NUMBER_OF_BLOCKS);
    }

    public IPv4(int[] address, int prefixLength) {
	super(address, prefixLength);
    }

    @Override
    public IPv4 getParent() {
	if (!hasParent()) {
	    return null;
	}
	
	int[] parentAddress = super.getParentAdress();
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
    protected String getStringSeperator() {
	return STRING_SEPERATOR;
    }

}
