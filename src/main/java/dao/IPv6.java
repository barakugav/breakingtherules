package dao;

public class IPv6 extends IP {

    protected static final int BLOCK_SIZE = 16;

    protected static final int NUMBER_OF_BLOCKS = 8;

    private static final String STRING_SEPERATOR = ":";

    public IPv6(String ip) {
	super(ip, STRING_SEPERATOR);
    }
    
    public IPv6(int[] address) {
	this(address, BLOCK_SIZE * NUMBER_OF_BLOCKS);
    }

    public IPv6(int[] address, int prefixLength) {
	super(address, prefixLength);
    }

    @Override
    public IPv6 getParent() {
	if (!hasParent()) {
	    return null;
	}
	
	int[] parentAddress = super.getParentAdress();
	int parentPrefixLength = m_prefixLength - 1;
	
	return new IPv6(parentAddress, parentPrefixLength);
    }

    @Override
    public IP[] getChildren() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public boolean equals(Object o) {
	if (o == null)
	    return false;
	if (!(o instanceof IPv6))
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
