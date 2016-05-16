package breakingtherules.firewall;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * IP address, can be {@link IPv4} or {@link IPv6}
 */
public abstract class IP implements Comparable<IP> {

    /**
     * Length of the constant prefix
     */
    protected final int m_prefixLength;

    /**
     * String representation of any IP
     */
    protected static final String ANY = "Any";

    /**
     * The prefix length separator
     */
    protected static final char PREFIX_LENGTH_SEPARATOR = '/';

    /**
     * Construct new IP
     * 
     * @param prefixLength
     *            length of constant prefix
     */
    protected IP(final int prefixLength) {
	m_prefixLength = prefixLength;
    }

    /**
     * Get the address of the IP
     * 
     * @return address of the IP
     */
    public abstract int[] getAddress();

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
    public abstract int getSubnetBitsNum();

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
    public abstract boolean hasChildren();

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
    public abstract boolean contains(IP other);

    /**
     * Create new IP from String IP
     * 
     * Detect format - IPv4 or IPv6
     * 
     * @param ip
     *            String IP
     * @return IP object based on the String IP
     */
    public static IP fromString(final String ip) {
	if (ip.equals("Any")) {
	    return getAnyIP();
	}

	// IPv4 format
	final boolean isIPv4 = ip.indexOf(IPv4.BLOCKS_SEPARATOR) >= 0;
	final boolean isIPv6 = ip.indexOf(IPv6.BLOCKS_SEPARATOR) >= 0;
	if (isIPv4 && isIPv6) {
	    throw new IllegalArgumentException("Unknown format");
	} else if (isIPv4) {
	    return IPv4.create(ip);
	} else if (isIPv6) {
	    return IPv6.create(ip);
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
    public static IP fromBooleans(final boolean[] ip, final Class<?> clazz) {
	if (clazz.equals(IPv4.class)) {
	    return IPv4.create(ip);
	} else if (clazz.equals(IPv6.class)) {
	    return IPv6.create(ip);
	} else if (clazz.equals(AnyIP.class)) {
	    return AnyIP.instance;
	} else {
	    throw new IllegalArgumentException(
		    "Choosen class in unkwon. Expected IPv4, IPv6 or AnyIP. Actual: " + clazz.getSimpleName());
	}
    }

    /**
     * Get the value of specific bit in the IP
     * 
     * @param bitNumber
     *            the bit's number
     * @return value of the requested bit
     */
    @JsonIgnore
    public abstract boolean getBit(int bitNumber);

    /**
     * Get the last bit value.
     * <p>
     * This method could have been implemented in this class and not to be
     * abstract by: <br>
     * <code>getBit(m_prefixLength);</code><br>
     * But by not implementing this, we encourage subclasses of this class to
     * implement specific implementation for last bit for performance. (If the
     * method is specific for last bit, less checks can be made and the bit
     * number is known - for example {@link IPv6#getLastBit()} compare to
     * {@link IPv6#getBit(int)}).
     * 
     * @return true if the last bit value is 1, else false
     * 
     */
    @JsonIgnore
    public abstract boolean getLastBit();

    /**
     * Checks if another IP is a brother of this IP.
     * <p>
     * IP brothers are IP of the same type that are equals except last bit. For
     * example: 127.0.0.1 and 127.0.0.0, or 10.69.0.0/16 and 10.68.0.0/16
     * 
     * @param other
     *            potential brother
     * @return true if the other IP is brother of this IP, else - false
     */
    public abstract boolean isBrother(IP other);

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
     * The AnyIP class represents 'Any' IP (contains all others). This class is
     * singleton
     */
    private static class AnyIP extends IP {

	private static final AnyIP instance = new AnyIP();

	private AnyIP() {
	    super(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see breakingtherules.firewall.IP#hasParent()
	 */
	@Override
	public boolean hasParent() {
	    return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see breakingtherules.firewall.IP#getParent()
	 */
	@Override
	public IP getParent() {
	    return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see breakingtherules.firewall.IP#hasChildren()
	 */
	@Override
	public boolean hasChildren() {
	    return false;
	}

	@Override
	public IP[] getChildren() {
	    return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see breakingtherules.firewall.IP#getLastBit()
	 */
	@Override
	public boolean getLastBit() {
	    return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * breakingtherules.firewall.IP#contains(breakingtherules.firewall.IP)
	 */
	@Override
	public boolean contains(IP other) {
	    return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
	    return o instanceof AnyIP;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
	    return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
	    return ANY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(IP o) {
	    return o instanceof AnyIP ? 0 : 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see breakingtherules.firewall.IP#getAddress()
	 */
	@Override
	public int[] getAddress() {
	    return new int[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see breakingtherules.firewall.IP#getBit(int)
	 */
	@Override
	public boolean getBit(int bitNumber) {
	    return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * breakingtherules.firewall.IP#isBrother(breakingtherules.firewall.IP)
	 */
	@Override
	public boolean isBrother(IP other) {
	    return other instanceof AnyIP;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see breakingtherules.firewall.IP#getSubnetBitsNum()
	 */
	@Override
	public int getSubnetBitsNum() {
	    return 0;
	}

    }

}
