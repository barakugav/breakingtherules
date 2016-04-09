package breakingtherules.firewall;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import breakingtherules.utilities.CloneablePublic;
import breakingtherules.utilities.Utility;

/**
 * IP address, can be {@link IPv4} or {@link IPv6}
 */
public abstract class IP implements Comparable<IP>, CloneablePublic {

    /**
     * IP address
     */
    protected final int[] m_address;

    /**
     * Length of the constant prefix
     */
    protected final int m_prefixLength;

    /**
     * Constructor
     * 
     * @param ip
     *            boolean array that represents the bits in the IP
     */
    public IP(boolean[] ip) {
	if (ip == null) {
	    throw new IllegalArgumentException("Ip can't be null");
	} else if (ip.length != getMaxLength()) {
	    throw new IllegalArgumentException(
		    "Unexpected ip length: " + Utility.format(getMaxLength(), ip.length));
	}

	m_address = new int[getNumberOfBlocks()];
	for (int blockNum = 0; blockNum < getNumberOfBlocks(); blockNum++) {
	    int blockValue = 0;
	    for (int bitNum = 0; bitNum < getBlockSize(); bitNum++) {
		boolean bitValue = ip[blockNum * getBlockSize() + bitNum];
		blockValue <<= 1;
		blockValue += bitValue ? 1 : 0;
	    }
	    m_address[blockNum] = blockValue;
	}
	m_prefixLength = getMaxLength();
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
	if (address == null) {
	    throw new IllegalArgumentException("Null arg");
	} else if (address.length != getNumberOfBlocks()) {
	    throw new IllegalArgumentException(
		    "Number of IP blocks doesn't match: " + address.length + " (Expected " + getNumberOfBlocks());
	} else if (prefixLength < 0 || prefixLength > getMaxLength()) {
	    throw new IllegalArgumentException("Const prefix length out of range: " + prefixLength);
	}
	for (int blockValue : address) {
	    if (blockValue < 0 || blockValue > getMaxBlockValue()) {
		throw new IllegalArgumentException("IP address block isn't in range: " + blockValue
			+ ". Should be in range [0, " + getMaxBlockValue() + "]");
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
	String expectedSeparator = getStringSeparator();
	List<Integer> address = new ArrayList<Integer>();
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

	if (address.size() != getNumberOfBlocks()) {
	    throw new IllegalArgumentException(
		    "Number of blocks is " + address.size() + " instead of " + getNumberOfBlocks());
	} else
	    for (int blockValue : address) {
		if (blockValue < 0 || blockValue > getMaxBlockValue())
		    throw new IllegalArgumentException("IP address block isn't in range: " + blockValue
			    + ". Should be in range [0, " + getMaxBlockValue() + "]");
	    }

	// Copy blocks values to m_address
	m_address = new int[getNumberOfBlocks()];
	for (int i = 0; i < address.size(); i++) {
	    m_address[i] = address.get(i);
	}

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
    public long getSubnetSize() {
	return 1L << (getMaxLength() - m_prefixLength);
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
	} else if (this instanceof AnyIP) {
	    return true;
	} else if (other instanceof AnyIP) {
	    return false;
	} else if (this instanceof IPv4 && !(other instanceof IPv4)) {
	    return false;
	} else if (this instanceof IPv6 && !(other instanceof IPv6)) {
	    return false;
	}

	if (!(m_prefixLength <= other.m_prefixLength)) {
	    return false;
	}

	int blockNum = 0;
	for (blockNum = 0; blockNum < m_prefixLength / getBlockSize(); blockNum++) {
	    if (m_address[blockNum] != other.m_address[blockNum]) {
		return false;
	    }
	}

	if (blockNum == getNumberOfBlocks()) {
	    return true;
	}

	int bitsLeft = getBlockSize() - (m_prefixLength % getBlockSize());
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
	    return "Any";
	}
	String st = Integer.toString(m_address[0]);
	for (int i = 1; i < m_address.length; i++) {
	    st += getStringSeparator() + m_address[i];
	}
	if (m_prefixLength != getMaxLength()) {
	    st += "/" + m_prefixLength;
	}
	return st;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	int hash = 1;
	for (int i = 0; i < m_address.length; i++) {
	    hash = hash * 31 + m_address[i];
	}
	return hash;
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
	if (m_prefixLength != other.m_prefixLength) {
	    return false;
	} else if (m_address.length != other.m_address.length) {
	    return false;
	}
	for (int i = 0; i < m_address.length; i++) {
	    if (m_address[i] != other.m_address[i]) {
		return false;
	    }
	}
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    @Override
    public abstract IP clone();

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
	if (ip == null) {
	    throw new IllegalArgumentException("Null ip");
	}

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
	if (ip == null || clazz == null) {
	    throw new IllegalArgumentException("Arguments can't be null!");
	}
	if (clazz.equals(IPv4.class)) {
	    return new IPv4(ip);
	} else if (clazz.equals(IPv6.class)) {
	    return new IPv6(ip);
	} else if (clazz.equals(AnyIP.class)) {
	    return AnyIP.instance;
	} else {
	    throw new IllegalArgumentException(
		    "Choosen class in unkwon. Expected IPv4, IPv6 or AnyIP. Actual: " + clazz.getName());
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
    public boolean getBit(int bitNumber) {
	if (bitNumber < 0 || bitNumber > getMaxLength()) {
	    throw new IllegalArgumentException("Bit number should be in range [0, " + getMaxLength() + "]");
	}
	int blockNum = bitNumber == 0 ? 0 : (bitNumber-1) / getBlockSize();
	int bitNumInBlock = bitNumber - blockNum * getBlockSize();
	return (m_address[blockNum] & (1 << bitNumInBlock)) != 0;
    }

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
	for (int i = 0; i < m_address.length; i++) {
	    int diff = m_address[i] - other.m_address[i];
	    if (diff != 0) {
		return diff;
	    }
	}

	return 0;
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
	return Objects.equals(a.getParent(), b.getParent());
    }

    /**
     * Get the common parent of two IPs objects (direct parent) or null if not
     * brothers
     * 
     * @param a
     *            first IP
     * @param b
     *            second IP
     * @return the parent if a and b are brothers, else null
     */
    public static IP getCommonParent(IP a, IP b) {
	IP parent = a.getParent();
	if (parent.equals(b.getParent())) {
	    return parent;
	} else {
	    return null;
	}
    }

    /**
     * Get IP that represents 'Any' IP (contains) all others
     * 
     * @return instance of 'Any' IP
     */
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
	int helper = 1 << (getBlockSize() - m_prefixLength % getBlockSize()) - 1;
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
	for (int bit = getMaxLength() - 1; bit >= m_prefixLength; bit--) {
	    int andHelper = ~(1 << (getBlockSize() - (bit % getBlockSize())) - 1);
	    int blockNum = bit * getNumberOfBlocks() / getMaxLength();
	    m_address[blockNum] &= andHelper;
	}
    }

    /**
     * Get the max value of block in this IP's address
     * 
     * @return max value of a block
     */
    @JsonIgnore
    private long getMaxBlockValue() {
	return (1L << getBlockSize()) - 1;
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

	public AnyIP clone() {
	    return this;
	}

    }

}
