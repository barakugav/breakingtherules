package breakingtherules.firewall;

import java.util.Objects;

import breakingtherules.firewall.IPv6.IPv6Cache.IPv6CacheKey;
import breakingtherules.utilities.Cache;
import breakingtherules.utilities.Caches;
import breakingtherules.utilities.SoftCache;

/**
 * Destination attribute
 */
public class Destination extends IPAttribute {

    /**
     * Destination attribute that represent 'Any' destination (contains all
     * others)
     */
    public static final Destination ANY_DESTINATION = new AnyDestination();

    /**
     * Constructor
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

    /**
     * Use the <code>IPAttribute.contains</code> and a check that the other
     * attribute is a destination attribute
     */
    @Override
    public boolean contains(final Attribute other) {
	return other instanceof Destination && super.contains(other);
    }

    /**
     * Use the <code>IPAttribute.equals</code> and a check that the other
     * attribute is a destination attribute
     */
    @Override
    public boolean equals(final Object o) {
	return o instanceof Destination && super.equals(o);
    }

    @Override
    public Destination createMutation(final IP ip) {
	return Destination.create(ip);
    }

    public static Destination create(IP ip) {
	return createInternal(Objects.requireNonNull(ip));
    }

    public static Destination create(String ip) {
	return createInternal(IP.fromString(ip));
    }

    private static Destination createInternal(final IP ip) {
	Destination destination;
	if (ip instanceof IPv4) {
	    final Cache<Integer, Destination> cache = DestinationCache.IPv4Cache[ip.prefixLength];
	    final Integer addressInteger = new Integer(((IPv4) ip).m_address);
	    destination = cache.get(addressInteger);
	    if (destination == null) {
		destination = cache.add(addressInteger, new Destination(ip));
	    }

	} else if (ip instanceof IPv6) {
	    final Cache<IPv6CacheKey, Destination> cache = DestinationCache.IPv6Cache[ip.prefixLength];
	    final IPv6CacheKey key = new IPv6CacheKey(((IPv6) ip).m_address);
	    destination = cache.get(key);
	    if (destination == null) {
		destination = cache.add(key, new Destination(ip));
	    }

	} else {
	    destination = new Destination(ip);
	}
	return destination;
    }

    private static class DestinationCache {

	static final Cache<Integer, Destination>[] IPv4Cache;

	static final Cache<IPv6CacheKey, Destination>[] IPv6Cache;

	static {
	    @SuppressWarnings({ "unchecked", "unused" })
	    Object dummy1 = IPv4Cache = new Cache[IPv4.MAX_LENGTH + 1];

	    @SuppressWarnings({ "unchecked", "unused" })
	    Object dummy2 = IPv6Cache = new Cache[IPv6.MAX_LENGTH + 1];

	    for (int i = IPv4Cache.length; i-- != 0;)
		IPv4Cache[i] = Caches.synchronizedCache(new SoftCache<>());

	    for (int i = IPv6Cache.length; i-- != 0;)
		IPv6Cache[i] = Caches.synchronizedCache(new SoftCache<>());
	}

    }

    private static class AnyDestination extends Destination {

	public AnyDestination() {
	    super(IP.ANY_IP);
	}

	@Override
	public boolean contains(Attribute other) {
	    return other instanceof Destination;
	}

	@Override
	public boolean equals(Object o) {
	    return o instanceof AnyDestination || super.equals(o);
	}

	@Override
	public String toString() {
	    return "Any";
	}

    }

}
