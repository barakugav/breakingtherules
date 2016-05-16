package breakingtherules.firewall;

import java.util.List;

import breakingtherules.utilities.Utility;

public class IPv4 extends IP {

    private final int m_address;

    public static final int BLOCK_NUMBER = 1 << 2; // 4
    public static final int BLOCK_SIZE = 1 << 3; // 8
    public static final int MAX_BLOCK_VALUE = (1 << BLOCK_SIZE) - 1; // 255
    public static final int MAX_LENGTH = BLOCK_NUMBER * BLOCK_SIZE; // 32
    public static final char BLOCKS_SEPARATOR = '.';

    private static final int BLOCK_MASK = (1 << BLOCK_SIZE) - 1; // 255

    private IPv4(final int address, final int prefixLength) {
	super(prefixLength);
	m_address = address;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getAddress()
     */
    @Override
    public int[] getAddress() {
	int a = m_address;
	final int[] aArr = new int[BLOCK_NUMBER];
	for (int i = BLOCK_NUMBER - 1; i >= 0; i--) {
	    int block = a & BLOCK_MASK;
	    aArr[i] = block;
	    a >>= BLOCK_SIZE;
	}
	return aArr;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getConstPrefixLength()
     */
    @Override
    public int getConstPrefixLength() {
	return m_prefixLength;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getSubnetBitsNum()
     */
    @Override
    public int getSubnetBitsNum() {
	return MAX_LENGTH - m_prefixLength;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#hasChildren()
     */
    @Override
    public boolean hasChildren() {
	return m_prefixLength < MAX_LENGTH;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getParent()
     */
    @Override
    public IPv4 getParent() {
	final int p = m_prefixLength;
	if (p == 0) {
	    throw new IllegalStateException("No parent");
	}

	final int mask = ~(1 << (MAX_LENGTH - p));
	return new IPv4(m_address & mask, p - 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getChildren()
     */
    @Override
    public IP[] getChildren() {
	final int p = m_prefixLength + 1;
	if (p > MAX_LENGTH) {
	    throw new IllegalStateException("No children");
	}
	final int a = m_address;
	final int mask = 1 << (MAX_LENGTH - p);
	return new IPv4[] { new IPv4(a & ~mask, p), new IPv4(a | mask, p) };
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#contains(breakingtherules.firewall.IP)
     */
    @Override
    public boolean contains(final IP other) {
	if (!(other instanceof IPv4)) {
	    return false;
	}
	final IPv4 o = (IPv4) other;

	final int p = m_prefixLength;
	return p <= o.m_prefixLength && (p == 0 || ((m_address ^ o.m_address) & ~((1 << (MAX_LENGTH - p)) - 1)) == 0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getLastBit()
     */
    @Override
    public boolean getLastBit() {
	return (m_address & (1 << (MAX_LENGTH - m_prefixLength))) != 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getBit(int)
     */
    @Override
    public boolean getBit(final int bitNumber) {
	if (bitNumber < 0 || bitNumber > MAX_LENGTH) {
	    throw new IllegalArgumentException("Bit number should be in range [0, " + MAX_LENGTH + "]");
	}
	return (m_address & (1 << MAX_LENGTH - bitNumber)) != 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#isBrother(breakingtherules.firewall.IP)
     */
    @Override
    public boolean isBrother(final IP other) {
	if (!(other instanceof IPv4)) {
	    return false;
	}
	final IPv4 o = (IPv4) other;

	final int p = m_prefixLength;
	if (p != other.m_prefixLength) {
	    return false;
	}
	if (p == 0) {
	    return true;
	}

	final int shiftSize = MAX_LENGTH - p + 1;
	return (m_address >> shiftSize) == (o.m_address >> shiftSize);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object o) {
	if (o == this) {
	    return true;
	} else if (!(o instanceof IPv4)) {
	    return false;
	}

	final IPv4 other = (IPv4) o;
	return m_prefixLength == other.m_prefixLength && m_address == other.m_address;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	return m_prefixLength ^ m_address;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	final int p = m_prefixLength;
	if (p == 0) {
	    return ANY;
	}

	final int[] aArr = getAddress();
	final StringBuilder st = new StringBuilder();
	for (int i = 0; i < aArr.length - 1; i++) {
	    st.append(aArr[i]);
	    st.append(BLOCKS_SEPARATOR);
	}
	st.append(aArr[aArr.length - 1]);

	if (p != MAX_LENGTH) {
	    st.append(PREFIX_LENGTH_SEPARATOR);
	    st.append(p);
	}

	return st.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final IP other) {
	if (other == null) {
	    return -1;
	}
	// Assume AnyIP < IPv4 < IPv6
	if (!(other instanceof IPv4)) {
	    return other instanceof IPv6 ? -1 : 1;
	}

	final IPv4 o = (IPv4) other;
	final int diff = m_address - o.m_address;
	return diff != 0 ? diff : m_prefixLength - other.m_prefixLength;
    }

    public static IPv4 create(final String ip) {
	int address = 0;
	int prefix;

	List<String> blocks = Utility.breakToWords(ip, "" + BLOCKS_SEPARATOR);
	if (blocks.size() != BLOCK_NUMBER) {
	    throw new IllegalArgumentException("IPv4 blocks number: " + Utility.format(BLOCK_NUMBER, blocks.size()));
	}
	for (int blockNum = 0; blockNum < blocks.size() - 1; blockNum++) {
	    final int blockVal;
	    try {
		blockVal = Integer.parseInt(blocks.get(blockNum));
	    } catch (NumberFormatException e) {
		throw new IllegalArgumentException("In block number " + blockNum, e);
	    }
	    if (!(0 <= blockVal && blockVal <= MAX_BLOCK_VALUE)) {
		throw new IllegalArgumentException("IPv4 block value: " + Utility.format(0, MAX_BLOCK_VALUE, blockVal));
	    }
	    address = (address << BLOCK_SIZE) + blockVal;
	}
	String lastBlock = blocks.get(BLOCK_NUMBER - 1);

	// Read suffix of IP - last block
	int separatorIndex = lastBlock.indexOf(PREFIX_LENGTH_SEPARATOR);
	if (separatorIndex < 0) {
	    // No const prefix specification
	    final int blockVal;
	    try {
		blockVal = Integer.parseInt(lastBlock);
	    } catch (NumberFormatException e) {
		throw new IllegalArgumentException("In block last block", e);
	    }
	    if (!(0 <= blockVal && blockVal <= MAX_BLOCK_VALUE))
		throw new IllegalArgumentException("IPv4 block value: " + Utility.format(0, MAX_BLOCK_VALUE, blockVal));
	    address = (address << BLOCK_SIZE) + blockVal;
	    prefix = MAX_LENGTH;
	} else {
	    // Has const prefix specification
	    String stNum = lastBlock.substring(0, separatorIndex);
	    lastBlock = lastBlock.substring(separatorIndex + 1);

	    final int blockVal;
	    try {
		blockVal = Integer.parseInt(stNum);
	    } catch (NumberFormatException e) {
		throw new IllegalArgumentException("In block last block", e);
	    }
	    if (!(0 <= blockVal && blockVal <= MAX_BLOCK_VALUE))
		throw new IllegalArgumentException("IPv4 block value: " + Utility.format(0, MAX_BLOCK_VALUE, blockVal));
	    address = (address << BLOCK_SIZE) + blockVal;

	    // Read const prefix length
	    if (ip.length() > 0) {
		try {
		    prefix = Integer.parseInt(lastBlock);
		} catch (NumberFormatException e) {
		    throw new IllegalArgumentException("IPv4 const prefix", e);
		}
		if (!(0 <= prefix && prefix <= MAX_LENGTH)) {
		    throw new IllegalArgumentException("IPv4 prefix length: " + Utility.format(0, MAX_LENGTH, prefix));
		}
	    } else {
		prefix = MAX_LENGTH;
	    }
	}

	// Reset suffix
	address &= prefix != 0 ? ~((1 << (MAX_LENGTH - prefix)) - 1) : 0;

	return new IPv4(address, prefix);
    }

    public static IPv4 create(final int[] ip) {
	return create(ip, MAX_LENGTH);
    }

    public static IPv4 create(final int[] ip, final int prefix) {
	if (ip.length != BLOCK_NUMBER) {
	    throw new IllegalArgumentException("IPv4 blocks number: " + Utility.format(BLOCK_NUMBER, ip.length));
	}
	if (!(0 <= prefix && prefix <= MAX_LENGTH)) {
	    throw new IllegalArgumentException("IPv4 prefix length: " + Utility.format(0, MAX_LENGTH, prefix));
	}

	int address = 0;
	for (int i = 0; i < ip.length; i++) {
	    address = (address << BLOCK_SIZE) + ip[i];
	}

	// Reset suffix
	address &= prefix != 0 ? ~((1 << (MAX_LENGTH - prefix)) - 1) : 0;
	return new IPv4(address, prefix);
    }

    public static IPv4 create(final boolean[] ip) {
	if (ip.length != MAX_LENGTH) {
	    throw new IllegalArgumentException("IPv4 length: " + Utility.format(MAX_LENGTH, ip.length));
	}
	int address = 0;
	for (int bitNum = 0; bitNum < ip.length; bitNum++) {
	    address <<= 1;
	    address += ip[bitNum] ? 1 : 0;
	}
	return new IPv4(address, MAX_LENGTH);
    }

}
