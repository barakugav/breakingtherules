package breakingtherules.firewall;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import breakingtherules.utilities.Cache;
import breakingtherules.utilities.Caches;
import breakingtherules.utilities.Caches.CacheSupplierPair;
import breakingtherules.utilities.IntArrayStrategy;
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

    // TODO - javadoc
    private static final int OFFSET_IN_BLOCK_MASK = 0x1f; // 31

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

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getAddress()
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

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getAddressBits()
     */
    @Override
    public int[] getAddressBits() {
	return m_address.clone();
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
    public IPv6 getParent() {
	final int m = m_maskSize;
	if (m == 0) {
	    throw new IllegalStateException("no parent");
	}

	final int[] parentAddress = m_address.clone();
	final int blockNum = (m - 1) / Integer.SIZE;
	final int mask = ~(1 << (Integer.SIZE - (m & OFFSET_IN_BLOCK_MASK)));
	parentAddress[blockNum] &= mask;

	return new IPv6(parentAddress, m - 1);
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
    public IPv6[] getChildren() {
	final int m = m_maskSize + 1;
	if (m > SIZE) {
	    throw new IllegalStateException("no children");
	}

	// Set helper variable
	int[][] childrenAddresses = new int[][] { m_address.clone(), m_address.clone() };
	final int helper = 1 << (Integer.SIZE - (m & OFFSET_IN_BLOCK_MASK));
	final int blockNum = m_maskSize * ADDRESS_ARRAY_SIZE / SIZE;
	childrenAddresses[0][blockNum] &= ~helper;
	childrenAddresses[1][blockNum] |= helper;

	if (m == SIZE) {
	    return new IPv6[] { createInternal(childrenAddresses[0]), createInternal(childrenAddresses[1]) };
	}
	return new IPv6[] { new IPv6(childrenAddresses[0], m), new IPv6(childrenAddresses[1], m) };
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#contains(breakingtherules.firewall.IP)
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
		& ~((1 << (Integer.SIZE - (m & OFFSET_IN_BLOCK_MASK))) - 1)) == 0;
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
	final int blockNum = bitNumber == 0 ? 0 : (bitNumber - 1) / Integer.SIZE;
	final int bitNumInBlock = bitNumber - blockNum * Integer.SIZE;
	return (m_address[blockNum] & (1 << (Integer.SIZE - bitNumInBlock))) != 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#getLastBit()
     */
    @Override
    public boolean getLastBit() {
	final int m = m_maskSize;
	final int blockNum = m == 0 ? 0 : (m - 1) / Integer.SIZE;
	final int bitNumInBlock = m - blockNum * Integer.SIZE;
	return (m_address[blockNum] & (1 << Integer.SIZE - bitNumInBlock)) != 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IP#isBrother(breakingtherules.firewall.IP)
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
	final int shiftSize = Integer.SIZE - ((p - 1) & OFFSET_IN_BLOCK_MASK);
	return (aAddress[lastEqualBlock] >> shiftSize) == (bAddress[lastEqualBlock] >> shiftSize);
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
	} else if (!(o instanceof IPv6)) {
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	int h = 31 + m_maskSize;
	for (int blockVal : m_address) {
	    h = h * 31 + blockVal;
	}
	return h;
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
     * Create an IP from array of the address' blocks.
     * <p>
     * The address array should be at the length as IPv6 number of blocks(8).
     * <p>
     * For example:<br>
     * To create the IP 128.4.5.1.0.5789.500.21 the array should be [128, 4, 5,
     * 1, 0, 5789, 500, 21].<br>
     * 
     * @param address
     *            the address blocks.
     * @return IPv6 object with the desire address.
     * @throws NullPointerException
     *             if the address is null.
     * @throws IllegalArgumentException
     *             if the blocks values are out of range (0 to 65535).
     */
    public static IPv6 create(int[] address) {
	return create(address, SIZE);
    }

    /**
     * Create an IP from array of the address' blocks.
     * <p>
     * The address array should be at the length as IPv6 number of blocks(8).
     * <p>
     * For example:<br>
     * To create the IP 128.4.5.1.0.5789.500.0/112 the array should be [128, 4,
     * 5, 1, 0, 5789, 500, 0] and the maskSize should be 112.<br>
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
    public static IPv6 create(int[] address, int maskSize) {
	if (address.length != BLOCK_NUMBER) {
	    throw new IllegalArgumentException(
		    "IPv6 block number: " + Utility.formatEqual(BLOCK_NUMBER, address.length));
	}
	if (!(0 <= maskSize && maskSize <= SIZE)) {
	    throw new IllegalArgumentException("IPv6 subnetwork mask size: " + Utility.formatRange(0, SIZE, maskSize));
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

	if (maskSize == SIZE) {
	    createInternal(a);
	}

	// Reset suffix
	for (int blockNum = ADDRESS_ARRAY_SIZE; blockNum-- > 0 && maskSize < ((blockNum + 1) << 5);) {
	    a[blockNum] &= maskSize <= (blockNum << 5) ? 0
		    : ~((1 << (Integer.SIZE - (maskSize & OFFSET_IN_BLOCK_MASK))) - 1);

	}
	return new IPv6(a, maskSize);
    }

    /**
     * Create an IPv6 from string.
     * <p>
     * The expected format is: A:B:C:D:E:F:G:H or A:B:C:D:E:F:G:H/M when A, B,
     * C, D, E, F, G and H are the blocks value (in range 0 to 65535) and M is
     * the subnetwork mask size (in range 0 to 128).
     * 
     * @param ip
     *            string of IP.
     * @return IPv6 object parsed from string.
     * @throws NullPointerException
     *             if the string is null.
     * @throws IllegalArgumentException
     *             if the format is illegal or the values are out of range.
     */
    public static IPv6 createFromString(String ip) {
	// TODO - better implementation like IPv4.create(String) using
	// Utility.breakToWords(String).
	int maskSize;

	final List<Integer> address = new ArrayList<>();
	try {
	    int separatorIndex = ip.indexOf(BLOCKS_SEPARATOR);

	    // Read address blocks
	    while (separatorIndex >= 0) {
		String stNum = ip.substring(0, separatorIndex);
		int intNum = Integer.parseInt(stNum);
		address.add(Integer.valueOf(intNum));
		ip = ip.substring(separatorIndex + 1);
		separatorIndex = ip.indexOf(BLOCKS_SEPARATOR);
	    }

	    // Read suffix of IP - last block
	    separatorIndex = ip.indexOf('/');
	    if (separatorIndex < 0) {
		// No mask size specification
		address.add(Integer.valueOf(Integer.parseInt(ip)));
		maskSize = SIZE;
	    } else {
		// Has mask size specification
		String stNum = ip.substring(0, separatorIndex);
		ip = ip.substring(separatorIndex + 1);

		final int intNum = Integer.parseInt(stNum);
		address.add(Integer.valueOf(intNum));

		// Read mask size
		if (ip.length() > 0) {
		    maskSize = Integer.parseInt(ip);
		    if (maskSize < 0) {
			throw new IllegalArgumentException("Negative mask size");
		    } else if (maskSize > SIZE) {
			throw new IllegalArgumentException("Mask size over SIZE");
		    }
		} else {
		    maskSize = SIZE;
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
	    addressTemp[i] = address.get(i).intValue();
	}

	final int[] a = new int[ADDRESS_ARRAY_SIZE];
	for (int blockNum = 0; blockNum < BLOCK_NUMBER; blockNum++) {
	    int blockValue = addressTemp[blockNum];
	    if (!(0 <= blockValue && blockValue <= MAX_BLOCK_VALUE)) {
		throw new IllegalArgumentException(
			"IPv6 block value: " + Utility.formatRange(0, MAX_BLOCK_VALUE, blockValue));
	    }
	    blockValue <<= (blockNum & 1) == 0 ? 16 : 0;
	    a[blockNum >> 1] |= blockValue;
	}

	if (maskSize == SIZE) {
	    return createInternal(a);
	}

	// Reset by mask
	for (int blockNum = ADDRESS_ARRAY_SIZE; blockNum-- > 0 && maskSize < ((blockNum + 1) << 5);) {
	    a[blockNum] &= maskSize <= (blockNum << 5) ? 0
		    : ~((1 << (Integer.SIZE - (maskSize & OFFSET_IN_BLOCK_MASK))) - 1);
	}

	return new IPv6(a, maskSize);
    }

    /**
     * Create an IPv6 from 128 bits (ints array).
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
    public static IPv6 createFromBits(final int[] addressBits) {
	return createFromBits(addressBits, SIZE);
    }

    /**
     * Create an IPv6 from 128 bits (ints array) and specified subnetwork mask
     * size.
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
    public static IPv6 createFromBits(int[] addressBits, final int maskSize) {
	if (addressBits.length != ADDRESS_ARRAY_SIZE) {
	    throw new IllegalArgumentException(
		    "Address bits array: " + Utility.formatEqual(ADDRESS_ARRAY_SIZE, addressBits.length));
	}
	// Must clone addressBits to be safe that the address won't be changed
	// in the future.
	addressBits = addressBits.clone();
	if (!(0 <= maskSize && maskSize < SIZE)) {
	    if (maskSize == SIZE) {
		return createInternal(addressBits);
	    }
	    throw new IllegalArgumentException("IPv6 subnetwork mask size: " + Utility.formatRange(0, SIZE, maskSize));
	}

	return new IPv6(addressBits, maskSize);
    }

    /**
     * Create an IPv6 from boolean bits list.
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
    public static IPv6 createFromBits(final List<Boolean> addressBits) {
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
	return createInternal(address);
    }

    /**
     * Create an IPv6, used internally.
     * 
     * @param address
     *            the 128 bits array of the address.
     * @param maskSize
     *            the subnetwork mask size.
     * @return IPv6 object with the specified address and maskSize.
     */
    private static IPv6 createInternal(final int[] address) {
	return IPv6Cache.cache.getOrAdd(address);
    }

    /**
     * Cache of {@link IPv6} objects.
     * 
     * @author Barak Ugav
     * @author Yishai Gronich
     *
     */
    private static class IPv6Cache {

	/**
	 * All caches.
	 * <p>
	 * The caches array is of size {@value IPv4#SIZE} + 1, and in each cache
	 * 'i' the elements are the IPs with maskSize = i.
	 */
	static final CacheSupplierPair<int[], IPv6> cache;

	static {
	    final Cache<int[], IPv6> c = Caches.synchronizedCache(new SoftCustomHashCache<>(IntArrayStrategy.INSTANCE));
	    cache = Caches.cacheSupplierPair(c, address -> new IPv6(address, SIZE));
	}

    }

}
