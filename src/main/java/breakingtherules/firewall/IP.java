package breakingtherules.firewall;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * IP address, can be {@link IPv4} or {@link IPv6}.
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * 
 */
public abstract class IP implements Comparable<IP> {

    /**
     * Any IP, contains all other
     */
    public static final IP ANY_IP = new AnyIP();

    /**
     * Size of the subnetwork mask
     */
    final int m_maskSize;

    /**
     * String representation of any IP
     */
    static final String ANY = "Any";

    /**
     * The mask size separator in the string representation of the IP.
     */
    static final char MASK_SIZE_SEPARATOR = '/';

    /**
     * Construct new IP
     * 
     * @param maskSize
     *            size of subnetwork mask
     */
    IP(final int maskSize) {
	m_maskSize = maskSize;
    }

    /**
     * Get the address of the IP
     * 
     * @return address of the IP
     */
    public abstract int[] getAddress();

    /**
     * Get the size of the sub network of this IP.
     * <p>
     * Should be <code>SIZE - maskSize</code>.
     * 
     * @return this IP's network size
     */
    @JsonIgnore
    public abstract int getSubnetBitsNum();

    /**
     * Get the size of the subnetwork mask.
     * <p>
     * Should be <code>SIZE</code> for non-subnetworks.
     * 
     * @return size of the subnetwork mask.
     */
    public int getMaskSize() {
	return m_maskSize;
    }

    /**
     * Checks if this IP has parent (more general IP, subnetwork that contains
     * this IP and it's subnetwork mask is smaller then this one's mask by 1).
     * 
     * @return true if this IP has parent, else - false
     * @see #getParent()
     */
    public boolean hasParent() {
	return m_maskSize > 0;
    }

    /**
     * Get this IP's parent (more general IP, subnetwork that contains this IP
     * and it's subnetwork mask is smaller then this one by 1).
     * <p>
     * For example:<br>
     * IP: 101.54.17.0/24<br>
     * Parent: 101.54.16.0/23<br>
     * 
     * @return this IP's parent
     * @throws IllegalStateException
     *             if the IP doesn't have a parent.
     */
    @JsonIgnore
    public abstract IP getParent();

    /**
     * Checks if this IP has children (more specific IPs, 2 IPs that this IP's
     * contains and their subnetwork mask is greater then this one by 1).
     * <p>
     * 
     * @return true if this IP has children. else - false
     * @see #getChildren()
     */
    public abstract boolean hasChildren();

    /**
     * Get this IP's children (more specific IPs, 2 IPs that this IP's contains
     * and their subnetwork mask is greater then this one by 1).
     * <p>
     * For example:<br>
     * IP: 101.54.16.0/23<br>
     * Children: 101.54.16.0/24, 101.54.17.0/24<br>
     * 
     * @return this IP's children
     * @throws IllegalStateException
     *             if the IP doesn't have children
     */
    @JsonIgnore
    public abstract IP[] getChildren();

    /**
     * Checks if this IP (sub-network) contain other IP.
     * <p>
     * By definition, this contains itself.
     * <p>
     * For example:<br>
     * IP: 101.54.17.0/24<br>
     * Contains: 101.54.17.87, 101.54.17.64/26, 101.54.17.0/24<br>
     * Not contains: 201.54.17.87, 101.54.0.0/16, 101.54.16.0/24<br>
     * 
     * @param other
     *            other IP to compare to
     * @return true if this IP contain in his sub-network the other IP
     */
    public abstract boolean contains(IP other);

    /**
     * Get the value of specific bit in the IP.
     * <p>
     * 
     * @param bitNumber
     *            the bit's number
     * @return value of the requested bit
     * @throws IndexOutOfBoundsException
     *             if the bit number is negative or greater then the IP size.
     */
    @JsonIgnore
    public abstract boolean getBit(int bitNumber);

    /**
     * Get the last masked bit value.
     * <p>
     * For example:<br>
     * IP: 101.54.17/24 lastBit: 1
     * IP: 101.54.16/25 lastBit: 0
     * <p>
     * This method could have been implemented in this class and not to be
     * abstract by: <br>
     * <code>getBit(getMaskSize());</code><br>
     * But by not implementing this, we encourage subclasses of this class to
     * implement specific implementation for last bit for performance. (If the
     * method is specific for last bit, less checks can be made and the bit
     * number is known - for example {@link IPv6#getLastBit()} compare to
     * {@link IPv6#getBit(int)}).
     * 
     * @return true if the last bit value is 1, else false
     */
    @JsonIgnore
    public abstract boolean getLastBit();

    /**
     * Checks if another IP is a brother of this IP.
     * <p>
     * IP brothers are IP of the same type that are equals except last bit.
     * <p>
     * For example:<br>
     * 127.0.0.1 and 127.0.0.0<br>
     * or 10.69.0.0/16 and 10.68.0.0/16<br>
     * 
     * @param other
     *            potential brother
     * @return true if the other IP is brother of this IP, else - false
     */
    public abstract boolean isBrother(IP other);

    /**
     * Get the size of the IP, the number of bits of it.
     * 
     * @return size of the IP.
     */
    public abstract int getSize();

    /**
     * Create new IP from String IP
     * <p>
     * This method detect formats of IPv4 and IPv6 only.
     * <p>
     * 
     * @param ip
     *            String IP
     * @return IP object based on the String IP
     * @throws NullPointerException
     *             if the string is null
     * @throws IllegalArgumentException
     *             if the string is invalid.
     */
    public static IP fromString(final String ip) {
	if (ip.equals("Any")) {
	    return ANY_IP;
	}

	final boolean isIPv4 = ip.indexOf(IPv4.BLOCKS_SEPARATOR) >= 0;
	final boolean isIPv6 = ip.indexOf(IPv6.BLOCKS_SEPARATOR) >= 0;
	if (isIPv4 && isIPv6) {
	    throw new IllegalArgumentException("Unknown format");
	} else if (isIPv4) {
	    return IPv4.createFromString(ip);
	} else if (isIPv6) {
	    return IPv6.createFromString(ip);
	} else {
	    throw new IllegalArgumentException("Unknown format: " + ip);
	}
    }

    /**
     * Create new IP from boolean bits list.
     * <p>
     * This method create IPs of type IPv4, IPv6 and AnyIP only.
     * <p>
     * 
     * @param ip
     *            bits of the IP
     * @param clazz
     *            class of the requested IP - IPv4, IPv6 or AnyIP
     * @return IP object based on the boolean bits
     * @throws IllegalArgumentException
     *             if the given class is not IPv4, IPv6 or AnyIP, or if the bits
     *             list is invalid.
     */
    public static IP fromBooleans(final List<Boolean> ip, final Class<?> clazz) {
	if (clazz.equals(IPv4.class)) {
	    return IPv4.createFromBits(ip);
	} else if (clazz.equals(IPv6.class)) {
	    return IPv6.createFromBits(ip);
	} else if (clazz.equals(AnyIP.class)) {
	    return ANY_IP;
	} else {
	    throw new IllegalArgumentException(
		    "Choosen class in unkwon. Expected IPv4, IPv6 or AnyIP. Actual: " + clazz.getSimpleName());
	}
    }

    /**
     * The AnyIP class represents 'Any' IP (contains all others).
     * 
     * @author Barak Ugav
     * @author Yishai Gronich
     * 
     */
    private static class AnyIP extends IP {

	/**
	 * Construct new AnyIP. Called once.
	 */
	AnyIP() {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see breakingtherules.firewall.IP#getChildren()
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see breakingtherules.firewall.IP#getSize()
	 */
	@Override
	public int getSize() {
	    return 0;
	}

    }

}
