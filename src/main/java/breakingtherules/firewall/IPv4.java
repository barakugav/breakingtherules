package breakingtherules.firewall;

import java.util.List;

import breakingtherules.utilities.Utility;
import breakingtherules.utilities.WeakCache;

public class IPv4 extends IP {

    protected final int m_address;

    public static final int BLOCK_NUMBER = 1 << 2; // 4
    public static final int BLOCK_SIZE = 1 << 3; // 8
    public static final int MAX_BLOCK_VALUE = (1 << BLOCK_SIZE) - 1; // 255
    public static final int MAX_LENGTH = BLOCK_NUMBER * BLOCK_SIZE; // 32
    public static final char BLOCKS_SEPARATOR = '.';

    private static final int BLOCK_MASK = (1 << BLOCK_SIZE) - 1; // 255

    protected IPv4(final int address, final int prefixLength) {
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

    // TODO - better name
    public int address() {
	return m_address;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getSubnetBitsNum()
     */
    @Override
    public int getSubnetBitsNum() {
	return MAX_LENGTH - prefixLength;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#hasChildren()
     */
    @Override
    public boolean hasChildren() {
	return prefixLength < MAX_LENGTH;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getParent()
     */
    @Override
    public IPv4 getParent() {
	final int p = prefixLength;
	if (p == 0) {
	    throw new IllegalStateException("No parent");
	}

	final int mask = ~(1 << (MAX_LENGTH - p));
	return createInternal(m_address & mask, p - 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getChildren()
     */
    @Override
    public IPv4[] getChildren() {
	final int p = prefixLength + 1;
	if (p > MAX_LENGTH) {
	    throw new IllegalStateException("No children");
	}
	final int a = m_address;
	final int mask = 1 << (MAX_LENGTH - p);
	return new IPv4[] { createInternal(a & ~mask, p), createInternal(a | mask, p) };
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

	final int p = prefixLength;
	return p <= o.prefixLength && (p == 0 || ((m_address ^ o.m_address) & ~((1 << (MAX_LENGTH - p)) - 1)) == 0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getLastBit()
     */
    @Override
    public boolean getLastBit() {
	return (m_address & (1 << (MAX_LENGTH - prefixLength))) != 0;
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

	final int p = prefixLength;
	if (p != other.prefixLength) {
	    return false;
	}
	if (p == 0) {
	    return true;
	}

	final int mask = ~(1 << (MAX_LENGTH - p));
	return (m_address & mask) == (o.m_address & mask);
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getMaxLength()
     */
    @Override
    public int getMaxLength() {
	return MAX_LENGTH;
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
	return prefixLength == other.prefixLength && m_address == other.m_address;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	return prefixLength ^ m_address;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	final int p = prefixLength;
	if (p == 0) {
	    return ANY;
	}

	final int[] a = getAddress();
	final StringBuilder st = new StringBuilder();
	for (int i = 0; i < a.length - 1; i++) {
	    st.append(a[i]);
	    st.append(BLOCKS_SEPARATOR);
	}
	st.append(a[a.length - 1]);

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
	// Assume AnyIP < IPv4 < IPv6
	if (!(other instanceof IPv4)) {
	    return other instanceof IPv6 ? -1 : 1;
	}

	final IPv4 o = (IPv4) other;
	final int a1 = m_address;
	final int a2 = o.m_address;

	// compare as unsigned
	return a1 == a2 ? prefixLength - other.prefixLength
		: ((a1 + Integer.MIN_VALUE) < (a2 + Integer.MIN_VALUE)) ? -1 : 1;
    }

    public static void refreshCache() {
	for (WeakCache<Integer, IPv4> cache : IPv4Cache.cache)
	    cache.cleanCache();
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

	return createInternal(address, prefix);
    }

    public static IPv4 create(final int... address) {
	return create(address, MAX_LENGTH);
    }

    public static IPv4 create(final int[] address, final int prefix) {
	if (address.length != BLOCK_NUMBER) {
	    throw new IllegalArgumentException("IPv4 blocks number: " + Utility.format(BLOCK_NUMBER, address.length));
	}
	if (!(0 <= prefix && prefix <= MAX_LENGTH)) {
	    throw new IllegalArgumentException("IPv4 prefix length: " + Utility.format(0, MAX_LENGTH, prefix));
	}

	int a = 0;
	for (int i = 0; i < address.length; i++) {
	    a = (a << BLOCK_SIZE) + address[i];
	}

	// Reset suffix
	a &= prefix != 0 ? ~((1 << (MAX_LENGTH - prefix)) - 1) : 0;
	return createInternal(a, prefix);
    }

    public static IPv4 create(final List<Boolean> address) {
	if (address.size() != MAX_LENGTH) {
	    throw new IllegalArgumentException("IPv4 length: " + Utility.format(MAX_LENGTH, address.size()));
	}
	int a = 0;
	for (boolean bit : address) {
	    a <<= 1;
	    if (bit) {
		a += 1;
	    }
	}
	return createInternal(a, MAX_LENGTH);
    }

    private static IPv4 createInternal(final int address, final int prefix) {
	final WeakCache<Integer, IPv4> cache = IPv4Cache.cache[prefix];

	// We don't use the static constructor Integer.valueOf(int)
	// intentionally here. The Integer.valueOf(int) method can help
	// performance in general cases because it will use the cached Integers
	// that are in range [-128, 127] and will not create new objects. The
	// chance that the address will be in that range is negligible, so we
	// prefer to use the straight up constructor to avoid unnecessary
	// (probably) range checks.
	final Integer addressInteger = new Integer(address);

	IPv4 ip = cache.get(addressInteger);
	if (ip == null) {
	    ip = new IPv4(address, prefix);
	    cache.add(addressInteger, ip);
	}
	return ip;
    }

    private static final class IPv4Cache {

	static final WeakCache<Integer, IPv4>[] cache;

	static {
	    /**
	     * Used dummy variable to suppress only warnings of cache creation
	     * and not to the whole IPv4Cache class (which is necessary if we
	     * don't use temporary variable).
	     */
	    @SuppressWarnings({ "unchecked", "unused" })
	    Object dummy = cache = new WeakCache[MAX_LENGTH + 1];

	    for (int i = cache.length; i-- != 0;)
		cache[i] = new WeakCache<>();
	}

    }

}
