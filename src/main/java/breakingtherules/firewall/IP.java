package breakingtherules.firewall;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * IP address, can be {@link IPv4} or {@link IPv6}.
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @see IPv4
 * @see IPv6
 * @see IPAttribute
 */
public abstract class IP implements Comparable<IP> {

    /**
     * Size of the subnetwork mask.
     */
    final short m_maskSize;

    /**
     * Any IP, contains all others.
     */
    public static final IP ANY_IP = new AnyIP();

    /**
     * The mask size separator in the string representation of the IP.
     */
    static final char MASK_SIZE_SEPARATOR = '/';

    /**
     * String representation of any IP.
     */
    private static final String ANY_IP_STR = "Any";

    /**
     * Construct new IP.
     *
     * @param maskSize
     *            size of subnetwork mask.
     */
    IP(final short maskSize) {
	m_maskSize = maskSize;
    }

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
     * Get the address of the IP, in blocks format.
     * <p>
     * This method should be used as a user friendly method. The method return
     * an array of the 'blocks' of the IP.
     * <p>
     * for example:<br>
     * If the IP is 102.54.87.89 the method will return [102, 54, 87, 89].<br>
     * If the IP is 10842:54:8557:89:0:40045:84:999 the method will return
     * [10842, 54, 8557, 89, 0, 40045, 84, 999].
     * <p>
     * To get the actual bits of the address, in the minimal time and space, use
     * {@link #getAddressBits()}.
     * <p>
     *
     * @return address of the IP, in blocks format.
     */
    public abstract int[] getAddress();

    /**
     * Get the address bits of the IP, in the minimal size of int array.
     * <p>
     * This method is more technical and less user friendly then
     * {@link #getAddress()}. The method return array of int, each int is 32
     * bits, each cell in the array represent another 32 bits of the address
     * (the array is as minimal as possible).
     * <p>
     * For example:<br>
     * If the IP is 102.54.87.89 the method will return [1714837337].<br>
     * If the IP is 10842:54:8557:89:0:40045:84:999 the method will return
     * [710541366, 560791641, 40045, 5506023].
     * <p>
     * For more user friendly address use {@link #getAddress()}.
     * <p>
     *
     * @return array of the bits of the address in minimal space.
     */
    public abstract int[] getAddressBits();

    /**
     * Get the value of specific bit in the IP.
     * <p>
     *
     * @param bitNumber
     *            the bit's number.
     * @return value of the requested bit.
     * @throws IndexOutOfBoundsException
     *             if the bit number is negative or greater then the IP size.
     */
    @JsonIgnore
    public abstract boolean getBit(int bitNumber);

    /**
     * Get this IP's children (more specific IPs, 2 IPs that this IP's contains
     * and their subnetwork mask is greater then this one by 1).
     * <p>
     * For example:<br>
     * IP: 101.54.16.0/23<br>
     * Children: 101.54.16.0/24, 101.54.17.0/24<br>
     * <p>
     *
     * @return this IP's children
     * @throws IllegalStateException
     *             if the IP doesn't have children (by {@link #hasChildren()}).
     */
    @JsonIgnore
    public abstract IP[] getChildren();

    /**
     * Get the last masked bit value.
     * <p>
     * For example:<br>
     * IP: 101.54.17/24 lastBit: 1<br>
     * IP: 101.54.16/25 lastBit: 0<br>
     * <p>
     * This method could have been implemented in this class and not to be
     * abstract by: <br>
     * <code>getBit(getMaskSize());</code><br>
     * But by not implementing this, we encourage subclasses of this class to
     * implement specific implementation for last bit for performance. (If the
     * method is specific for last bit, less checks can be made and the bit
     * number is known - for example {@link IPv6#getLastBit()} compare to
     * {@link IPv6#getBit(int)}).
     * <p>
     *
     * @return true if the last bit value is 1, else false
     */
    @JsonIgnore
    public abstract boolean getLastBit();

    /**
     * Get the size of the subnetwork mask.
     * <p>
     * Should be <code>SIZE</code> for non-subnetworks.
     *
     * @return size of the subnetwork mask.
     */
    public short getMaskSize() {
	return m_maskSize;
    }

    /**
     * Get this IP's parent (more general IP, subnetwork that contains this IP
     * and it's subnetwork mask is smaller then this one by 1).
     * <p>
     * For example:<br>
     * IP: 101.54.17.0/24<br>
     * Parent: 101.54.16.0/23<br>
     * <p>
     *
     * @return this IP's parent.
     * @throws IllegalStateException
     *             if the IP doesn't have a parent (by {@link #hasParent()}).
     */
    @JsonIgnore
    public abstract IP getParent();

    /**
     * Get the size of the IP, the number of bits of it.
     *
     * @return size of the IP.
     */
    public abstract short getSize();

    /**
     * Get the size of the sub network of this IP.
     * <p>
     * Should be <code>SIZE - maskSize</code>.
     *
     * @return this IP's network size.
     */
    @JsonIgnore
    public abstract int getSubnetBitsNum();

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
     * Checks if this IP has parent (more general IP, subnetwork that contains
     * this IP and it's subnetwork mask is smaller then this one's mask by 1).
     * <p>
     *
     * @return true if this IP has parent, else - false
     * @see #getParent()
     */
    public boolean hasParent() {
	return m_maskSize > 0;
    }

    /**
     * Checks if another IP is a brother of this IP.
     * <p>
     * IP brothers are IP of the same type that are equals except last bit.
     * <p>
     * For example:<br>
     * 127.0.0.1 and 127.0.0.0<br>
     * or 10.69.0.0/16 and 10.68.0.0/16<br>
     * <p>
     *
     * @param other
     *            potential brother
     * @return true if the other IP is brother of this IP, else - false
     */
    public abstract boolean isBrother(IP other);

    /**
     * Parses an IP from bits list.
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
    public static IP parseIPFromBits(final List<Boolean> ip, final Class<?> clazz) {
	return parseIPFromBits(ip, clazz, null);
    }

    /**
     * Parses an IP from bits list.
     * <p>
     * This method create IPs of type IPv4, IPv6 and AnyIP only.
     * <p>
     * If the cache isn't null, will used the cached IP from the cache if one
     * exist, or will create a new one and cache it to the cache otherwise.
     * <p>
     *
     * @param ip
     *            bits of the IP.
     * @param clazz
     *            class of the requested IP - IPv4, IPv6 or AnyIP.
     * @param cache
     *            the cached containing cached IPs objects. Can be null.
     * @return IP object based on the boolean bits
     * @throws IllegalArgumentException
     *             if the given class is not IPv4, IPv6 or AnyIP, or if the bits
     *             list is invalid.
     */
    public static IP parseIPFromBits(final List<Boolean> ip, final Class<?> clazz, final IP.Cache cache) {

	// TODO - remove this method

	if (clazz.equals(IPv4.class))
	    return IPv4.parseIPv4FromBits(ip, cache != null ? cache.ipv4Cache : null);
	if (clazz.equals(IPv6.class))
	    return IPv6.parseIPv6FromBits(ip, cache != null ? cache.ipv6Cache : null);
	if (clazz.equals(AnyIP.class))
	    return ANY_IP;
	throw new IllegalArgumentException(
		"Choosen class in unkwon. Expected IPv4, IPv6 or AnyIP. Actual: " + clazz.getSimpleName());
    }

    /**
     * Get IP object parsed from string.
     * <p>
     * This method detect formats of IPv4 and IPv6 only.
     * <p>
     *
     * @param s
     *            string representation of an IP.
     * @return IP object based on the String IP
     * @throws NullPointerException
     *             if the string is null.
     * @throws IllegalArgumentException
     *             if the string is invalid.
     */
    public static IP valueOf(final String s) {
	return valueOf(s, null);
    }

    /**
     * Get IP object parsed from string.
     * <p>
     * This method detect formats of IPv4 and IPv6 only.
     * <p>
     * If the cache isn't null, will used the cached IP from the cache if one
     * exist, or will create a new one and cache it to the cache otherwise.
     * <p>
     *
     * @param s
     *            string representation of an IP.
     * @param cache
     *            the cached containing cached IPs objects. Can be null.
     * @return IP object based on the String IP
     * @throws NullPointerException
     *             if the string is null.
     * @throws IllegalArgumentException
     *             if the string is invalid.
     */
    public static IP valueOf(final String s, final IP.Cache cache) {
	int separator;
	if ((separator = s.indexOf(IPv4.BLOCKS_SEPARATOR)) >= 0)
	    return IPv4.valueOf(s, cache != null ? cache.ipv4Cache : null, separator);
	if ((separator = s.indexOf(IPv6.BLOCKS_SEPARATOR)) >= 0)
	    return IPv6.valueOf(s, cache != null ? cache.ipv6Cache : null, separator);
	if (s.startsWith(ANY_IP_STR)) {
	    // Start with 'Any'

	    if (s.length() == 7 && s.startsWith("IPv", 3)) {
		// Equals to 'AnyIPv_'
		if (s.charAt(6) == '4')
		    // Equals to 'AnyIPv4'
		    return IPv4.ANY_IPv4;
		if (s.charAt(6) == '6')
		    // Equals to 'AnyIPv6'
		    return IPv6.ANY_IPv6;
	    }
	    if (s.length() == 3)
		// Equals to 'Any'
		return ANY_IP;
	}
	throw new IllegalArgumentException("Unknown format: " + s);

    }

    /**
     * Cache of {@link IP} objects.
     *
     * @author Barak Ugav
     * @author Yishai Gronich
     *
     * @see IPv4.Cache
     * @see IPv6.Cache
     */
    public static final class Cache {

	/**
	 * Cache of {@link IPv4} objects.
	 */
	final IPv4.Cache ipv4Cache;

	/**
	 * Cache of {@link IPv6} objects.
	 */
	final IPv6.Cache ipv6Cache;

	/**
	 * Construct new IPs cache.
	 */
	public Cache() {
	    ipv4Cache = new IPv4.Cache();
	    ipv6Cache = new IPv6.Cache();
	}

    }

    /**
     * The AnyIP class represents 'Any' IP (contains all others).
     * <p>
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
	    super((short) 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(final IP o) {
	    return o instanceof AnyIP ? 0 : 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(final IP other) {
	    return other != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object o) {
	    return o instanceof AnyIP;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int[] getAddress() {
	    return new int[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int[] getAddressBits() {
	    return new int[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean getBit(final int bitNumber) {
	    return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IP[] getChildren() {
	    throw new IllegalStateException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean getLastBit() {
	    return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IP getParent() {
	    throw new IllegalStateException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public short getSize() {
	    return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSubnetBitsNum() {
	    return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasChildren() {
	    return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
	    return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasParent() {
	    return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isBrother(final IP other) {
	    return other instanceof AnyIP;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
	    return ANY_IP_STR;
	}

    }

}
