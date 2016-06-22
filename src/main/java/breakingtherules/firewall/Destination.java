package breakingtherules.firewall;

import java.util.Objects;
import java.util.function.Function;

import breakingtherules.utilities.Cache;
import breakingtherules.utilities.Caches;
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
	return Destination.valueOf(ip);
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
	return valueOfInternal(IP.valueOf(s));
    }

    /**
     * Get Destination object with the specified IP.
     * 
     * @param ip
     *            an IP
     * @return destination object of the IP
     */
    public static Destination valueOf(final IP ip) {
	return valueOfInternal(Objects.requireNonNull(ip));
    }

    /**
     * Get Destination object with the specified IP, used internally
     * 
     * @param ip
     *            an IP
     * @return destination object of the IP
     */
    private static Destination valueOfInternal(final IP ip) {
	if (ip.m_maskSize == ip.getSize()) {
	    // If ip is a full IP (most common destination objects) search it in
	    // cache, or add one if one doesn't exist.
	    if (ip instanceof IPv4) {
		return DestinationCache.IPv4Cache.getOrAdd((IPv4) ip, DestinationCache.supplier);
	    }
	    if (ip instanceof IPv6) {
		return DestinationCache.IPv6Cache.getOrAdd((IPv6) ip, DestinationCache.supplier);
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
    private static class DestinationCache {

	/**
	 * Cache of destination objects with full(not subnetwork) IPv4 ips.
	 */
	static final Cache<IPv4, Destination> IPv4Cache;

	/**
	 * Cache of destination objects with full(not subnetwork) IPv6 ips.
	 */
	static final Cache<IPv6, Destination> IPv6Cache;

	/**
	 * Supplier used to supply new destination objects if need to the cache
	 * in case they are missing.
	 * <p>
	 * Used when using {@link Cache#getOrAdd(Object, Function)}.
	 */
	static final Function<IP, Destination> supplier;

	static {
	    IPv4Cache = Caches.synchronizedCache(new SoftCustomHashCache<>(IPv4AddressStrategy.INSTANCE));
	    IPv6Cache = Caches.synchronizedCache(new SoftCustomHashCache<>(IPv6AddressStrategy.INSTANCE));
	    supplier = ip -> new Destination(ip);
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
