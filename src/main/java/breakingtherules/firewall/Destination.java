package breakingtherules.firewall;

import java.util.Objects;
import java.util.function.Function;

import breakingtherules.utilities.Cache;
import breakingtherules.utilities.Caches;
import breakingtherules.utilities.Caches.CacheSupplierPair;
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * breakingtherules.firewall.IPAttribute#contains(breakingtherules.firewall.
     * Attribute)
     */
    @Override
    public boolean contains(final Attribute other) {
	return other instanceof Destination && super.contains(other);
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.Attribute#getType()
     */
    @Override
    public String getType() {
	return DESTINATION_TYPE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.Attribute#getTypeId()
     */
    @Override
    public int getTypeId() {
	return DESTINATION_TYPE_ID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IPAttribute#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object o) {
	return o instanceof Destination && super.equals(o);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * breakingtherules.firewall.IPAttribute#createMutation(breakingtherules.
     * firewall.IP)
     */
    @Override
    public Destination createMutation(final IP ip) {
	return Destination.create(ip);
    }

    /**
     * Create a destination from an IP
     * 
     * @param ip
     *            an IP
     * @return destination object of the IP
     */
    public static Destination create(final IP ip) {
	return createInternal(Objects.requireNonNull(ip));
    }

    /**
     * Create a destination from a string of IP
     * 
     * @param ip
     *            a string IP
     * @return destination object of the IP
     */
    public static Destination createFromString(final String ip) {
	return createInternal(IP.createFromString(ip));
    }

    /**
     * Create a destination, used internally
     * 
     * @param ip
     *            an IP
     * @return destination object of the IP
     */
    private static Destination createInternal(final IP ip) {
	if (ip.m_maskSize == ip.getSize()) {
	    if (ip instanceof IPv4) {
		return DestinationCache.IPv4Cache.getOrAdd((IPv4) ip);
	    } else if (ip instanceof IPv6) {
		return DestinationCache.IPv6Cache.getOrAdd((IPv6) ip);
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
     */
    private static class DestinationCache {

	static final CacheSupplierPair<IPv4, Destination> IPv4Cache;

	static final CacheSupplierPair<IPv6, Destination> IPv6Cache;

	static {
	    final Function<IP, Destination> supplier = ip -> new Destination(ip);
	    final Cache<IPv4, Destination> cache4 = Caches
		    .synchronizedCache(new SoftCustomHashCache<>(IPv4AddressStrategy.INSTANCE));
	    final Cache<IPv6, Destination> cache6 = Caches
		    .synchronizedCache(new SoftCustomHashCache<>(IPv6AddressStrategy.INSTANCE));

	    IPv4Cache = Caches.cacheSupplierPair(cache4, supplier);
	    IPv6Cache = Caches.cacheSupplierPair(cache6, supplier);
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
	public boolean contains(Attribute other) {
	    return other instanceof Destination;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see breakingtherules.firewall.Destination#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
	    return o instanceof AnyDestination || super.equals(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see breakingtherules.firewall.IPAttribute#toString()
	 */
	@Override
	public String toString() {
	    return "Any";
	}

    }

}
