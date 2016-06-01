package breakingtherules.firewall;

import java.util.Objects;

import breakingtherules.utilities.WeakCache;

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

    public static void refreshCache() {
	for (WeakCache<Integer, Destination> cache : DestinationCache.IPv4cache)
	    cache.cleanCache();
    }

    public static Destination create(IP ip) {
	return createInternal(Objects.requireNonNull(ip));
    }

    public static Destination create(String ip) {
	return createInternal(IP.fromString(ip));
    }

    private static Destination createInternal(IP ip) {
	Destination destination;
	if (ip instanceof IPv4) {
	    WeakCache<Integer, Destination> cache = DestinationCache.IPv4cache[ip.prefixLength];
	    Integer addressInteger = new Integer(((IPv4) ip).address());
	    destination = cache.get(addressInteger);
	    if (destination == null) {
		destination = new Destination(ip);
		cache.add(addressInteger, destination);
	    }
	} else {
	    destination = new Destination(ip);
	}
	return destination;
    }

    private static class DestinationCache {

	static final WeakCache<Integer, Destination>[] IPv4cache;

	static {
	    @SuppressWarnings({ "unchecked", "unused" })
	    Object dummy = IPv4cache = new WeakCache[IPv4.MAX_LENGTH + 1];

	    for (int i = IPv4cache.length; i-- != 0;)
		IPv4cache[i] = new WeakCache<>();
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
