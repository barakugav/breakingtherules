package dao;

public abstract class IP {

	protected int[] m_address;

	protected int m_prefixLength;

	public IP(int[] address, int prefixLength) {
		if (address == null)
			return;
		if (address.length != getNumberOfBlocks())
			return;
		if (prefixLength < 0 || prefixLength > getMaxLength())
			return;

		for (long blockValue : address) {
			if (blockValue < 0)
				return;
			if (blockValue > getMaxBlockValue())
				return;
		}

		m_address = address;
		m_prefixLength = prefixLength;
	}

	public IP(String ip, String expectedSeperator) {

	}

	public boolean hasParent() {
		return m_prefixLength > 0;
	}

	public abstract IP getParent();

	public boolean hasChildren() {
		return m_prefixLength < getMaxLength();
	}

	public abstract IP[] getChildren();

	public boolean contain(IP other) {
		// TODO
		return false;
	}

	@Override
	public String toString() {
		String st = "" + m_address[0];
		for (int i = 1; i < m_address.length; i++)
			st += getStringSeperator() + m_address[i];
		return st;
	}

	@Override
	public int hashCode() {
		int sum = 0;
		for (int i = 0; i < m_address.length; i++)
			sum += m_address[i] << (i * Integer.BYTES / getNumberOfBlocks());
		return sum;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof IP))
			return false;

		IP other = (IP) o;
		if (this.m_prefixLength != other.m_prefixLength)
			return false;
		if (this.m_address.length != other.m_address.length)
			return false;
		for (int i = 0; i < m_address.length; i++)
			if (this.m_address[i] != other.m_address[i])
				return false;
		return true;
	}

	public static IP fromString(String ip) {
		if (ip.length() > IPv4.BLOCK_SIZE * IPv4.NUMBER_OF_BLOCKS * 2)
			return new IPv6(ip);
		else
			return new IPv4(ip);
	}

	protected abstract int getNumberOfBlocks();

	protected abstract int getBlockSize();

	protected abstract int getMaxLength();

	protected abstract String getStringSeperator();

	protected int[] getParentAdress() {
		if (!hasParent()) {
			return null;
		}

		int[] parentAdress = m_address.clone();

		/*
		 * int xorHelper = ~(1 << m_prefixLength); int blockNum = getMaxLength()
		 * * getNumberOfBlocks() / m_prefixLength;
		 * 
		 * parentAdress[blockNum] ^= xorHelper;
		 */

		return parentAdress;
	}

	private int getMaxBlockValue() {
		return 2 << getBlockSize();
	}

}
