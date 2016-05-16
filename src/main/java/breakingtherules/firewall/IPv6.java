package breakingtherules.firewall;

import java.util.ArrayList;
import java.util.List;

import breakingtherules.utilities.Utility;

/**
 * IP on protocol IPv6
 */
public class IPv6 extends IP {

    /**
     * IP address
     */
    private final int[] m_address;

    /**
     * Cache for the IP's hash
     */
    private int hash;

    public static final int BLOCK_NUMBER = 1 << 3; // 8
    public static final int BLOCK_SIZE = 1 << 4; // 16
    public static final int MAX_BLOCK_VALUE = (1 << BLOCK_SIZE) - 1; // 65536
    public static final int MAX_LENGTH = BLOCK_NUMBER * BLOCK_SIZE; // 128
    public static final char BLOCKS_SEPARATOR = ':';

    private static final int ADDRESS_ARRAY_SIZE = MAX_LENGTH / Integer.SIZE; // 4
    private static final int BLOCK_MASK = (1 << BLOCK_SIZE) - 1; // 65536
    private static final int OFFSET_IN_BLOCK_MASK = (1 << 6) - 1; // 63

    private IPv6(final int[] address, final int prefixLength) {
	super(prefixLength);
	m_address = address;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getParent()
     */
    @Override
    public IPv6 getParent() {
	final int p = m_prefixLength;
	if (p == 0) {
	    throw new IllegalStateException("no parent");
	}

	final int[] parentAddress = m_address.clone();
	final int blockNum = (p - 1) / Integer.SIZE;
	final int mask = ~(1 << (Integer.SIZE - (p & OFFSET_IN_BLOCK_MASK)));
	parentAddress[blockNum] &= mask;

	return new IPv6(parentAddress, p - 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getChildren()
     */
    @Override
    public IPv6[] getChildren() {
	final int p = m_prefixLength + 1;
	if (p > MAX_LENGTH) {
	    throw new IllegalStateException("no children");
	}

	// Set helper variable
	int[][] childrenAddresses = new int[][] { m_address.clone(), m_address.clone() };
	final int helper = 1 << (Integer.SIZE - (p & OFFSET_IN_BLOCK_MASK));
	final int blockNum = m_prefixLength * BLOCK_NUMBER / MAX_LENGTH;
	// TODO - this code probably contains bugs, but is not used yet.
	childrenAddresses[0][blockNum] &= ~helper;
	childrenAddresses[1][blockNum] |= helper;

	IPv6[] children = new IPv6[2];
	children[0] = new IPv6(childrenAddresses[0], p);
	children[1] = new IPv6(childrenAddresses[1], p);

	return children;
    }

    @Override
    public String toString() {
	final int p = m_prefixLength;
	if (p == 0) {
	    return ANY;
	}

	final int[] a = getAddress();
	final StringBuilder builder = new StringBuilder(Integer.toString(a[0]));
	for (int i = 1; i < a.length; i++) {
	    builder.append(BLOCKS_SEPARATOR);
	    builder.append(a[i]);
	}
	final int prefix = m_prefixLength;
	if (prefix != MAX_LENGTH) {
	    builder.append("/");
	    builder.append(prefix);
	}
	return builder.toString();
    }

    @Override
    public boolean isBrother(final IP other) {
	if (!(other instanceof IPv6)) {
	    return false;
	}
	final IPv6 o = (IPv6) other;

	final int p = m_prefixLength, op = o.m_prefixLength;
	if (p != op) {
	    return false; // Different sub network sizes
	}
	if (p == 0) {
	    return true; // Both are biggest sub network
	}

	final int[] aAddress = m_address, bAddress = o.m_address;
	final int lastEqualBlock = (p - 1) / Integer.SIZE;
	for (int blockNum = 0; blockNum < lastEqualBlock; blockNum++) {
	    if (aAddress[blockNum] != bAddress[blockNum]) {
		return false;
	    }
	}
	final int shiftSize = Integer.SIZE - ((p - 1) & OFFSET_IN_BLOCK_MASK);
	return (aAddress[lastEqualBlock] >> shiftSize) == (bAddress[lastEqualBlock] >> shiftSize);
    }

    @Override
    public boolean getBit(final int bitNumber) {
	if (bitNumber < 0 || bitNumber > MAX_LENGTH) {
	    throw new IllegalArgumentException("Bit number should be in range [0, " + MAX_LENGTH + "]");
	}
	final int blockNum = bitNumber == 0 ? 0 : (bitNumber - 1) / Integer.SIZE;
	final int bitNumInBlock = bitNumber - blockNum * Integer.SIZE;
	return (m_address[blockNum] & (1 << (Integer.SIZE - bitNumInBlock))) != 0;
    }

    @Override
    public boolean getLastBit() {
	return (m_address[ADDRESS_ARRAY_SIZE - 1] & 1) != 0;
    }

    @Override
    public int compareTo(final IP other) {
	if (other == null) {
	    return -1;
	}
	// Assume AnyIP < IPv4 < IPv6
	if (!(other instanceof IPv6)) {
	    return 1;
	}
	final IPv6 o = (IPv6) other;

	final int[] thisAddress = m_address;
	final int[] otherAddress = o.m_address;
	for (int i = 0; i < thisAddress.length; i++) {
	    final int diff = thisAddress[i] - otherAddress[i];
	    if (diff != 0) {
		return diff;
	    }
	}

	return m_prefixLength - other.m_prefixLength;
    }

    @Override
    public boolean equals(final Object o) {
	if (o == this) {
	    return true;
	} else if (!(o instanceof IPv6)) {
	    return false;
	}

	final IPv6 other = (IPv6) o;
	final int[] thisAddress = m_address;
	final int[] otherAddress = other.m_address;
	if (m_prefixLength != other.m_prefixLength) {
	    return false;
	} else if (thisAddress.length != otherAddress.length) {
	    return false;
	}
	for (int i = 0; i < thisAddress.length; i++) {
	    if (thisAddress[i] != otherAddress[i]) {
		return false;
	    }
	}
	return true;
    }

    @Override
    public int hashCode() {
	// Look for cached hash first
	int h = hash;
	final int[] a = m_address;
	if (h != 0 || a.length == 0) {
	    return h;
	}

	h = 1;
	for (int i = a.length; i-- > 0;) {
	    h = h * 31 + a[i];
	}
	return hash = h;
    }

    @Override
    public boolean contains(final IP other) {
	if (!(other instanceof IPv6)) {
	    return false;
	}
	final IPv6 o = (IPv6) other;

	final int p = m_prefixLength;
	if (p > other.m_prefixLength) {
	    return false;
	}
	if (p == 0) {
	    return true;
	}

	int blockNum;
	for (blockNum = 0; blockNum < p / Integer.SIZE; blockNum++) {
	    if (m_address[blockNum] != o.m_address[blockNum]) {
		return false;
	    }
	}
	if (p == MAX_LENGTH) {
	    return true;
	}

	return ((m_address[blockNum] ^ o.m_address[blockNum])
		& ~((1 << (Integer.SIZE - (p & OFFSET_IN_BLOCK_MASK))) - 1)) == 0;
    }

    @Override
    public int[] getAddress() {
	final int[] address = m_address;
	final int[] a = new int[BLOCK_NUMBER];
	for (int i = ADDRESS_ARRAY_SIZE; i-- > 0;) {
	    final int value = address[i];
	    a[i << 1] = (value >> BLOCK_SIZE) & BLOCK_MASK;
	    a[(i << 1) + 1] = value & BLOCK_MASK;
	}
	return a;
    }

    @Override
    public int getSubnetBitsNum() {
	return MAX_LENGTH - m_prefixLength;
    }

    @Override
    public boolean hasChildren() {
	return m_prefixLength < MAX_LENGTH;
    }

    public static IPv6 create(int[] address) {
	return create(address, MAX_LENGTH);
    }

    public static IPv6 create(int[] address, int prefixLength) {
	if (address.length != BLOCK_NUMBER) {
	    throw new IllegalArgumentException("IPv6 length: " + Utility.format(MAX_LENGTH, address.length));
	}
	if (!(0 <= prefixLength && prefixLength <= MAX_LENGTH)) {
	    throw new IllegalArgumentException("IPv6 prefix legnth: " + Utility.format(0, MAX_LENGTH, prefixLength));
	}
	final int[] a = new int[ADDRESS_ARRAY_SIZE];
	for (int blockNum = BLOCK_NUMBER; blockNum-- > 0;) {
	    int blockValue = address[blockNum];
	    if (!(0 <= blockValue && blockValue <= MAX_BLOCK_VALUE)) {
		throw new IllegalArgumentException(
			"IPv6 block value: " + Utility.format(0, MAX_BLOCK_VALUE, blockValue));
	    }
	    if ((blockNum & 1) == 0) {
		blockValue <<= 16;
	    }
	    a[blockNum >> 1] |= blockValue;
	}

	// Reset suffix
	if (prefixLength != MAX_LENGTH) {
	    for (int blockNum = ADDRESS_ARRAY_SIZE; blockNum-- > 0 && prefixLength < ((blockNum + 1) << 5);) {
		a[blockNum] &= prefixLength <= (blockNum << 5) ? 0
			: ~((1 << (Integer.SIZE - (prefixLength & OFFSET_IN_BLOCK_MASK))) - 1);
	    }
	}
	return new IPv6(a, prefixLength);
    }

    public static IPv6 create(String ip) {
	int prefix;

	List<Integer> address = new ArrayList<>();
	try {
	    int separatorIndex = ip.indexOf(BLOCKS_SEPARATOR);

	    // Read address blocks
	    while (separatorIndex >= 0) {
		String stNum = ip.substring(0, separatorIndex);
		int intNum = Integer.parseInt(stNum);
		address.add(intNum);
		ip = ip.substring(separatorIndex + 1);
		separatorIndex = ip.indexOf(BLOCKS_SEPARATOR);
	    }

	    // Read suffix of IP - last block
	    separatorIndex = ip.indexOf('/');
	    if (separatorIndex < 0) {
		// No const prefix specification
		address.add(Integer.parseInt(ip));
		prefix = MAX_LENGTH;
	    } else {
		// Has const prefix specification
		String stNum = ip.substring(0, separatorIndex);
		ip = ip.substring(separatorIndex + 1);

		int intNum = Integer.parseInt(stNum);
		address.add(intNum);

		// Read const prefix length
		if (ip.length() > 0) {
		    prefix = Integer.parseInt(ip);
		    if (prefix < 0) {
			throw new IllegalArgumentException("Negative prefix length");
		    } else if (prefix > MAX_LENGTH) {
			throw new IllegalArgumentException("Prefix length over max length");
		    }
		} else {
		    prefix = MAX_LENGTH;
		}
	    }
	} catch (NumberFormatException e) {
	    throw new IllegalArgumentException("Integer parse failed: " + e.getMessage());
	}

	final int numberOfBlocks = BLOCK_NUMBER;
	if (address.size() != numberOfBlocks) {
	    throw new IllegalArgumentException(
		    "Number of blocks is " + address.size() + " instead of " + numberOfBlocks);
	} else {
	    for (int blockValue : address) {
		if (blockValue < 0 || blockValue > MAX_BLOCK_VALUE) {
		    throw new IllegalArgumentException("IP address block isn't in range: " + blockValue
			    + ". Should be in range [0, " + MAX_BLOCK_VALUE + "]");
		}
	    }
	}

	// Copy blocks values to m_address
	int[] addressTemp = new int[numberOfBlocks];
	for (int i = 0; i < address.size(); i++) {
	    addressTemp[i] = address.get(i);
	}

	final int[] a = new int[ADDRESS_ARRAY_SIZE];
	for (int blockNum = 0; blockNum < BLOCK_NUMBER; blockNum++) {
	    int blockValue = addressTemp[blockNum];
	    if (!(0 <= blockValue && blockValue <= MAX_BLOCK_VALUE)) {
		throw new IllegalArgumentException(
			"IPv6 block value: " + Utility.format(0, MAX_BLOCK_VALUE, blockValue));
	    }
	    blockValue <<= (blockNum & 1) == 0 ? 16 : 0;
	    a[blockNum >> 1] |= blockValue;
	}

	// Reset prefix
	if (prefix != MAX_LENGTH) {
	    for (int blockNum = ADDRESS_ARRAY_SIZE; blockNum-- > 0 && prefix < ((blockNum + 1) << 5);) {
		a[blockNum] &= prefix <= (blockNum << 5) ? 0 : ~((1 << (Integer.SIZE - (prefix & OFFSET_IN_BLOCK_MASK))) - 1);
	    }
	}

	return new IPv6(a, prefix);
    }

    public static IPv6 create(boolean[] ip) {
	if (ip.length != MAX_LENGTH) {
	    throw new IllegalArgumentException("IPv6 length: " + Utility.format(MAX_LENGTH, ip.length));
	}
	int index = 0;
	final int[] address = new int[ADDRESS_ARRAY_SIZE];
	for (int blockNum = 0; blockNum < BLOCK_NUMBER; blockNum++) {
	    int blockValue = 0;
	    for (int bitNum = 0; bitNum < BLOCK_SIZE; bitNum++) {
		boolean bitValue = ip[index++];
		blockValue <<= 1;
		blockValue += bitValue ? 1 : 0;
	    }
	    blockValue <<= (blockNum & 1) == 0 ? 16 : 0;
	    address[blockNum >> 1] |= blockValue;
	}
	return new IPv6(address, MAX_LENGTH);
    }

}
