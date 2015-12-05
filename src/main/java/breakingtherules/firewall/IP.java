package breakingtherules.firewall;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * IP address, can be {@link IPv4} or {@link IPv6}
 */
public abstract class IP {

    /**
     * IP address
     */
    protected int[] m_address;

    /**
     * Length of the constant prefix
     */
    protected int m_prefixLength;

    /**
     * Constructor
     * 
     * @param address
     *            IP address
     * @param prefixLength
     *            length of the constant prefix
     */
    public IP(int[] address, int prefixLength) throws IllegalArgumentException {
	if (address == null)
	    throw new IllegalArgumentException("Null arg");
	if (address.length != getNumberOfBlocks())
	    throw new IllegalArgumentException(
		    "Number of IP blocks doesn't match: " + address.length + " (Expected " + getNumberOfBlocks());
	if (prefixLength < 0 || prefixLength > getMaxLength())
	    throw new IllegalArgumentException("Const prefix length out of range: " + prefixLength);

	for (int blockValue : address)
	    if (blockValue < 0 || blockValue > getMaxBlockValue())
		throw new IllegalArgumentException("IP address block isn't in range: " + blockValue
			+ ". Should be in range [0, " + getMaxBlockValue() + "]");

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
    protected IP(String ip, String expectedSeparator) throws IllegalArgumentException {
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
		    if (m_prefixLength < 0)
			throw new IllegalArgumentException("Negative prefix length");
		    if (m_prefixLength > getMaxLength())
			throw new IllegalArgumentException("Prefix length over max length");
		}
	    }
	} catch (NumberFormatException e) {
	    throw new IllegalArgumentException("Integer parse failed: " + e.getMessage());
	}

	if (address.size() != getNumberOfBlocks())
	    throw new IllegalArgumentException(
		    "Number of blocks is " + address.size() + " instead of " + getNumberOfBlocks());
	for (int blockValue : address)
	    if (blockValue < 0 || blockValue > getMaxBlockValue())
		throw new IllegalArgumentException("IP address block isn't in range: " + blockValue
			+ ". Should be in range [0, " + getMaxBlockValue() + "]");

	// Copy blocks values to m_address
	m_address = new int[getNumberOfBlocks()];
	for (int i = 0; i < address.size(); i++)
	    m_address[i] = address.get(i);

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
     * Get the length of the const prefix of the IP
     * 
     * @return length of the const prefix of the IP
     */
    public int getConstPrefixLength() {
	return m_prefixLength;
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
	if (other == null)
	    return false;
	if (this instanceof IPv4 && !(other instanceof IPv4))
	    return false;
	if (this instanceof IPv6 && !(other instanceof IPv6))
	    return false;

	if (!(m_prefixLength <= other.m_prefixLength))
	    return false;

	int blockNum = 0;
	for (blockNum = 0; blockNum < m_prefixLength / getBlockSize(); blockNum++)
	    if (m_address[blockNum] != other.m_address[blockNum])
		return false;

	if (blockNum == getNumberOfBlocks())
	    return true;

	int bitsLeft = getBlockSize() - (m_prefixLength % getBlockSize());
	return (m_address[blockNum] ^ other.m_address[blockNum]) < (1 << bitsLeft);
    }

    @Override
    public String toString() {
	if (m_prefixLength == 0)
	    return "Any";
	String st = Integer.toString(m_address[0]);
	for (int i = 1; i < m_address.length; i++)
	    st += getStringSeparator() + m_address[i];
	if (m_prefixLength != getMaxLength()) {
	    st += "/";
	    st += m_prefixLength;
	}
	return st;
    }

    @Override
    public int hashCode() {
	int sum = 0;
	for (int i = 0; i < m_address.length; i++)
	    sum += m_address[i] << (i * Integer.BYTES / getNumberOfBlocks());
	return sum;
    }

    @Override
    public boolean equals(Object o) {
	if (o == null)
	    return false;
	if (!(o instanceof IP))
	    return false;

	IP other = (IP) o;
	if (this.m_prefixLength != other.m_prefixLength)
	    return false;
	if (this.m_address.length != other.m_address.length)
	    return false;
	for (int i = 0; i < m_address.length; i++)
	    if (this.m_address[i] != other.m_address[i])
		return false;
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
    public static IP fromString(String ip) throws IllegalArgumentException {
	if (ip == null) {
	    throw new IllegalArgumentException("Null arg");
	}

	if (ip.length() < 5) {
	    throw new IllegalArgumentException("Unknown format");
	}

	// IPv4 format
	if (ip.substring(0, 5).equals("IPv4 ")) {
	    return new IPv4(ip.substring(5));
	}

	// IPv6 format
	if (ip.substring(0, 5).equals("IPv6 ")) {
	    return new IPv6(ip.substring(5));
	}

	throw new IllegalArgumentException("Unknown format");
    }

    /**
     * Get the number of blocks in this IP
     * 
     * @return number of blocks in the IP
     */
    @JsonIgnore
    public abstract int getNumberOfBlocks();

    /**
     * Get the size of each block in this IP
     * 
     * @return size of block in the IP
     */
    public abstract int getBlockSize();

    /**
     * Get the string separator used when converting IP to string
     * 
     * @return string separator of this IP
     */
    @JsonIgnore
    public abstract String getStringSeparator();

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
     * Get the addresses of this IP's children - more specific IPs
     * 
     * @return addresses of this IP's children
     */
    @JsonIgnore
    protected int[][] getChildrenAdress() {
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
     * Set all bits after const prefix to zeros
     */
    protected void resetSuffix() {
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
    private int getMaxBlockValue() {
	return (1 << getBlockSize()) - 1;
    }

}
