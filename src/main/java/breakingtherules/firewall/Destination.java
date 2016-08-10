package breakingtherules.firewall;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;

import breakingtherules.util.Int2ObjectCache;
import breakingtherules.util.Int2ObjectOpenAddressingHashCache;
import breakingtherules.util.Object2ObjectCache;
import breakingtherules.util.Object2ObjectCustomBucketHashCache;

/**
 * Destination attribute.
 * <p>
 * Defined by it's IP.
 * <p>
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @see IP
 */
public class Destination extends IPAttribute {

    /**
     * Destination attribute that represent 'Any' destination (contains all
     * others)
     */
    public static final Destination ANY_DESTINATION = new AnyDestination();

    /**
     * Construct new destination of an IP.
     *
     * @param ip
     *            IP of the destination.
     */
    private Destination(final IP ip) {
	super(ip);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final Attribute other) {
	return other instanceof Destination && super.contains(other);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Destination createMutation(final IP ip) {
	return Destination.valueOf(ip);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
	return o instanceof Destination && super.equals(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributeType getType() {
	return AttributeType.DESTINATION;
    }

    /**
     * Get Destination object with the specified IP.
     *
     * @param ip
     *            an IP
     * @return destination object of the IP
     */
    public static Destination valueOf(final IP ip) {
	return new Destination(Objects.requireNonNull(ip));
    }

    /**
     * Get Destination object parsed from string.
     *
     * TODO - specified expected input format.
     *
     * @param s
     *            string representation of a destination.
     * @return destination object of the IP
     */
    public static Destination valueOf(final String s) {
	return new Destination(IP.valueOf(s));
    }

    /**
     * Cache of {@link Destination} objects.
     *
     * @author Barak Ugav
     * @author Yishai Gronich
     *
     * @see Cache
     */
    public static final class Cache {

	/**
	 * The IP objects cached used by this cache.
	 */
	private final IP.Cache ipsCache;

	/**
	 * Cache of destination objects with IPv4 addresses.
	 */
	private final Int2ObjectCache<Destination> ipv4Cache;

	/**
	 * Cache of destination objects with IPv6 addresses.
	 */
	private final Object2ObjectCache<int[], Destination> ipv6Cache;

	/**
	 * The mapping function of destination object with IPv4 addresses.
	 */
	private final IntFunction<Destination> ipv4DestinationsMappingFunction;

	/**
	 * The mapping function of destination object with IPv6 addresses.
	 */
	private final Function<int[], Destination> ipv6DestinationsMappingFunction;

	/**
	 * Construct new destination cache.
	 * <p>
	 * If an IP cache exists, the
	 * {@link Destination.Cache#Cache(breakingtherules.firewall.IP.Cache)}
	 * should be used.
	 */
	public Cache() {
	    this(null);
	}

	/**
	 * Construct new destination cache, built on an existing IPs cache.
	 * <p>
	 *
	 * @param ipsCache
	 *            the existing IPs cache. (can be null)
	 */
	public Cache(final IP.Cache ipsCache) {
	    this.ipsCache = ipsCache != null ? ipsCache : new IP.Cache();
	    ipv4DestinationsMappingFunction = address -> new Destination(
		    this.ipsCache.ipv4Cache.cache.getOrAdd(address, IPv4.Cache.supplier));
	    ipv6DestinationsMappingFunction = address -> new Destination(
		    this.ipsCache.ipv6Cache.cache.getOrAdd(address, IPv6.Cache.supplier));
	    ipv4Cache = new Int2ObjectOpenAddressingHashCache<>();
	    ipv6Cache = new Object2ObjectCustomBucketHashCache<>(IPv6.Cache.IPv6AddressesStrategy.INSTANCE);
	}

	public Destination add(final Destination destination) {
	    final IP ip = destination.getIp();
	    if (ip.m_maskSize == ip.getSize()) {
		// If ip is a full IP (most common destination objects) search
		// it in cache, or add one if one doesn't exist.
		if (ip instanceof IPv4)
		    return ipv4Cache.add(((IPv4) ip).m_address, destination);
		if (ip instanceof IPv6)
		    return ipv6Cache.add(((IPv6) ip).m_address, destination);
	    }
	    return destination;
	}

	public Destination valueOf(final IP ip) {
	    if (ip.m_maskSize == ip.getSize()) {
		// If ip is a full IP (most common destination objects) search
		// it in cache, or add one if one doesn't exist.
		if (ip instanceof IPv4)
		    return ipv4Cache.getOrAdd(((IPv4) ip).m_address, ipv4DestinationsMappingFunction);
		if (ip instanceof IPv6)
		    return ipv6Cache.getOrAdd(((IPv6) ip).m_address, ipv6DestinationsMappingFunction);
	    }
	    return new Destination(ip);
	}

	public Destination valueOf(final String s) {
	    return valueOf(IP.valueOf(s));
	}

    }

    /**
     * Any destination - contains all others.
     *
     * @author Barak Ugav
     * @author Yishai Gronich
     *
     * @see IP#ANY_IP
     */
    private static class AnyDestination extends Destination {

	/**
	 * Construct new AnyDestination. Called once.
	 */
	AnyDestination() {
	    super(IP.ANY_IP);
	}

	/**
	 * Contains all destinations.
	 */
	@Override
	public boolean contains(final Attribute other) {
	    return other instanceof Destination;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object o) {
	    return o instanceof AnyDestination || super.equals(o);
	}

    }

}
