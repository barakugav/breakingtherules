package breakingtherules.firewall;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import breakingtherules.utilities.Utility;

/**
 * IP address, can be {@link IPv4} or {@link IPv6}
 */
public abstract class IP implements Comparable<IP> {

    /**
     * IP address
     */
    protected final int[] m_address;

    /**
     * Length of the constant prefix
     */
    protected final int m_prefixLength;

    /**
     * Cache for the IP's hash
     */
    private int hash;

    /**
     * String representation of any IP
     */
    private static final String ANY = "Any";

    /**
     * Constructor
     * 
     * @param ip
     *            boolean array that represents the bits in the IP
     */
    public IP(boolean[] ip) {
	int maxLength = getMaxLength();
	if (ip.length != maxLength) {
	    throw new IllegalArgumentException("Unexpected ip length: " + Utility.format(maxLength, ip.length));
	}

	final int numberOfBlocks = getNumberOfBlocks();
	final int blockSize = getBlockSize();
	int[] address = new int[numberOfBlocks];
	for (int blockNum = 0; blockNum < numberOfBlocks; blockNum++) {
	    int blockValue = 0;
	    for (int bitNum = 0; bitNum < blockSize; bitNum++) {
		boolean bitValue = ip[blockNum * blockSize + bitNum];
		blockValue <<= 1;
		blockValue += bitValue ? 1 : 0;
	    }
	    address[blockNum] = blockValue;
	}
	m_address = address;
	m_prefixLength = maxLength;
    }

    /**
     * Constructor
     * 
     * @param address
     *            IP address
     * @param prefixLength
     *            length of the constant prefix
     */
    protected IP(int[] address, int prefixLength) {
	final int numberOfBlocks = getNumberOfBlocks();
	if (address.length != numberOfBlocks) {
	    throw new IllegalArgumentException(
		    "Number of IP blocks doesn't match: " + address.length + " (Expected " + numberOfBlocks);
	} else if (prefixLength < 0 || prefixLength > getMaxLength()) {
	    throw new IllegalArgumentException("Const prefix length out of range: " + prefixLength);
	}
	int maxBlockValue = getMaxBlockValue();
	for (int blockValue : address) {
	    if (blockValue < 0 || blockValue > maxBlockValue) {
		throw new IllegalArgumentException("IP address block isn't in range: " + blockValue
			+ ". Should be in range [0, " + maxBlockValue + "]");
	    }
	}

	m_address = address;
	m_prefixLength = prefixLength;
	resetSuffix();
    }

    /**
     * Constructor from String IP
     * 
     * @param ip
     *            String IP
     * @param expectedSeparator
     *            String separator between two blocks in the String IP
     */
    protected IP(String ip) {
	final String expectedSeparator = getStringSeparator();
	List<Integer> address = new ArrayList<>();
	int separatorIndex = ip.indexOf(expectedSeparator);
	try {
	    // Read address blocks
	    while (separatorIndex >= 0) {
		String stNum = ip.substring(0, separatorIndex);
		int intNum = Integer.parseInt(stNum);
		address.add(intNum);
		ip = ip.substring(separatorIndex + 1);
		separatorIndex = ip.indexOf(expectedSeparator);
	    }

	    // Read suffix of IP - last block
	    separatorIndex = ip.indexOf('/');
	    if (separatorIndex < 0) {
		// No const prefix specification
		address.add(Integer.parseInt(ip));
		m_prefixLength = getMaxLength();
	    } else {
		// Has const prefix specification
		String stNum = ip.substring(0, separatorIndex);
		ip = ip.substring(separatorIndex + 1);

		int intNum = Integer.parseInt(stNum);
		address.add(intNum);

		// Read const prefix length
		if (ip.length() > 0) {
		    m_prefixLength = Integer.parseInt(ip);
		    if (m_prefixLength < 0) {
			throw new IllegalArgumentException("Negative prefix length");
		    } else if (m_prefixLength > getMaxLength()) {
			throw new IllegalArgumentException("Prefix length over max length");
		    }
		} else {
		    m_prefixLength = 32;
		}
	    }
	} catch (NumberFormatException e) {
	    throw new IllegalArgumentException("Integer parse failed: " + e.getMessage());
	}

	final int numberOfBlocks = getNumberOfBlocks();
	if (address.size() != numberOfBlocks) {
	    throw new IllegalArgumentException(
		    "Number of blocks is " + address.size() + " instead of " + numberOfBlocks);
	} else {
	    int maxBlockValue = getMaxBlockValue();
	    for (int blockValue : address) {
		if (blockValue < 0 || blockValue > maxBlockValue) {
		    throw new IllegalArgumentException("IP address block isn't in range: " + blockValue
			    + ". Should be in range [0, " + maxBlockValue + "]");
		}
	    }
	}

	// Copy blocks values to m_address
	int[] addressTemp = new int[numberOfBlocks];
	for (int i = 0; i < address.size(); i++) {
	    addressTemp[i] = address.get(i);
	}
	m_address = addressTemp;

	resetSuffix();
    }

    /**
     * Get the address of the IP
     * 
     * @return address of the IP
     */
    public int[] getAddress() {
	return m_address;
    }

    /**
     * Get the length of the constant prefix of the IP
     * 
     * @return length of the constant prefix of the IP
     */
    public int getConstPrefixLength() {
	return m_prefixLength;
    }

    /**
     * Get the size of the sub network of this IP
     * 
     * @return this IP's network size
     */
    @JsonIgnore
    public int getSubnetBitsNum() {
	return getMaxLength() - m_prefixLength;
    }

    /**
     * Checks if this IP has parent - more general IP
     * 
     * @return true if this IP has parent, else - false
     */
    public boolean hasParent() {
	return m_prefixLength > 0;
    }

    /**
     * Get this IP's parent - more general IP
     * 
     * @return this IP's parent
     */
    @JsonIgnore
    public abstract IP getParent();

    /**
     * Checks if this IP has children - more specific IPs
     * 
     * @return true if this IP has children. else - false
     */
    public boolean hasChildren() {
	return m_prefixLength < getMaxLength();
    }

    /**
     * Get this IP's children - more specific IP's
     * 
     * @return this IP's children
     */
    @JsonIgnore
    public abstract IP[] getChildren();

    /**
     * Checks if this IP (sub-network) contain other IP
     * 
     * By definition, this contains itself.
     * 
     * @param other
     *            other IP to compare to
     * @return true if this IP contain in his sub-network the other IP
     */
    public boolean contains(IP other) {
	if (this == other) {
	    return true;
	} else if (other == null) {
	    return false;
	} else if (other instanceof AnyIP) {
	    return false;
	} else if (!getClass().equals(other.getClass())) {
	    return false;
	}

	if (m_prefixLength > other.m_prefixLength) {
	    return false;
	}

	final int blockSize = getBlockSize();
	int blockNum = 0;
	for (blockNum = 0; blockNum < m_prefixLength / blockSize; blockNum++) {
	    if (m_address[blockNum] != other.m_address[blockNum]) {
		return false;
	    }
	}

	if (blockNum == getNumberOfBlocks()) {
	    return true;
	}

	int bitsLeft = blockSize - (m_prefixLength % blockSize);
	return (m_address[blockNum] ^ other.m_address[blockNum]) < (1L << bitsLeft);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	if (m_prefixLength == 0) {
	    return ANY;
	}

	int[] address = m_address;
	final String separator = getStringSeparator();
	StringBuilder builder = new StringBuilder(Integer.toString(address[0]));
	for (int i = 1; i < m_address.length; i++) {
	    builder.append(separator);
	    builder.append(address[i]);
	}
	int prefix = m_prefixLength;
	if (prefix != getMaxLength()) {
	    builder.append("/");
	    builder.append(prefix);
	}
	return builder.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	// Look for cached hash first
	int h = hash;
	int[] address = m_address;
	if (h != 0 || address.length == 0)
	    return h;

	h = 1;
	for (int i = 0; i < address.length; i++) {
	    h = h * 31 + address[i];
	}
	return hash = h;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
	if (o == this) {
	    return true;
	} else if (o == null) {
	    return false;
	} else if (!(o instanceof IP)) {
	    return false;
	}

	IP other = (IP) o;
	int[] thisAddress = m_address;
	int[] otherAddress = other.m_address;
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

    /**
     * Create new IP from String IP
     * 
     * Detect format - IPv4 or IPv6
     * 
     * @param ip
     *            String IP
     * @return IP object based on the String IP
     */
    public static IP fromString(String ip) {
	if (ip.equals("Any")) {
	    return getAnyIP();
	}

	// IPv4 format
	boolean isIPv4 = ip.indexOf(IPv4.STRING_SEPARATOR) >= 0;
	boolean isIPv6 = ip.indexOf(IPv6.STRING_SEPARATOR) >= 0;
	if (isIPv4 && isIPv6) {
	    throw new IllegalArgumentException("Unknown format");
	} else if (isIPv4) {
	    return new IPv4(ip);
	} else if (isIPv6) {
	    return new IPv6(ip);
	} else {
	    throw new IllegalArgumentException("Unknown format");
	}
    }

    /**
     * Create new IP from boolean array
     * 
     * @param ip
     *            bits of the IP
     * @param clazz
     *            class of the requested IP - IPv4, IPv6 or AnyIP
     * @return IP object based on the boolean bits
     */
    public static IP fromBooleans(boolean[] ip, Class<?> clazz) {
	if (clazz.equals(IPv4.class)) {
	    return new IPv4(ip);
	} else if (clazz.equals(IPv6.class)) {
	    return new IPv6(ip);
	} else if (clazz.equals(AnyIP.class)) {
	    return AnyIP.instance;
	} else {
	    throw new IllegalArgumentException(
		    "Choosen class in unkwon. Expected IPv4, IPv6 or AnyIP. Actual: " + clazz.getSimpleName());
	}
    }

    /**
     * Get the max length of this IP's address
     * 
     * @return max length of the IP's address
     */
    @JsonIgnore
    public int getMaxLength() {
	return getBlockSize() * getNumberOfBlocks();
    }

    /**
     * Get the value of specific bit in the IP
     * 
     * @param bitNumber
     *            the bit's number
     * @return value of the requested bit
     */
    @JsonIgnore
    public boolean getBit(int bitNumber) {
	if (bitNumber < 0 || bitNumber > getMaxLength()) {
	    throw new IllegalArgumentException("Bit number should be in range [0, " + getMaxLength() + "]");
	}
	final int blockSize = getBlockSize();
	int blockNum = bitNumber == 0 ? 0 : (bitNumber - 1) / blockSize;
	int bitNumInBlock = bitNumber - blockNum * blockSize;
	return (m_address[blockNum] & (1 << (blockSize - bitNumInBlock))) != 0;
    }

    /**
     * Get the last bit value
     * 
     * @return true if the last value is 1, else false
     */
    @JsonIgnore
    public boolean getLastBit() {
	return getBit(m_prefixLength);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(IP other) {
	if (other == null) {
	    return -1;
	}

	// Assume AnyIP < IPv4 < IPv6
	int thisIpType = this instanceof AnyIP ? 0 : this instanceof IPv4 ? 1 : 2;
	int otherIpType = other instanceof AnyIP ? 0 : other instanceof IPv4 ? 1 : 2;
	if (thisIpType != otherIpType) {
	    return thisIpType - otherIpType;
	}
	int[] thisAddress = m_address;
	int[] otherAddress = other.m_address;
	for (int i = 0; i < thisAddress.length; i++) {
	    int diff = thisAddress[i] - otherAddress[i];
	    if (diff != 0) {
		return diff;
	    }
	}

	return m_prefixLength - other.m_prefixLength;
    }

    /**
     * Checks if two IPs are brothers (have the same parent)
     * 
     * @param a
     *            first IP
     * @param b
     *            second IP
     * @return true if a and b are brothers, else -false
     */
    public static boolean isBrothers(IP a, IP b) {
	if (a.getClass() != b.getClass())
	    return false;
	int aPrefix = a.m_prefixLength, bPrefix = b.m_prefixLength;
	if (aPrefix != bPrefix)
	    return false; // Different sub network sizes
	if (aPrefix == 0)
	    return true; // Both are biggest sub network

	int[] aAddress = a.m_address, bAddress = b.m_address;
	final int blockSize = a.getBlockSize();
	int lastEqualBlock = (aPrefix - 1) / blockSize;
	for (int blockNum = 0; blockNum < lastEqualBlock; blockNum++)
	    if (aAddress[blockNum] != bAddress[blockNum])
		return false;
	int shiftSize = blockSize - ((aPrefix - 1) % blockSize);
	return (aAddress[lastEqualBlock] >> shiftSize) == (bAddress[lastEqualBlock] >> shiftSize);
    }

    /**
     * Get IP that represents 'Any' IP (contains) all others
     * 
     * @return instance of 'Any' IP
     */
    @JsonIgnore
    public static IP getAnyIP() {
	return AnyIP.instance;
    }

    /**
     * Get the addresses of this IP's children - more specific IPs
     * 
     * @return addresses of this IP's children
     */
    @JsonIgnore
    protected int[][] getChildrenAdresses() {
	if (!hasChildren()) {
	    return null;
	}

	// Set helper variable
	int[][] childrenAddresses = new int[][] { m_address.clone(), m_address.clone() };
	final int blockSize = getBlockSize();
	int helper = 1 << (blockSize - m_prefixLength % blockSize) - 1;
	int blockNum = m_prefixLength * getNumberOfBlocks() / getMaxLength();

	childrenAddresses[0][blockNum] &= ~helper;
	childrenAddresses[1][blockNum] |= helper;

	return childrenAddresses;
    }

    /**
     * Get the number of blocks in this IP
     * 
     * @return number of blocks in the IP
     */
    @JsonIgnore
    protected abstract int getNumberOfBlocks();

    /**
     * Get the size of each block in this IP
     * 
     * @return size of block in the IP
     */
    @JsonIgnore
    protected abstract int getBlockSize();

    /**
     * Get the string separator used when converting IP to string
     * 
     * @return string separator of this IP
     */
    @JsonIgnore
    protected abstract String getStringSeparator();

    /**
     * Set all bits after const prefix to zeros
     */
    private void resetSuffix() {
	final int blockSize = getBlockSize();
	final int numberOfBlocks = getNumberOfBlocks();
	final int maxLength = getMaxLength();
	for (int bit = getMaxLength() - 1; bit >= m_prefixLength; bit--) {
	    int andHelper = ~(1 << (blockSize - (bit % blockSize)) - 1);
	    int blockNum = bit * numberOfBlocks / maxLength;
	    m_address[blockNum] &= andHelper;
	}
    }

    /**
     * Get the max value of block in this IP's address
     * 
     * @return max value of a block
     */
    @JsonIgnore
    private int getMaxBlockValue() {
	return (1 << getBlockSize()) - 1;
    }

    /**
     * The AnyIP class represents 'Any' IP (contains all others). This class is
     * singleton
     */
    private static class AnyIP extends IP {

	private static final AnyIP instance = new AnyIP(new int[0], 0);

	private AnyIP(int[] address, int prefixLength) {
	    super(address, prefixLength);
	}

	@Override
	public boolean hasParent() {
	    return false;
	}

	@Override
	public IP getParent() {
	    return null;
	}

	@Override
	public boolean hasChildren() {
	    return false;
	}

	@Override
	public IP[] getChildren() {
	    return null;
	}

	@Override
	public boolean getLastBit() {
	    return false;
	}

	@Override
	public boolean contains(IP other) {
	    return true;
	}

	@Override
	public int getNumberOfBlocks() {
	    return 0;
	}

	@Override
	public int getBlockSize() {
	    return 0;
	}

	@Override
	public String getStringSeparator() {
	    return null;
	}

	@Override
	public boolean equals(Object o) {
	    return o instanceof AnyIP;
	}

	@Override
	public int hashCode() {
	    return 0;
	}

	@Override
	public String toString() {
	    return ANY;
	}

    }

}
