package breakingtherules.firewall;

import java.util.Objects;

import breakingtherules.utilities.Cache;
import breakingtherules.utilities.Caches;
import breakingtherules.utilities.IntArrayWrapper;
import breakingtherules.utilities.SoftCache;

/**
 * Destination attribute.
 * <p>
 * Defined by it's IP.
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * @see IP
 */
public class Destination extends IPAttribute {

    /**
     * Destination attribute that represent 'Any' destination (contains all
     * others)
     */
    public static final Destination ANY_DESTINATION = new AnyDestination();

    /**
     * Construct new destination of an IP
     * 
     * @param ip
     *            IP of the destination
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
    public static Destination create(IP ip) {
	return createInternal(Objects.requireNonNull(ip));
    }

    /**
     * Create a destination from a string of IP
     * 
     * @param ip
     *            a string IP
     * @return destination object of the IP
     */
    public static Destination create(String ip) {
	return createInternal(IP.fromString(ip));
    }

    /**
     * Create a destination, used internally
     * 
     * @param ip
     *            an IP
     * @return destination object of the IP
     */
    private static Destination createInternal(final IP ip) {
	Destination destination;
	if (ip instanceof IPv4) {
	    final Cache<Integer, Destination> cache = DestinationCache.IPv4Cache[ip.m_maskSize];
	    // Intentionally using 'new Integer(int)' and not
	    // 'Integer.valueOf(int)'
	    final Integer addressInteger = new Integer(((IPv4) ip).m_address);
	    destination = cache.get(addressInteger);
	    if (destination == null) {
		destination = cache.add(addressInteger, new Destination(ip));
	    }

	} else if (ip instanceof IPv6) {
	    final Cache<IntArrayWrapper, Destination> cache = DestinationCache.IPv6Cache[ip.m_maskSize];
	    final IntArrayWrapper key = new IntArrayWrapper(((IPv6) ip).m_address);
	    destination = cache.get(key);
	    if (destination == null) {
		destination = cache.add(key, new Destination(ip));
	    }

	} else {
	    destination = new Destination(ip);
	}
	return destination;
    }

    /**
     * Cache of {@link Destination} objects
     * 
     * @author Barak Ugav
     * @author Yishai Gronich
     *
     */
    private static class DestinationCache {

	/**
	 * Cache of destination objects with IPv4 IPs
	 */
	static final Cache<Integer, Destination>[] IPv4Cache;

	/**
	 * Cache of destination objects with IPv6 IPs
	 */
	static final Cache<IntArrayWrapper, Destination>[] IPv6Cache;

	static {
	    // Used dummy to suppress warnings
	    @SuppressWarnings({ "unchecked", "unused" })
	    Object dummy1 = IPv4Cache = new Cache[IPv4.SIZE + 1];

	    // Used dummy to suppress warnings
	    @SuppressWarnings({ "unchecked", "unused" })
	    Object dummy2 = IPv6Cache = new Cache[IPv6.SIZE + 1];

	    for (int i = IPv4Cache.length; i-- != 0;)
		IPv4Cache[i] = Caches.synchronizedCache(new SoftCache<>());

	    for (int i = IPv6Cache.length; i-- != 0;)
		IPv6Cache[i] = Caches.synchronizedCache(new SoftCache<>());
	}

    }

    /**
     * Any destination - contains all others.
     * 
     * @author Barak Ugav
     * @author Yishai Gronich
     * @see IP#ANY_IP
     *
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
