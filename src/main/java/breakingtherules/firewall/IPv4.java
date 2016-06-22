package breakingtherules.firewall;

import java.util.List;
import java.util.function.Function;

import breakingtherules.utilities.Cache;
import breakingtherules.utilities.Caches;
import breakingtherules.utilities.SoftHashCache;
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
     * String representation of any IPv4.
     */
    private static final String ANY_IPv4_STR = "AnyIPv4";

    /**
     * 'Any' IPv4, contains all others,
     */
    private static final IPv4 ANY_IPv4 = new IPv4(0, 0);

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

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSubnetBitsNum() {
	return SIZE - m_maskSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPv4 getParent() {
	final int m = m_maskSize;
	if (m <= 1) {
	    if (m == 1) {
		return ANY_IPv4;
	    }
	    throw new IllegalStateException("No parent");
	}
	final int mask = ~(1 << (SIZE - m));
	return new IPv4(m_address & mask, m - 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasChildren() {
	return m_maskSize < SIZE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPv4[] getChildren() {
	final int m = m_maskSize + 1;
	if (m > SIZE) {
	    throw new IllegalStateException("No children");
	}
	final int a = m_address;
	final int mask = 1 << (SIZE - m);
	if (m == SIZE) {
	    return new IPv4[] { valueOfFullIPv4Internal(a & ~mask), valueOfFullIPv4Internal(a | mask) };
	}
	return new IPv4[] { new IPv4(a & ~mask, m), new IPv4(a | mask, m) };
    }

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getBit(final int bitNumber) {
	if (bitNumber < 0 || bitNumber > SIZE) {
	    throw new IndexOutOfBoundsException("Bit number should be in range [0, " + SIZE + "]");
	}
	return (m_address & (1 << SIZE - bitNumber)) != 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getLastBit() {
	return (m_address & (1 << (SIZE - m_maskSize))) != 0;
    }

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSize() {
	return SIZE;
    }

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
	return m_maskSize ^ m_address;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
	final int m = m_maskSize;
	if (m == 0) {
	    return ANY_IPv4_STR;
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

    /**
     * {@inheritDoc}
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
     * Parses IPv4 from boolean bits list.
     * 
     * @param addressBits
     *            the bits list
     * @return IPv4 with address built from the bits.
     * @throws NullPointerException
     *             if the bits list is null, or one of the Boolean objects in
     *             the list is null.
     * @throws IllegalArgumentException
     *             if number of bits is unequal to {@value #SIZE}.
     */
    public static IPv4 parseIPv4FromBits(final List<Boolean> addressBits) {

	// TODO- remove this method

	if (addressBits.size() != SIZE) {
	    throw new IllegalArgumentException("IPv4 size: " + Utility.formatEqual(SIZE, addressBits.size()));
	}
	int address = 0;
	for (Boolean bit : addressBits) {
	    address <<= 1;
	    if (bit.booleanValue()) {
		address += 1;
	    }
	}
	return valueOfFullIPv4Internal(address);
    }

    /**
     * Get IPv4 object parsed from string.
     * <p>
     * The expected format is: A.B.C.D or A.B.C.D/M when A, B, C, and D are the
     * blocks values (in range 0 to 255) and M is the subnetwork mask size (in
     * range 0 to 32).
     * <p>
     * 
     * @param s
     *            string representation of an IPv4.
     * @return IPv4 object parsed from string
     * @throws NullPointerException
     *             if the string is null
     * @throws IllegalArgumentException
     *             if the format is illegal or the values are out of range.
     */
    public static IPv4 valueOf(final String s) {
	if (s.equals(ANY_IPv4_STR)) {
	    return ANY_IPv4;
	}

	int address = 0;
	int maskSize;

	int fromIndex = 0;
	int separatorIndex = s.indexOf(BLOCKS_SEPARATOR);
	int numberOfSeparators = 0;
	while (separatorIndex >= 0) {
	    if (fromIndex == separatorIndex) {
		throw new IllegalArgumentException("Empty block. " + s);
	    }
	    final int blockVal = parseBlockValue(s, fromIndex, separatorIndex);
	    address = (address << BLOCK_SIZE) + blockVal;
	    numberOfSeparators++;
	    fromIndex = separatorIndex + 1;
	    separatorIndex = s.indexOf(BLOCKS_SEPARATOR, fromIndex);
	}
	if (numberOfSeparators != BLOCK_NUMBER - 1) {
	    throw new IllegalArgumentException(
		    "IPv4 blocks number: " + Utility.formatEqual(BLOCK_NUMBER, numberOfSeparators + 1));
	}

	// Read suffix of IP - last block
	final int maskSeparatorIndex = s.indexOf(MASK_SIZE_SEPARATOR, fromIndex);
	if (maskSeparatorIndex < 0) {
	    // No mask size specification
	    final int lastBlockVal = parseBlockValue(s, fromIndex, s.length());
	    address = (address << BLOCK_SIZE) + lastBlockVal;
	    return valueOfFullIPv4Internal(address);
	}

	// Has mask size specification
	final int lastBlockVal = parseBlockValue(s, fromIndex, maskSeparatorIndex);
	address = (address << BLOCK_SIZE) + lastBlockVal;

	// Read subnetwork mask
	try {
	    maskSize = Integer.parseInt(s.substring(maskSeparatorIndex + 1, s.length()));
	} catch (NumberFormatException e) {
	    throw new IllegalArgumentException("IPv4 subnetwork mask", e);
	}

	if (!(0 < maskSize && maskSize < SIZE)) {
	    if (maskSize == SIZE) {
		return valueOfFullIPv4Internal(address);
	    }
	    if (maskSize == 0) {
		return ANY_IPv4;
	    }
	    throw new IllegalArgumentException("IPv4 subnetwork mask: " + Utility.formatRange(0, SIZE, maskSize));
	}

	// Reset suffix
	address &= maskSize != 0 ? ~((1 << (SIZE - maskSize)) - 1) : 0;

	return new IPv4(address, maskSize);
    }

    /**
     * Get IPv4 object with the specified address.
     * <p>
     * The input address is an array of the address' blocks values. The array
     * should be one with of length as IPv4 number of blocks(
     * {@value #BLOCK_NUMBER}).
     * <p>
     * For example:<br>
     * To create the IPv4 12.78.94.65 the array should be [12, 78, 94, 65].<br>
     * 
     * @param address
     *            the address blocks
     * @return IPv4 object with the desire address
     * @throws NullPointerException
     *             if the address is null
     * @throws IllegalArgumentException
     *             if the blocks values are out of range (0 to 255)
     */
    public static IPv4 valueOf(final int[] address) {
	return valueOf(address, SIZE);
    }

    /**
     * Get IPv4 object with the specified address and subnetwork mask size.
     * <p>
     * The input address is an array of the address' blocks values. The array
     * should be one with of length as IPv4 number of blocks(
     * {@value #BLOCK_NUMBER}).
     * <p>
     * For example:<br>
     * To create the IPv4 12.78.94.64/26 the array should be [12, 78, 94, 64]
     * and the maskSize should be 16.<br>
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
    public static IPv4 valueOf(final int[] address, final int maskSize) {
	if (address.length != BLOCK_NUMBER) {
	    throw new IllegalArgumentException(
		    "IPv4 blocks number: " + Utility.formatEqual(BLOCK_NUMBER, address.length));
	}

	int a = 0;
	for (int i = 0; i < address.length; i++) {
	    a = (a << BLOCK_SIZE) + address[i];
	}

	if (!(0 < maskSize && maskSize < SIZE)) {
	    if (maskSize == SIZE) {
		return valueOfFullIPv4Internal(a);
	    }
	    if (maskSize == 0) {
		return ANY_IPv4;
	    }
	    throw new IllegalArgumentException("IPv4 subnetwork mask: " + Utility.formatRange(0, SIZE, maskSize));
	}

	// Reset suffix
	a &= maskSize != 0 ? ~((1 << (SIZE - maskSize)) - 1) : 0;

	return new IPv4(a, maskSize);
    }

    /**
     * Get IPv4 object with the specified 32 bits(int) address.
     * 
     * @param addressBits
     *            the 32 bits of the address
     * @return IPv4 object with specified address
     */
    public static IPv4 valueOfBits(final int addressBits) {
	return valueOfBits(addressBits, SIZE);
    }

    /**
     * Get IPv4 object with the specified 32 bits(int) address and subnetwork
     * mask size.
     * 
     * @param addressBits
     *            the 32 bits of the address.
     * @param maskSize
     *            the size of the subnetwork mask.
     * @return IPv4 object with the specified address and maskSize.
     * @throws IllegalArgumentException
     *             if the mask size is out of range (0 to 32).
     */
    public static IPv4 valueOfBits(final int addressBits, final int maskSize) {
	if (!(0 < maskSize && maskSize < SIZE)) {
	    if (maskSize == SIZE) {
		return valueOfFullIPv4Internal(addressBits);
	    }
	    if (maskSize == 0) {
		return ANY_IPv4;
	    }
	    throw new IllegalArgumentException("IPv4 subnetwork mask: " + Utility.formatRange(0, SIZE, maskSize));
	}
	return new IPv4(addressBits, maskSize);
    }

    /**
     * Get full (maskSize = {@value #SIZE}) IPv4 object with the specified
     * address, used internally.
     * 
     * @param address
     *            the 32 bits of the address.
     * @return IPv4 object with the specified address and maskSize.
     */
    private static IPv4 valueOfFullIPv4Internal(final int address) {
	return IPv4Cache.cache.getOrAdd(new Integer(address), IPv4Cache.supplier);
    }

    /**
     * Cache of {@link IPv4} objects.
     * 
     * @author Barak Ugav
     * @author Yishai Gronich
     *
     * @see Cache
     */
    private static final class IPv4Cache {

	/**
	 * Cache of full (maskSize = {@link IPv4#SIZE}) IPv4 objects.
	 */
	static final Cache<Integer, IPv4> cache;

	/**
	 * Supplier of new IPv4 objects by integer address.
	 * <p>
	 * Used by {@link Cache#getOrAdd(Object, Function)}.
	 */
	static final Function<Integer, IPv4> supplier;

	static {
	    cache = Caches.synchronizedCache(new SoftHashCache<>());
	    supplier = address -> new IPv4(address.intValue(), SIZE);
	}

    }

    /**
     * Parse a block of IPv4 and check if it's range is valid.
     * 
     * @param str
     *            the full string.
     * @param fromIndex
     *            the start index of the value in the text.
     * @param toIndex
     *            the end index of the value in the text.
     * @return block value parsed from the specified interval on the text.
     * @throws IllegalArgumentException
     *             if failed to parse to integer or the parsed integer is not in
     *             in range of valid block value [0,
     *             {@link IPv4#MAX_BLOCK_VALUE}].
     */
    private static int parseBlockValue(final String str, final int fromIndex, final int toIndex) {
	final int blockVal;
	try {
	    blockVal = Integer.parseInt(str.substring(fromIndex, toIndex));
	} catch (NumberFormatException e) {
	    throw new IllegalArgumentException(e);
	}
	if (!(0 <= blockVal && blockVal <= MAX_BLOCK_VALUE)) {
	    throw new IllegalArgumentException(
		    "IPv4 block value: " + Utility.formatRange(0, MAX_BLOCK_VALUE, blockVal));
	}
	return blockVal;
    }

}
