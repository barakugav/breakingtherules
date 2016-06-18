package breakingtherules.firewall;

import java.util.List;
import java.util.function.Function;

import breakingtherules.utilities.Cache;
import breakingtherules.utilities.Caches;
import breakingtherules.utilities.Caches.CacheSupplierPair;
import breakingtherules.utilities.SoftCache;
import breakingtherules.utilities.Utility;

/**
 * IP address that is represented by a 32 bits.
 * <p>
 * For more information, see the
 * <a href='https://en.wikipedia.org/wiki/IPv4'>wiki</a>.<p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * 
 * @see IPv6
 */
public final class IPv4 extends IP {

    /**
     * 32 bits of the address.
     */
    final int m_address;

    /**
     * Size of the IP, number of bits used to represent it's address.
     */
    public static final int SIZE = 32;

    /**
     * Number of blocks in the IP.
     */
    public static final int BLOCK_NUMBER = 4;

    /**
     * Number of bits in each block.
     * <p>
     * Should be {@link #SIZE} / {@link #BLOCK_NUMBER}.
     */
    public static final int BLOCK_SIZE = 8;

    /**
     * The string separator used to separate between blocks in the string
     * representation of the IP.
     */
    static final char BLOCKS_SEPARATOR = '.';

    /**
     * Bit mask for a block in an int.
     */
    private static final int BLOCK_MASK = 0xff; // 255

    /**
     * Maximum value for a block.
     */
    public static final int MAX_BLOCK_VALUE = 255;

    /**
     * Construct new IPv4 with the specified address and maskSize.
     * 
     * @param address
     *            32 bits of the IP's address
     * @param maskSize
     *            the size of the subnetwork mask
     */
    private IPv4(final int address, final int maskSize) {
	super(maskSize);
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
     * @see breakingtherules.firewall.IP#getAddressBits()
     */
    @Override
    public int[] getAddressBits() {
	return new int[] { m_address };
    }

    /**
     * Get the address bits in an int form.
     * <p>
     * Similar to {@link #getAddressBits()} but specific for IPv4. This method
     * is possible because IPv4 address is only 32 bits, so it can be
     * represented in a single int and doesn't require array of ints.
     * <p>
     * 
     * @return 32 bits of the address in an int form.
     */
    public int getAddressBitsInt() {
	return m_address;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getSubnetBitsNum()
     */
    @Override
    public int getSubnetBitsNum() {
	return SIZE - m_maskSize;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getParent()
     */
    @Override
    public IPv4 getParent() {
	final int m = m_maskSize;
	if (m == 0) {
	    throw new IllegalStateException("No parent");
	}
	final int mask = ~(1 << (SIZE - m));
	return createInternal(m_address & mask, m - 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#hasChildren()
     */
    @Override
    public boolean hasChildren() {
	return m_maskSize < SIZE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getChildren()
     */
    @Override
    public IPv4[] getChildren() {
	final int m = m_maskSize + 1;
	if (m > SIZE) {
	    throw new IllegalStateException("No children");
	}
	final int a = m_address;
	final int mask = 1 << (SIZE - m);
	return new IPv4[] { createInternal(a & ~mask, m), createInternal(a | mask, m) };
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
	final int m = m_maskSize;
	return m <= o.m_maskSize && (m == 0 || ((m_address ^ o.m_address) & ~((1 << (SIZE - m)) - 1)) == 0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getBit(int)
     */
    @Override
    public boolean getBit(final int bitNumber) {
	if (bitNumber < 0 || bitNumber > SIZE) {
	    throw new IndexOutOfBoundsException("Bit number should be in range [0, " + SIZE + "]");
	}
	return (m_address & (1 << SIZE - bitNumber)) != 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getLastBit()
     */
    @Override
    public boolean getLastBit() {
	return (m_address & (1 << (SIZE - m_maskSize))) != 0;
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

	final int m = m_maskSize;
	if (m != other.m_maskSize) {
	    return false;
	}
	if (m == 0) {
	    return true;
	}

	final int mask = ~(1 << (SIZE - m));
	return (m_address & mask) == (o.m_address & mask);
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getSize()
     */
    @Override
    public int getSize() {
	return SIZE;
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
	return m_address == other.m_address && m_maskSize == other.m_maskSize;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	return m_maskSize ^ m_address;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	final int m = m_maskSize;
	if (m == 0) {
	    return ANY;
	}

	final int[] a = getAddress();
	final StringBuilder st = new StringBuilder();
	for (int i = 0; i < a.length - 1; i++) {
	    st.append(a[i]);
	    st.append(BLOCKS_SEPARATOR);
	}
	st.append(a[a.length - 1]);

	if (m != SIZE) {
	    st.append(MASK_SIZE_SEPARATOR);
	    st.append(m);
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
	return a1 == a2 ? m_maskSize - other.m_maskSize
		: ((a1 + Integer.MIN_VALUE) < (a2 + Integer.MIN_VALUE)) ? -1 : 1;
    }

    /**
     * Create an IP from array of the address' blocks values.
     * <p>
     * The address array should be at the length as IPv4 number of blocks(4).
     * <p>
     * For example:<br>
     * To create the IP 12.78.94.65 the array should be [12, 78, 94, 65].<br>
     * 
     * @param address
     *            the address blocks
     * @return IPv4 object with the desire address
     * @throws NullPointerException
     *             if the address is null
     * @throws IllegalArgumentException
     *             if the blocks values are out of range (0 to 255)
     */
    public static IPv4 create(final int[] address) {
	return create(address, SIZE);
    }

    /**
     * Create an IP from array of the address' blocks values.
     * <p>
     * The address array should be at the length as IPv4 number of blocks(4).
     * <p>
     * For example:<br>
     * To create the IP 12.78.94.64/26 the array should be [12, 78, 94, 64] and
     * the maskSize should be 16.<br>
     * 
     * @param address
     *            the address blocks
     * @param maskSize
     *            the size of the subnetwork mask
     * @return IPv4 object with the desire address and maskSize
     * @throws NullPointerException
     *             if the address is null
     * @throws IllegalArgumentException
     *             if the blocks values are out of range (0 to 255) or the
     *             maskSize is out of range (0 to 32).
     */
    public static IPv4 create(final int[] address, final int maskSize) {
	if (address.length != BLOCK_NUMBER) {
	    throw new IllegalArgumentException(
		    "IPv4 blocks number: " + Utility.formatEqual(BLOCK_NUMBER, address.length));
	}
	if (!(0 <= maskSize && maskSize <= SIZE)) {
	    throw new IllegalArgumentException("IPv4 subnetwork mask: " + Utility.formatRange(0, SIZE, maskSize));
	}

	int a = 0;
	for (int i = 0; i < address.length; i++) {
	    a = (a << BLOCK_SIZE) + address[i];
	}

	// Reset suffix
	a &= maskSize != 0 ? ~((1 << (SIZE - maskSize)) - 1) : 0;
	return createInternal(a, maskSize);
    }

    /**
     * Create an IPv4 from string.
     * <p>
     * The expected format is: A.B.C.D or A.B.C.D/M when A, B, C, and D are the
     * blocks values (in range 0 to 255) and M is the subnetwork mask size (in
     * range 0 to 32).
     * <p>
     * 
     * @param ip
     *            string of IP
     * @return IPv4 object parsed from string
     * @throws NullPointerException
     *             if the string is null
     * @throws IllegalArgumentException
     *             if the format is illegal or the values are out of range.
     */
    public static IPv4 createFromString(final String ip) {
	int address = 0;
	int maskSize;

	List<String> blocks = Utility.breakToWords(ip, String.valueOf(BLOCKS_SEPARATOR));
	if (blocks.size() != BLOCK_NUMBER) {
	    throw new IllegalArgumentException(
		    "IPv4 blocks number: " + Utility.formatEqual(BLOCK_NUMBER, blocks.size()));
	}
	for (int blockNum = 0; blockNum < blocks.size() - 1; blockNum++) {
	    final int blockVal;
	    try {
		blockVal = Integer.parseInt(blocks.get(blockNum));
	    } catch (NumberFormatException e) {
		throw new IllegalArgumentException("In block number " + blockNum, e);
	    }
	    if (!(0 <= blockVal && blockVal <= MAX_BLOCK_VALUE)) {
		throw new IllegalArgumentException(
			"IPv4 block value: " + Utility.formatRange(0, MAX_BLOCK_VALUE, blockVal));
	    }
	    address = (address << BLOCK_SIZE) + blockVal;
	}
	String lastBlock = blocks.get(BLOCK_NUMBER - 1);

	// Read suffix of IP - last block
	int separatorIndex = lastBlock.indexOf(MASK_SIZE_SEPARATOR);
	if (separatorIndex < 0) {
	    // No mask size specification
	    final int blockVal;
	    try {
		blockVal = Integer.parseInt(lastBlock);
	    } catch (NumberFormatException e) {
		throw new IllegalArgumentException("In block last block", e);
	    }
	    if (!(0 <= blockVal && blockVal <= MAX_BLOCK_VALUE))
		throw new IllegalArgumentException(
			"IPv4 block value: " + Utility.formatRange(0, MAX_BLOCK_VALUE, blockVal));
	    address = (address << BLOCK_SIZE) + blockVal;
	    maskSize = SIZE;
	} else {
	    // Has mask size specification
	    String stNum = lastBlock.substring(0, separatorIndex);
	    lastBlock = lastBlock.substring(separatorIndex + 1);

	    final int blockVal;
	    try {
		blockVal = Integer.parseInt(stNum);
	    } catch (NumberFormatException e) {
		throw new IllegalArgumentException("In block last block", e);
	    }
	    if (!(0 <= blockVal && blockVal <= MAX_BLOCK_VALUE))
		throw new IllegalArgumentException(
			"IPv4 block value: " + Utility.formatRange(0, MAX_BLOCK_VALUE, blockVal));
	    address = (address << BLOCK_SIZE) + blockVal;

	    // Read subnetwork mask
	    if (ip.length() > 0) {
		try {
		    maskSize = Integer.parseInt(lastBlock);
		} catch (NumberFormatException e) {
		    throw new IllegalArgumentException("IPv4 subnetwork mask", e);
		}
		if (!(0 <= maskSize && maskSize <= SIZE)) {
		    throw new IllegalArgumentException(
			    "IPv4 subnetwork mask: " + Utility.formatRange(0, SIZE, maskSize));
		}
	    } else {
		maskSize = SIZE;
	    }
	}

	// Reset suffix
	address &= maskSize != 0 ? ~((1 << (SIZE - maskSize)) - 1) : 0;

	return createInternal(address, maskSize);
    }

    /**
     * Create an IPv4 from 32 bits (int).
     * 
     * @param addressBits
     *            the 32 bits of the address
     * @return IPv4 object with specified address
     */
    public static IPv4 createFromBits(final int addressBits) {
	return createFromBits(addressBits, SIZE);
    }

    /**
     * Create an IPv4 from 32 bits (int) and specified subnetwork mask size.
     * 
     * @param addressBits
     *            the 32 bits of the address.
     * @param maskSize
     *            the size of the subnetwork mask.
     * @return IPv4 object with the specified address and maskSize.
     * @throws IllegalArgumentException
     *             if the mask size is out of range (0 to 32).
     */
    public static IPv4 createFromBits(final int addressBits, final int maskSize) {
	if (!(0 <= maskSize && maskSize <= SIZE)) {
	    throw new IllegalArgumentException("IPv4 subnetwork mask: " + Utility.formatRange(0, SIZE, maskSize));
	}
	return createInternal(addressBits, maskSize);
    }

    /**
     * Create an IPv4 from boolean bits list.
     * 
     * @param addressBitss
     *            the bits list
     * @return IPv4 with address built from the bits.
     * @throws NullPointerException
     *             if the bits list is null, or one of the Boolean objects in
     *             the list is null.
     * @throws IllegalArgumentException
     *             if number of bits is unequal to {@value #SIZE}.
     */
    public static IPv4 createFromBits(final List<Boolean> addressBitss) {
	if (addressBitss.size() != SIZE) {
	    throw new IllegalArgumentException("IPv4 size: " + Utility.formatEqual(SIZE, addressBitss.size()));
	}
	int address = 0;
	for (Boolean bit : addressBitss) {
	    address <<= 1;
	    if (bit.booleanValue()) {
		address += 1;
	    }
	}
	return createInternal(address, SIZE);
    }

    /**
     * Create an IPv4, used internally.
     * 
     * @param address
     *            the 32 bits of the address.
     * @param maskSize
     *            the subnetwork mask size.
     * @return IPv4 object with the specified address and maskSize.
     */
    private static IPv4 createInternal(final int address, final int maskSize) {
	// Intentionally using 'new Integer(int)' and not 'Integer.valueOf(int)'
	return IPv4Cache.caches[maskSize].getOrAdd(new Integer(address));
    }

    /**
     * Cache of {@link IPv4} objects.
     * 
     * @author Barak Ugav
     * @author Yishai Gronich
     *
     */
    private static final class IPv4Cache {

	/**
	 * All caches.
	 * <p>
	 * The caches array is of size {@value IPv4#SIZE} + 1, and in each cache
	 * 'i' the elements are the IPs with maskSize = i.
	 */
	static final CacheSupplierPair<Integer, IPv4>[] caches;

	static {
	    // Used dummy to suppress warnings
	    @SuppressWarnings({ "unchecked", "unused" })
	    Object dummy1 = caches = new CacheSupplierPair[SIZE + 1];

	    for (int i = caches.length; i-- != 0;) {
		final int maskSize = i;
		final Cache<Integer, IPv4> cache = Caches.synchronizedCache(new SoftCache<>());
		final Function<Integer, IPv4> supplier = (final Integer address) -> {
		    return new IPv4(address.intValue(), maskSize);
		};
		caches[i] = Caches.cacheSupplierPair(cache, supplier);
	    }
	}

    }

}
