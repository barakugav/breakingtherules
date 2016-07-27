package breakingtherules.firewall;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import breakingtherules.util.Hashs.Strategy;
import breakingtherules.util.Object2ObjectCache;
import breakingtherules.util.Object2ObjectCustomBucketHashCache;
import breakingtherules.util.Utility;

/**
 * IP address that is represented by a 128 bits.
 * <p>
 * For more information, see the
 * <a href='https://en.wikipedia.org/wiki/IPv6'>wiki</a>.
 * <p>
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @see IPv4
 */
public final class IPv6 extends IP {

    /**
     * 128 bits of the address.
     */
    final int[] m_address;

    /**
     * Size of the IP, number of bits used to represent it's address.
     */
    public static final short SIZE = 128;

    /**
     * Number of blocks in the IP.
     */
    public static final int BLOCK_NUMBER = 8;

    /**
     * Number of bits in each block.
     * <p>
     * Should be {@link #SIZE} / {@link #BLOCK_NUMBER}.
     */
    public static final int BLOCK_SIZE = 16;

    /**
     * The string separator used to separate between blocks in the string
     * representation of the IP.
     */
    static final char BLOCKS_SEPARATOR = ':';

    /**
     * Number of ints needed to represent the address.
     */
    static final int ADDRESS_ARRAY_SIZE = SIZE / Integer.SIZE; // 4

    /**
     * Bit mask for a block in an int.
     */
    private static final int BLOCK_MASK = 0xffff; // 65535

    /**
     * The mask used to calculate the offset of a bit number in a block.
     */
    private static final int MASK_OFFSET_IN_BLOCK = 0x1f; // 31

    /**
     * Maximum value for a block.
     */
    private static final int MAX_BLOCK_VALUE = 65535;

    /**
     * Max number of digits of a block value in base 10.
     */
    private static final int MAX_BLOCK_DIGITS_NUMBER = Utility.digitsCount(MAX_BLOCK_VALUE);

    /**
     * Max number of digits of a mask size in base 10.
     */
    private static final int MAX_MASK_SIZE_DIGITS_NUMBER = Utility.digitsCount(SIZE);

    /**
     * String representation of Any IPv6.
     */
    static final String ANY_IPv6_STR = "AnyIPv6";

    /**
     * 'Any' IPv6, contains all others.
     */
    static final IPv6 ANY_IPv6 = new IPv6(new int[ADDRESS_ARRAY_SIZE], (short) 0);

    /**
     * Construct new IPv6 with the specified address and maskSize.
     *
     * @param address
     *            array of size 4 of ints, representing 128 bits address.
     * @param maskSize
     *            the size of the subnetwork mask.
     */
    private IPv6(final int[] address, final short maskSize) {
	super(maskSize);
	m_address = address;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final IP other) {
	if (other == null)
	    return -1;
	// Assume AnyIP < IPv4 < IPv6
	if (!(other instanceof IPv6))
	    return 1;
	final IPv6 o = (IPv6) other;

	final int[] thisAddress = m_address;
	final int[] otherAddress = o.m_address;
	for (int i = 0; i < thisAddress.length; i++) {
	    final int a1 = thisAddress[i];
	    final int a2 = otherAddress[i];
	    // unsigned compare
	    final int diff = a1 == a2 ? 0 : a1 + Integer.MIN_VALUE < a2 + Integer.MIN_VALUE ? -1 : 1;
	    if (diff != 0)
		return diff;
	}

	return m_maskSize - other.m_maskSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final IP other) {
	if (!(other instanceof IPv6))
	    return false;
	final IPv6 o = (IPv6) other;

	final short m = m_maskSize;
	if (m > other.m_maskSize)
	    return false;
	if (m == 0)
	    return true;

	int blockNum;
	for (blockNum = 0; blockNum < m / Integer.SIZE; blockNum++)
	    if (m_address[blockNum] != o.m_address[blockNum])
		return false;
	if (m == SIZE)
	    return true;

	return ((m_address[blockNum] ^ o.m_address[blockNum])
		& ~((1 << Integer.SIZE - (m & MASK_OFFSET_IN_BLOCK)) - 1)) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
	if (o == this)
	    return true;
	if (!(o instanceof IPv6))
	    return false;

	final IPv6 other = (IPv6) o;
	if (m_maskSize != other.m_maskSize)
	    return false;
	final int[] thisAddress = m_address;
	final int[] otherAddress = other.m_address;
	for (int i = 0; i < thisAddress.length; i++)
	    if (thisAddress[i] != otherAddress[i])
		return false;
	return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] getAddress() {
	final int[] address = m_address;
	final int[] a = new int[BLOCK_NUMBER];
	for (int i = ADDRESS_ARRAY_SIZE; i-- > 0;) {
	    final int value = address[i];
	    a[i << 1] = value >> BLOCK_SIZE & BLOCK_MASK;
	    a[(i << 1) + 1] = value & BLOCK_MASK;
	}
	return a;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] getAddressBits() {
	return m_address.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getBit(final int bitNumber) {
	if (bitNumber < 0 || bitNumber > SIZE)
	    throw new IndexOutOfBoundsException("Bit number should be in range [0, " + SIZE + "]");
	final int blockNum = bitNumber == 0 ? 0 : (bitNumber - 1) / Integer.SIZE;
	final int bitNumInBlock = bitNumber - blockNum * Integer.SIZE;
	return (m_address[blockNum] & 1 << Integer.SIZE - bitNumInBlock) != 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPv6[] getChildren() {
	final short m = (short) (m_maskSize + 1);
	if (m > SIZE)
	    throw new IllegalStateException("no children");

	// Set helper variable
	final int[][] childrenAddresses = new int[][] { m_address.clone(), m_address.clone() };
	final int helper = 1 << Integer.SIZE - (m & MASK_OFFSET_IN_BLOCK);
	final int blockNum = m_maskSize * ADDRESS_ARRAY_SIZE / SIZE;
	childrenAddresses[0][blockNum] &= ~helper;
	childrenAddresses[1][blockNum] |= helper;
	return new IPv6[] { new IPv6(childrenAddresses[0], m), new IPv6(childrenAddresses[1], m) };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getLastBit() {
	final short m = m_maskSize;
	final int blockNum = m == 0 ? 0 : (m - 1) / Integer.SIZE;
	final int bitNumInBlock = m - blockNum * Integer.SIZE;
	return (m_address[blockNum] & 1 << Integer.SIZE - bitNumInBlock) != 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPv6 getParent() {
	final short m = m_maskSize;
	if (m <= 1) {
	    if (m == 1)
		return ANY_IPv6;
	    throw new IllegalStateException("no parent");
	}

	final int[] parentAddress = m_address.clone();
	final int blockNum = (m - 1) / Integer.SIZE;
	final int mask = ~(1 << Integer.SIZE - (m & MASK_OFFSET_IN_BLOCK));
	parentAddress[blockNum] &= mask;

	return new IPv6(parentAddress, (short) (m - 1));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getSize() {
	return SIZE;
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
    public boolean hasChildren() {
	return m_maskSize < SIZE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
	int h = 31 + m_maskSize;
	final int[] address = m_address;
	for (final int blockVal : address)
	    h = h * 31 + blockVal;
	return h;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBrother(final IP other) {
	if (!(other instanceof IPv6))
	    return false;
	final IPv6 o = (IPv6) other;

	final short m = m_maskSize, om = o.m_maskSize;
	if (m != om)
	    return false; // Different sub network sizes
	if (m == 0)
	    return true; // Both are biggest sub network

	final int[] aAddress = m_address, bAddress = o.m_address;
	final int lastEqualBlock = (m - 1) / Integer.SIZE;
	for (int blockNum = 0; blockNum < lastEqualBlock; blockNum++)
	    if (aAddress[blockNum] != bAddress[blockNum])
		return false;
	final int shiftSize = Integer.SIZE - (m - 1 & MASK_OFFSET_IN_BLOCK);
	return aAddress[lastEqualBlock] >> shiftSize == bAddress[lastEqualBlock] >> shiftSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
	final short m = m_maskSize;
	if (m == 0)
	    return ANY_IPv6_STR;

	final int[] a = getAddress();
	final StringBuilder builder = new StringBuilder(Integer.toString(a[0]));
	for (int i = 1; i < a.length; i++) {
	    builder.append(BLOCKS_SEPARATOR);
	    builder.append(a[i]);
	}
	if (m != SIZE) {
	    builder.append("/");
	    builder.append(m);
	}
	return builder.toString();
    }

    /**
     * Get IPv6 object parsed from boolean bits list.
     *
     * @param bitsAddress
     *            the bits list.
     * @return IPv6 with the address build from the bits.
     * @throws NullPointerException
     *             if the list is null or one of the Boolean objects in the list
     *             is null.
     * @throws IllegalArgumentException
     *             if number of bits is unequal to {@value #SIZE}.
     */
    public static IPv6 parseIPv6FromBits(final List<Boolean> bitsAddress) {
	return parseIPv6FromBits(bitsAddress, null);
    }

    /**
     * Get IPv6 object parsed from boolean bits list.
     * <p>
     * If the cache isn't null, will used the cached IPv6 from the cache if one
     * exist, or will create a new one and cache it to the cache otherwise.
     * <p>
     *
     * @param bitsAddress
     *            the bits list.
     * @param cache
     *            the cached containing cached IPv6s objects. Can be null.
     * @return IPv6 with the address build from the bits.
     * @throws NullPointerException
     *             if the list is null or one of the Boolean objects in the list
     *             is null.
     * @throws IllegalArgumentException
     *             if number of bits is unequal to {@value #SIZE}.
     */
    public static IPv6 parseIPv6FromBits(final List<Boolean> bitsAddress, final IPv6.Cache cache) {

	// TODO - remove this method.

	if (bitsAddress.size() != SIZE)
	    throw new IllegalArgumentException("IPv6 size: " + Utility.formatEqual(SIZE, bitsAddress.size()));
	final Iterator<Boolean> it = bitsAddress.iterator();
	final int[] address = new int[ADDRESS_ARRAY_SIZE];
	for (int blockNum = 0; blockNum < BLOCK_NUMBER; blockNum++) {
	    int blockValue = 0;
	    for (int bitNum = 0; bitNum < BLOCK_SIZE; bitNum++) {
		blockValue <<= 1;
		if (it.next().booleanValue())
		    blockValue += 1;
	    }
	    blockValue <<= (blockNum & 1) == 0 ? 16 : 0;
	    address[blockNum >> 1] |= blockValue;
	}
	return valueOfInternal(address, cache);
    }

    /**
     * Get IPv6 object with the specified address.
     * <p>
     * The input address is an array of the address' blocks values. The array
     * should be one with of length as IPv6 number of blocks(
     * {@value #BLOCK_NUMBER}).
     * <p>
     * For example:<br>
     * To create the IPv6 128.4.5.1.0.5789.500.21 the array should be [128, 4,
     * 5, 1, 0, 5789, 500, 21].<br>
     *
     * @param address
     *            the address blocks.
     * @return IPv6 object with the desire address.
     * @throws NullPointerException
     *             if the address is null.
     * @throws IllegalArgumentException
     *             if the blocks values are out of range (0 to 65535).
     */
    public static IPv6 valueOf(final int[] address) {
	return valueOf(address, SIZE, null);
    }

    /**
     * Get IPv6 object with the specified address.
     * <p>
     * The input address is an array of the address' blocks values. The array
     * should be one with of length as IPv6 number of blocks(
     * {@value #BLOCK_NUMBER}).
     * <p>
     * For example:<br>
     * To create the IPv6 128.4.5.1.0.5789.500.21 the array should be [128, 4,
     * 5, 1, 0, 5789, 500, 21].<br>
     * <p>
     * If the cache isn't null, will used the cached IPv6 from the cache if one
     * exist, or will create a new one and cache it to the cache otherwise.
     * <p>
     *
     * @param address
     *            the address blocks.
     * @param cache
     *            the cached containing cached IPv6s objects. Can be null.
     * @return IPv6 object with the desire address.
     * @throws NullPointerException
     *             if the address is null.
     * @throws IllegalArgumentException
     *             if the blocks values are out of range (0 to 65535).
     */
    public static IPv6 valueOf(final int[] address, final IPv6.Cache cache) {
	return valueOf(address, SIZE, cache);
    }

    /**
     * Get IPv6 object with the specified address and subnetwork mask size.
     * <p>
     * The input address is an array of the address' blocks values. The array
     * should be one with of length as IPv6 number of blocks(
     * {@value #BLOCK_NUMBER}).
     * <p>
     * For example:<br>
     * To create the IPv6 128.4.5.1.0.5789.500.0/112 the array should be [128,
     * 4, 5, 1, 0, 5789, 500, 0] and the maskSize should be 112.<br>
     *
     * @param address
     *            the address blocks.
     * @param maskSize
     *            the size of the subnetwork mask.
     * @return IPv6 object with the desire address.
     * @throws NullPointerException
     *             if the address is null.
     * @throws IllegalArgumentException
     *             if the blocks values are out of range (0 to 65535) or the
     *             maskSize is out of range (0 to 128).
     */
    public static IPv6 valueOf(final int[] address, final short maskSize) {
	return valueOf(address, maskSize, null);
    }

    /**
     * Get IPv6 object with the specified address and subnetwork mask size.
     * <p>
     * The input address is an array of the address' blocks values. The array
     * should be one with of length as IPv6 number of blocks(
     * {@value #BLOCK_NUMBER}).
     * <p>
     * For example:<br>
     * To create the IPv6 128.4.5.1.0.5789.500.0/112 the array should be [128,
     * 4, 5, 1, 0, 5789, 500, 0] and the maskSize should be 112.<br>
     * <p>
     * If the cache isn't null, will used the cached IPv6 from the cache if one
     * exist, or will create a new one and cache it to the cache otherwise.
     * <p>
     *
     * @param address
     *            the address blocks.
     * @param maskSize
     *            the size of the subnetwork mask.
     * @param cache
     *            the cached containing cached IPv6s objects. Can be null.
     * @return IPv6 object with the desire address.
     * @throws NullPointerException
     *             if the address is null.
     * @throws IllegalArgumentException
     *             if the blocks values are out of range (0 to 65535) or the
     *             maskSize is out of range (0 to 128).
     */
    public static IPv6 valueOf(final int[] address, final short maskSize, final IPv6.Cache cache) {
	if (address.length != BLOCK_NUMBER)
	    throw new IllegalArgumentException(
		    "IPv6 block number: " + Utility.formatEqual(BLOCK_NUMBER, address.length));

	final int[] a = new int[ADDRESS_ARRAY_SIZE];
	for (int blockNum = BLOCK_NUMBER; blockNum-- > 0;) {
	    int blockValue = address[blockNum];
	    if (!(0 <= blockValue && blockValue <= MAX_BLOCK_VALUE))
		throw new IllegalArgumentException(
			"IPv6 block value: " + Utility.formatRange(0, MAX_BLOCK_VALUE, blockValue));
	    if ((blockNum & 1) == 0)
		blockValue <<= 16;
	    a[blockNum >> 1] |= blockValue;
	}
	if (!(0 < maskSize && maskSize < SIZE)) {
	    if (maskSize == SIZE)
		return valueOfInternal(a, cache);
	    if (maskSize == 0)
		return ANY_IPv6;
	    throw new IllegalArgumentException("IPv6 subnetwork mask size: " + Utility.formatRange(0, SIZE, maskSize));
	}

	// Reset suffix
	for (int blockNum = ADDRESS_ARRAY_SIZE; blockNum-- > 0 && maskSize < blockNum + 1 << 5;)
	    a[blockNum] &= maskSize <= blockNum << 5 ? 0
		    : ~((1 << Integer.SIZE - (maskSize & MASK_OFFSET_IN_BLOCK)) - 1);
	return new IPv6(a, maskSize);
    }

    /**
     * Get IPv6 object parsed from string.
     * <p>
     * The expected format is: A:B:C:D:E:F:G:H or A:B:C:D:E:F:G:H/M when A, B,
     * C, D, E, F, G and H are the blocks value (in range 0 to 65535) and M is
     * the subnetwork mask size (in range 0 to 128).
     *
     * @param s
     *            string representation of IPv6.
     * @return IPv6 object parsed from string.
     * @throws NullPointerException
     *             if the string is null.
     * @throws IllegalArgumentException
     *             if the format is illegal or the values are out of range.
     */
    public static IPv6 valueOf(final String s) {
	return valueOf(s, null, s.indexOf(BLOCKS_SEPARATOR));
    }

    /**
     * Get IPv6 object parsed from string.
     * <p>
     * The expected format is: A:B:C:D:E:F:G:H or A:B:C:D:E:F:G:H/M when A, B,
     * C, D, E, F, G and H are the blocks value (in range 0 to 65535) and M is
     * the subnetwork mask size (in range 0 to 128).
     * <p>
     * If the cache isn't null, will used the cached IPv6 from the cache if one
     * exist, or will create a new one and cache it to the cache otherwise.
     * <p>
     *
     * @param s
     *            string representation of IPv6.
     * @param cache
     *            the cached containing cached IPv6s objects. Can be null.
     * @return IPv6 object parsed from string.
     * @throws NullPointerException
     *             if the string is null.
     * @throws IllegalArgumentException
     *             if the format is illegal or the values are out of range.
     */
    public static IPv6 valueOf(final String s, final IPv6.Cache cache) {
	return valueOf(s, cache, s.indexOf(BLOCKS_SEPARATOR));
    }

    /**
     * Get IPv6 object with the specified 128 bits(ints array) address.
     *
     * @param bitsAddress
     *            the 128 bits of the address.
     * @return IPv6 object with the specified address.
     * @throws NullPointerException
     *             if the address bits array is null.
     * @throws IllegalArgumentException
     *             if the size of the address array is not
     *             {@value #ADDRESS_ARRAY_SIZE}.
     */
    public static IPv6 valueOfBits(final int[] bitsAddress) {
	checkAddressBits(bitsAddress);
	// Must clone bitsAddress to be safe that the address won't be changed
	// in the future.
	return new IPv6(bitsAddress.clone(), SIZE);
    }

    /**
     * Get IPv6 object with the specified 128 bits(ints array) address.
     * <p>
     * If the cache isn't null, will used the cached IPv6 from the cache if one
     * exist, or will create a new one and cache it to the cache otherwise.
     * <p>
     *
     * @param bitsAddress
     *            the 128 bits of the address.
     * @param cache
     *            the cached containing cached IPv6s objects. Can be null.
     * @return IPv6 object with the specified address.
     * @throws NullPointerException
     *             if the address bits array is null.
     * @throws IllegalArgumentException
     *             if the size of the address array is not
     *             {@value #ADDRESS_ARRAY_SIZE}.
     */
    public static IPv6 valueOfBits(final int[] bitsAddress, final IPv6.Cache cache) {
	checkAddressBits(bitsAddress);
	// Must clone bitsAddress to be safe that the address won't be changed
	// in the future.
	return valueOfInternal(bitsAddress.clone(), cache);
    }

    /**
     * Get IPv6 object with the specified 128 bits(ints array) address and
     * subnetwork mask size.
     *
     * @param bitsAddress
     *            the 128 bits of the address.
     * @param maskSize
     *            the size of the subnetwork mask.
     * @return IPv6 object with the specified address and maskSize.
     * @throws NullPointerException
     *             if the address bits array is null.
     * @throws IllegalArgumentException
     *             if the size of the address array is not
     *             {@value #ADDRESS_ARRAY_SIZE} or if the mask size is out of
     *             range (0 to 128).
     */
    public static IPv6 valueOfBits(final int[] bitsAddress, final short maskSize) {
	return valueOfBits(bitsAddress, maskSize, null);
    }

    /**
     * Get IPv6 object with the specified 128 bits(ints array) address and
     * subnetwork mask size.
     * <p>
     * If the cache isn't null, will used the cached IPv6 from the cache if one
     * exist, or will create a new one and cache it to the cache otherwise.
     * <p>
     *
     * @param bitsAddress
     *            the 128 bits of the address.
     * @param maskSize
     *            the size of the subnetwork mask.
     * @param cache
     *            the cached containing cached IPv6s objects. Can be null.
     * @return IPv6 object with the specified address and maskSize.
     * @throws NullPointerException
     *             if the address bits array is null.
     * @throws IllegalArgumentException
     *             if the size of the address array is not
     *             {@value #ADDRESS_ARRAY_SIZE} or if the mask size is out of
     *             range (0 to 128).
     */
    public static IPv6 valueOfBits(final int[] bitsAddress, final short maskSize, final IPv6.Cache cache) {
	checkAddressBits(bitsAddress);
	// Must clone bitsAddress to be safe that the address won't be changed
	// in the future.
	final int[] address = bitsAddress.clone();
	if (!(0 < maskSize && maskSize < SIZE)) {
	    if (maskSize == SIZE)
		return valueOfInternal(address, cache);
	    if (maskSize == 0)
		return ANY_IPv6;
	    throw new IllegalArgumentException("IPv6 subnetwork mask size: " + Utility.formatRange(0, SIZE, maskSize));
	}

	return new IPv6(address, maskSize);
    }

    /**
     * Get IPv6 object parsed from string.
     * <p>
     * The expected format is: A:B:C:D:E:F:G:H or A:B:C:D:E:F:G:H/M when A, B,
     * C, D, E, F, G and H are the blocks value (in range 0 to 65535) and M is
     * the subnetwork mask size (in range 0 to 128).
     * <p>
     * If the cache isn't null, will used the cached IPv6 from the cache if one
     * exist, or will create a new one and cache it to the cache otherwise.
     * <p>
     *
     * @param s
     *            string representation of IPv6.
     * @param cache
     *            the cached containing cached IPv6s objects. Can be null.
     * @param separatorIndex
     *            the first index of the separator in the string. (optimization)
     * @return IPv6 object parsed from string.
     * @throws NullPointerException
     *             if the string is null.
     * @throws IllegalArgumentException
     *             if the format is illegal or the values are out of range.
     */
    static IPv6 valueOf(final String s, final IPv6.Cache cache, int separatorIndex) {
	final int[] address = new int[ADDRESS_ARRAY_SIZE];
	int fromIndex = 0;
	int blockNumber = 0;

	for (; separatorIndex >= 0; separatorIndex = s.indexOf(BLOCKS_SEPARATOR, fromIndex)) {
	    if (blockNumber == BLOCK_NUMBER - 1)
		throw new IllegalArgumentException("Too many IPv6 blocks, expected " + BLOCK_NUMBER);
	    int blockVal = parseBlockValue(s, fromIndex, separatorIndex);
	    if ((blockNumber & 1) == 0)
		blockVal <<= 16;
	    address[blockNumber >> 1] |= blockVal;
	    blockNumber++;
	    fromIndex = separatorIndex + 1;
	}
	if (blockNumber != BLOCK_NUMBER - 1) {
	    if (s.equals(ANY_IPv6_STR))
		return ANY_IPv6;
	    throw new IllegalArgumentException(
		    "IPv6 blocks number: " + Utility.formatEqual(BLOCK_NUMBER, blockNumber + 1));
	}

	final int maskSeparatorIndex = s.indexOf(MASK_SIZE_SEPARATOR, fromIndex);
	if (maskSeparatorIndex < 0) {
	    // No mask size specification
	    final int lastBlockVal = parseBlockValue(s, fromIndex, s.length());
	    address[ADDRESS_ARRAY_SIZE - 1] |= lastBlockVal;
	    return valueOfInternal(address, cache);
	}

	// Has mask size specification
	final int lastBlockVal = parseBlockValue(s, fromIndex, maskSeparatorIndex);
	address[ADDRESS_ARRAY_SIZE - 1] |= lastBlockVal;

	// Read subnetwork mask
	final short maskSize = (short) Utility.parsePositiveIntUncheckedOverflow(s, maskSeparatorIndex + 1, s.length(),
		MAX_MASK_SIZE_DIGITS_NUMBER);

	if (!(0 < maskSize && maskSize < SIZE)) {
	    if (maskSize == SIZE)
		return valueOfInternal(address, cache);
	    if (maskSize == 0)
		return ANY_IPv6;
	    throw new IllegalArgumentException("IPv6 subnetwork mask: " + Utility.formatRange(0, SIZE, maskSize));
	}

	// Reset by mask
	for (int blockNum = ADDRESS_ARRAY_SIZE; blockNum-- > 0 && maskSize < blockNum + 1 << 5;)
	    address[blockNum] &= maskSize <= blockNum << 5 ? 0
		    : ~((1 << Integer.SIZE - (maskSize & MASK_OFFSET_IN_BLOCK)) - 1);

	return new IPv6(address, maskSize);
    }

    /**
     * Checks if an address is in valid.
     *
     * @param address
     *            the checked address.
     * @throws IllegalArgumentException
     *             if the address is not valid.
     */
    private static void checkAddressBits(final int[] address) {
	if (address.length != ADDRESS_ARRAY_SIZE)
	    throw new IllegalArgumentException(
		    "IPv6's address bits array: " + Utility.formatEqual(ADDRESS_ARRAY_SIZE, address.length));
    }

    /**
     * Parse a block of IPv6 and check if it's range is valid.
     *
     * @param s
     *            the full string.
     * @param fromIndex
     *            the start index of the value in the text.
     * @param toIndex
     *            the end index of the value in the text.
     * @return block value parsed from the specified interval on the text.
     * @throws IllegalArgumentException
     *             if failed to parse to integer or the parsed integer is not in
     *             in range of valid block value [0,
     *             {@link IPv6#MAX_BLOCK_VALUE}].
     */
    private static int parseBlockValue(final String s, final int fromIndex, final int toIndex) {
	final int blockVal = Utility.parsePositiveIntUncheckedOverflow(s, fromIndex, toIndex, MAX_BLOCK_DIGITS_NUMBER);

	if (!(0 <= blockVal && blockVal <= MAX_BLOCK_VALUE))
	    throw new IllegalArgumentException(
		    "IPv6 block value: " + Utility.formatRange(0, MAX_BLOCK_VALUE, blockVal));
	return blockVal;
    }

    /**
     * Get IPv6 object with the specified 128 bits(int array) address. Used
     * internally.
     * <p>
     * If the cache isn't null, will used the cached IPv6 from the cache if one
     * exist, or will create a new one and cache it to the cache otherwise.
     * <p>
     *
     * @param address
     *            the IP's address.
     * @param cache
     *            the cached containing cached IPv6s objects. Can be null.
     * @return IPv6 object with the specified address.
     */
    private static IPv6 valueOfInternal(final int[] address, final IPv6.Cache cache) {
	return cache != null ? cache.cache.getOrAdd(address, IPv6.Cache.supplier) : new IPv6(address, SIZE);
    }

    /**
     * Cache of {@link IPv6} objects.
     *
     * @author Barak Ugav
     * @author Yishai Gronich
     *
     * @see Cache
     */
    public static final class Cache {

	/**
	 * Cache of full (maskSize = {@link IPv6#SIZE}) IPv6 objects.
	 */
	final Object2ObjectCache<int[], IPv6> cache;

	/**
	 * Supplier of IPv6 object by address.
	 * <p>
	 * Used by {@link Object2ObjectCache#getOrAdd(Object, Function)} .
	 */
	static final Function<int[], IPv6> supplier;

	static {
	    supplier = address -> new IPv6(address, SIZE);
	}

	/**
	 * Construct new IPv6 objects.
	 */
	public Cache() {
	    cache = new Object2ObjectCustomBucketHashCache<>(IPv6AddressesStrategy.INSTANCE);
	}

	/**
	 * Strategy of IPv4 addresses.
	 *
	 * @author Barak Ugav
	 * @author Yishai Gronich
	 *
	 * @see Object2ObjectCustomBucketHashCache
	 */
	static class IPv6AddressesStrategy implements Strategy<int[]> {

	    /**
	     * The single instance of the strategy.
	     */
	    static final IPv6AddressesStrategy INSTANCE = new IPv6AddressesStrategy();

	    /**
	     * Construct new IPv6AddressesStrategy, called once.
	     * <p>
	     *
	     * @see #INSTANCE
	     */
	    private IPv6AddressesStrategy() {
	    }

	    /**
	     * Checks if two IPv6 address are equal.
	     * <p>
	     * Assuming the addresses are not null and of the same length.
	     */
	    @Override
	    public boolean equals(final int[] a, final int[] b) {
		// Could use Arrays.equals but there are redundant null and
		// length checks.
		for (int i = a.length; i-- != 0;)
		    if (a[i] != b[i])
			return false;
		return true;
	    }

	    /**
	     * Compute hash code for IPv6 address.
	     * <p>
	     * Assuming the address is not null and of length
	     * {@value IPv6#ADDRESS_ARRAY_SIZE}.
	     */
	    @Override
	    public int hashCode(final int[] k) {
		// Could use Arrays.hashCode but there are redundant null
		// checks.
		int h = 17;
		for (int i = IPv6.ADDRESS_ARRAY_SIZE; i-- != 0;)
		    h = h * 31 + k[i];
		return h;
	    }

	}

    }

}
