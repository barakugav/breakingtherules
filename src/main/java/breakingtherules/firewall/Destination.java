package breakingtherules.firewall;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;

import breakingtherules.utilities.Int2ObjectCache;
import breakingtherules.utilities.Int2ObjectSoftHashCache;
import breakingtherules.utilities.SoftCustomHashCache;

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
    public String getType() {
	return DESTINATION_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTypeId() {
	return DESTINATION_TYPE_ID;
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
    public Destination createMutation(final IP ip) {
	return Destination.valueOf(ip, null);
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
	return new Destination(IP.valueOf(s, null));
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
    public static Destination valueOf(final String s, final Destination.Cache cache) {
	return valueOf(IP.valueOf(s, null), cache);
    }

    public static Destination valueOf(final IP ip) {
	return new Destination(Objects.requireNonNull(ip));
    }

    /**
     * Get Destination object with the specified IP.
     * 
     * @param ip
     *            an IP
     * @return destination object of the IP
     */
    public static Destination valueOf(final IP ip, final Destination.Cache cache) {
	if (cache != null && ip.m_maskSize == ip.getSize()) {
	    // If ip is a full IP (most common destination objects) search it in
	    // cache, or add one if one doesn't exist.
	    if (ip instanceof IPv4) {
		return cache.IPv4Cache.getOrAdd(((IPv4) ip).m_address, cache.ipv4DestinationsMappingFunction);
	    }
	    if (ip instanceof IPv6) {
		return cache.IPv6Cache.getOrAdd(((IPv6) ip).m_address, cache.ipv6DestinationsMappingFunction);
	    }
	}
	return new Destination(ip);
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

	private final IP.Cache ipsCache;

	/**
	 * Cache of destination objects with full(not subnetwork) IPv4 ips.
	 */
	private final Int2ObjectCache<Destination> IPv4Cache;

	/**
	 * Cache of destination objects with full(not subnetwork) IPv6 ips.
	 */
	private final breakingtherules.utilities.Cache<int[], Destination> IPv6Cache;

	private final IntFunction<Destination> ipv4DestinationsMappingFunction;

	private final Function<int[], Destination> ipv6DestinationsMappingFunction;

	public Cache(final IP.Cache ipsCache) {
	    this.ipsCache = ipsCache != null ? ipsCache : new IP.Cache();
	    ipv4DestinationsMappingFunction = address -> new Destination(
		    ipsCache.ipv4Cache.cache.getOrAdd(address, IPv4.Cache.supplier));
	    ipv6DestinationsMappingFunction = address -> new Destination(
		    ipsCache.ipv6Cache.cache.getOrAdd(address, IPv6.Cache.supplier));
	    IPv4Cache = new Int2ObjectSoftHashCache<>();
	    IPv6Cache = new SoftCustomHashCache<>(IPv6.Cache.IPv6AddressesStrategy.INSTANCE);
	}

	public IP.Cache getIPsCache() {
	    return ipsCache;
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
