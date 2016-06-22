package breakingtherules.firewall;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import breakingtherules.utilities.Cache;
import breakingtherules.utilities.Caches;
import breakingtherules.utilities.Hashs.Strategy;
import breakingtherules.utilities.SoftCustomHashCache;
import breakingtherules.utilities.Utility;

/**
 * IP address that is represented by a 128 bits.
 * <p>
 * For more information, see the
 * <a href='https://en.wikipedia.org/wiki/IPv6'>wiki</a>.<p>
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
    public static final int SIZE = 128;

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
     * Bit mask for a block in an int.
     */
    private static final int BLOCK_MASK = 0xffff; // 65535

    /**
     * Maximum value for a block.
     */
    private static final int MAX_BLOCK_VALUE = 65535;

    /**
     * Number of ints needed to represent the address.
     */
    static final int ADDRESS_ARRAY_SIZE = SIZE / Integer.SIZE; // 4

    /**
     * The mask used to calculate the offset of a bit number in a block.
     */
    private static final int MASK_OFFSET_IN_BLOCK = 0x1f; // 31

    /**
     * String representation of Any IPv6.
     */
    private static final String ANY_IPv6_STR = "AnyIPv6";

    /**
     * 'Any' IPv6, contains all others.
     */
    private static final IPv6 ANY_IPv6 = new IPv6(new int[ADDRESS_ARRAY_SIZE], 0);

    /**
     * Construct new IPv6 with the specified address and maskSize.
     * 
     * @param address
     *            array of size 4 of ints, representing 128 bits address.
     * @param maskSize
     *            the size of the subnetwork mask.
     */
    private IPv6(final int[] address, final int maskSize) {
	super(maskSize);
	m_address = address;
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
	    a[i << 1] = (value >> BLOCK_SIZE) & BLOCK_MASK;
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
    public int getSubnetBitsNum() {
	return SIZE - m_maskSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPv6 getParent() {
	final int m = m_maskSize;
	if (m <= 1) {
	    if (m == 1) {
		return ANY_IPv6;
	    }
	    throw new IllegalStateException("no parent");
	}

	final int[] parentAddress = m_address.clone();
	final int blockNum = (m - 1) / Integer.SIZE;
	final int mask = ~(1 << (Integer.SIZE - (m & MASK_OFFSET_IN_BLOCK)));
	parentAddress[blockNum] &= mask;

	return new IPv6(parentAddress, m - 1);
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
    public IPv6[] getChildren() {
	final int m = m_maskSize + 1;
	if (m > SIZE) {
	    throw new IllegalStateException("no children");
	}

	// Set helper variable
	int[][] childrenAddresses = new int[][] { m_address.clone(), m_address.clone() };
	final int helper = 1 << (Integer.SIZE - (m & MASK_OFFSET_IN_BLOCK));
	final int blockNum = m_maskSize * ADDRESS_ARRAY_SIZE / SIZE;
	childrenAddresses[0][blockNum] &= ~helper;
	childrenAddresses[1][blockNum] |= helper;

	if (m == SIZE) {
	    return new IPv6[] { valueOfFullIPv6Internal(childrenAddresses[0]),
		    valueOfFullIPv6Internal(childrenAddresses[1]) };
	}
	return new IPv6[] { new IPv6(childrenAddresses[0], m), new IPv6(childrenAddresses[1], m) };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final IP other) {
	if (!(other instanceof IPv6)) {
	    return false;
	}
	final IPv6 o = (IPv6) other;

	final int m = m_maskSize;
	if (m > other.m_maskSize) {
	    return false;
	}
	if (m == 0) {
	    return true;
	}

	int blockNum;
	for (blockNum = 0; blockNum < m / Integer.SIZE; blockNum++) {
	    if (m_address[blockNum] != o.m_address[blockNum]) {
		return false;
	    }
	}
	if (m == SIZE) {
	    return true;
	}

	return ((m_address[blockNum] ^ o.m_address[blockNum])
		& ~((1 << (Integer.SIZE - (m & MASK_OFFSET_IN_BLOCK))) - 1)) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getBit(final int bitNumber) {
	if (bitNumber < 0 || bitNumber > SIZE) {
	    throw new IndexOutOfBoundsException("Bit number should be in range [0, " + SIZE + "]");
	}
	final int blockNum = bitNumber == 0 ? 0 : (bitNumber - 1) / Integer.SIZE;
	final int bitNumInBlock = bitNumber - blockNum * Integer.SIZE;
	return (m_address[blockNum] & (1 << (Integer.SIZE - bitNumInBlock))) != 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getLastBit() {
	final int m = m_maskSize;
	final int blockNum = m == 0 ? 0 : (m - 1) / Integer.SIZE;
	final int bitNumInBlock = m - blockNum * Integer.SIZE;
	return (m_address[blockNum] & (1 << Integer.SIZE - bitNumInBlock)) != 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBrother(final IP other) {
	if (!(other instanceof IPv6)) {
	    return false;
	}
	final IPv6 o = (IPv6) other;

	final int p = m_maskSize, op = o.m_maskSize;
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
	final int shiftSize = Integer.SIZE - ((p - 1) & MASK_OFFSET_IN_BLOCK);
	return (aAddress[lastEqualBlock] >> shiftSize) == (bAddress[lastEqualBlock] >> shiftSize);
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
	}
	if (!(o instanceof IPv6)) {
	    return false;
	}

	final IPv6 other = (IPv6) o;
	if (m_maskSize != other.m_maskSize) {
	    return false;
	}
	final int[] thisAddress = m_address;
	final int[] otherAddress = other.m_address;
	for (int i = 0; i < thisAddress.length; i++) {
	    if (thisAddress[i] != otherAddress[i]) {
		return false;
	    }
	}
	return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
	int h = 31 + m_maskSize;
	final int[] address = m_address;
	for (int blockVal : address) {
	    h = h * 31 + blockVal;
	}
	return h;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
	final int m = m_maskSize;
	if (m == 0) {
	    return ANY_IPv6_STR;
	}

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
     * {@inheritDoc}
     */
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
	    final int a1 = thisAddress[i];
	    final int a2 = otherAddress[i];
	    // unsigned compare
	    final int diff = a1 == a2 ? 0 : (a1 + Integer.MIN_VALUE) < (a2 + Integer.MIN_VALUE) ? -1 : 1;
	    if (diff != 0) {
		return diff;
	    }
	}

	return m_maskSize - other.m_maskSize;
    }

    /**
     * Get IPv6 object parsed from boolean bits list.
     * 
     * @param addressBits
     *            the bits list.
     * @return IPv6 with the address build from the bits.
     * @throws NullPointerException
     *             if the list is null or one of the Boolean objects in the list
     *             is null.
     * @throws IllegalArgumentException
     *             if number of bits is unequal to {@value #SIZE}.
     */
    public static IPv6 parseIPv6FromBits(final List<Boolean> addressBits) {

	// TODO - remove this method.

	if (addressBits.size() != SIZE) {
	    throw new IllegalArgumentException("IPv6 size: " + Utility.formatEqual(SIZE, addressBits.size()));
	}
	final Iterator<Boolean> it = addressBits.iterator();
	final int[] address = new int[ADDRESS_ARRAY_SIZE];
	for (int blockNum = 0; blockNum < BLOCK_NUMBER; blockNum++) {
	    int blockValue = 0;
	    for (int bitNum = 0; bitNum < BLOCK_SIZE; bitNum++) {
		blockValue <<= 1;
		if (it.next().booleanValue()) {
		    blockValue += 1;
		}
	    }
	    blockValue <<= (blockNum & 1) == 0 ? 16 : 0;
	    address[blockNum >> 1] |= blockValue;
	}
	return valueOfFullIPv6Internal(address);
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
	if (s.equals(ANY_IPv6_STR)) {
	    return ANY_IPv6;
	}

	final int[] address = new int[ADDRESS_ARRAY_SIZE];

	int fromIndex = 0;
	int blockNumber = 0;
	int separatorIndex = s.indexOf(BLOCKS_SEPARATOR);

	while (separatorIndex >= 0) {
	    if (fromIndex == separatorIndex) {
		throw new IllegalArgumentException("Empty block. " + s);
	    }
	    if (blockNumber == BLOCK_NUMBER - 1) {
		throw new IllegalArgumentException("Too many IPv6 blocks, expected " + BLOCK_NUMBER);
	    }
	    int blockVal = parseBlockValue(s, fromIndex, separatorIndex);
	    if ((blockNumber & 1) == 0)
		blockVal <<= 16;
	    address[blockNumber >> 1] |= blockVal;
	    blockNumber++;
	    fromIndex = separatorIndex + 1;
	    separatorIndex = s.indexOf(BLOCKS_SEPARATOR, fromIndex);
	}
	if (blockNumber != BLOCK_NUMBER - 1) {
	    throw new IllegalArgumentException(
		    "IPv6 blocks number: " + Utility.formatEqual(BLOCK_NUMBER, blockNumber + 1));
	}

	final int maskSeparatorIndex = s.indexOf(MASK_SIZE_SEPARATOR, fromIndex);
	if (maskSeparatorIndex < 0) {
	    // No mask size specification
	    final int lastBlockVal = parseBlockValue(s, fromIndex, s.length());
	    address[ADDRESS_ARRAY_SIZE - 1] |= lastBlockVal;
	    return valueOfFullIPv6Internal(address);
	}

	// Has mask size specification
	final int lastBlockVal = parseBlockValue(s, fromIndex, maskSeparatorIndex);
	address[ADDRESS_ARRAY_SIZE - 1] |= lastBlockVal;

	// Read subnetwork mask
	final int maskSize;
	try {
	    maskSize = Integer.parseInt(s.substring(maskSeparatorIndex + 1, s.length()));
	} catch (NumberFormatException e) {
	    throw new IllegalArgumentException("IPv6 subnetwork mask", e);
	}

	if (!(0 < maskSize && maskSize < SIZE)) {
	    if (maskSize == SIZE) {
		return valueOfFullIPv6Internal(address);
	    }
	    if (maskSize == 0) {
		return ANY_IPv6;
	    }
	    throw new IllegalArgumentException("IPv6 subnetwork mask: " + Utility.formatRange(0, SIZE, maskSize));
	}

	// Reset by mask
	for (int blockNum = ADDRESS_ARRAY_SIZE; blockNum-- > 0 && maskSize < ((blockNum + 1) << 5);) {
	    address[blockNum] &= maskSize <= (blockNum << 5) ? 0
		    : ~((1 << (Integer.SIZE - (maskSize & MASK_OFFSET_IN_BLOCK))) - 1);
	}

	return new IPv6(address, maskSize);
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
	return valueOf(address, SIZE);
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
    public static IPv6 valueOf(final int[] address, final int maskSize) {
	if (address.length != BLOCK_NUMBER) {
	    throw new IllegalArgumentException(
		    "IPv6 block number: " + Utility.formatEqual(BLOCK_NUMBER, address.length));
	}

	final int[] a = new int[ADDRESS_ARRAY_SIZE];
	for (int blockNum = BLOCK_NUMBER; blockNum-- > 0;) {
	    int blockValue = address[blockNum];
	    if (!(0 <= blockValue && blockValue <= MAX_BLOCK_VALUE)) {
		throw new IllegalArgumentException(
			"IPv6 block value: " + Utility.formatRange(0, MAX_BLOCK_VALUE, blockValue));
	    }
	    if ((blockNum & 1) == 0) {
		blockValue <<= 16;
	    }
	    a[blockNum >> 1] |= blockValue;
	}
	if (!(0 < maskSize && maskSize < SIZE)) {
	    if (maskSize == SIZE) {
		return valueOfFullIPv6Internal(a);
	    }
	    if (maskSize == 0) {
		return ANY_IPv6;
	    }
	    throw new IllegalArgumentException("IPv6 subnetwork mask size: " + Utility.formatRange(0, SIZE, maskSize));
	}

	// Reset suffix
	for (int blockNum = ADDRESS_ARRAY_SIZE; blockNum-- > 0 && maskSize < ((blockNum + 1) << 5);) {
	    a[blockNum] &= maskSize <= (blockNum << 5) ? 0
		    : ~((1 << (Integer.SIZE - (maskSize & MASK_OFFSET_IN_BLOCK))) - 1);

	}
	return new IPv6(a, maskSize);
    }

    /**
     * Get IPv6 object with the specified 128 bits(ints array) address.
     * 
     * @param addressBits
     *            the 128 bits of the address.
     * @return IPv6 object with the specified address.
     * @throws NullPointerException
     *             if the address bits array is null.
     * @throws IllegalArgumentException
     *             if the size of the address array is not
     *             {@value #ADDRESS_ARRAY_SIZE}.
     */
    public static IPv6 valueOfBits(final int[] addressBits) {
	return valueOfBits(addressBits, SIZE);
    }

    /**
     * Get IPv6 object with the specified 128 bits(ints array) address and
     * subnetwork mask size.
     * 
     * @param addressBits
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
    public static IPv6 valueOfBits(final int[] addressBits, final int maskSize) {
	if (addressBits.length != ADDRESS_ARRAY_SIZE) {
	    throw new IllegalArgumentException(
		    "Address bits array: " + Utility.formatEqual(ADDRESS_ARRAY_SIZE, addressBits.length));
	}
	// Must clone addressBits to be safe that the address won't be changed
	// in the future.
	final int[] address = addressBits.clone();
	if (!(0 < maskSize && maskSize < SIZE)) {
	    if (maskSize == SIZE) {
		return valueOfFullIPv6Internal(address);
	    }
	    if (maskSize == 0) {
		return ANY_IPv6;
	    }
	    throw new IllegalArgumentException("IPv6 subnetwork mask size: " + Utility.formatRange(0, SIZE, maskSize));
	}

	return new IPv6(address, maskSize);
    }

    /**
     * Get full (maskSize = {@value #SIZE}) IPv6 object with the specified
     * address, used internally.
     * 
     * @param address
     *            the 128 bits array of the address.
     * @return IPv6 object with the specified address and maskSize.
     */
    private static IPv6 valueOfFullIPv6Internal(final int[] address) {
	return IPv6Cache.cache.getOrAdd(address, IPv6Cache.supplier);
    }

    /**
     * Cache of {@link IPv6} objects.
     * 
     * @author Barak Ugav
     * @author Yishai Gronich
     *
     * @see Cache
     */
    private static class IPv6Cache {

	/**
	 * Cache of full (maskSize = {@link IPv6#SIZE}) IPv6 objects.
	 */
	static final Cache<int[], IPv6> cache;

	/**
	 * Supplier of IPv6 object by address.
	 * <p>
	 * Used by {@link Cache#getOrAdd(Object, Function)}.
	 */
	static final Function<int[], IPv6> supplier;

	static {
	    cache = Caches.synchronizedCache(new SoftCustomHashCache<>(IPv6AddressesStrategy.INSTANCE));
	    supplier = address -> new IPv6(address, SIZE);
	}

	/**
	 * Strategy of IPv4 addresses.
	 * 
	 * @author Barak Ugav
	 * @author Yishai Gronich
	 *
	 * @see SoftCustomHashCache
	 */
	private static class IPv6AddressesStrategy implements Strategy<int[]> {

	    /**
	     * The single instance of the strategy.
	     */
	    private static final IPv6AddressesStrategy INSTANCE = new IPv6AddressesStrategy();

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

    /**
     * Parse a block of IPv6 and check if it's range is valid.
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
     *             {@link IPv6#MAX_BLOCK_VALUE}].
     */
    private static int parseBlockValue(final String str, final int fromIndex, final int toIndex) {
	final int blockVal;
	try {
	    blockVal = Integer.parseInt(str.substring(fromIndex, toIndex));
	} catch (final NumberFormatException e) {
	    throw new IllegalArgumentException(e);
	}
	if (!(0 <= blockVal && blockVal <= MAX_BLOCK_VALUE)) {
	    throw new IllegalArgumentException(
		    "IPv6 block value: " + Utility.formatRange(0, MAX_BLOCK_VALUE, blockVal));
	}
	return blockVal;
    }

}
