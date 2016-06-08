package breakingtherules.firewall;

import java.util.Objects;

import breakingtherules.firewall.IPv6.IPv6Cache.IPv6CacheKey;
import breakingtherules.utilities.Cache;
import breakingtherules.utilities.Caches;
import breakingtherules.utilities.SoftCache;

/**
 * Source attribute, represent a source IP of a hit
 */
public class Source extends IPAttribute {

    /**
     * Source attribute that represent 'Any' source (contains all others)
     */
    public static final Source ANY_SOURCE = new AnySource();

    /**
     * Constructor
     * 
     * @param ip
     *            IP of the source
     */
    private Source(final IP ip) {
	super(ip);
    }

    /**
     * Use the <code>IPAttribute.contains</code> and a check that the other
     * attribute is a source attribute
     */
    @Override
    public boolean contains(final Attribute other) {
	return other instanceof Source && super.contains(other);
    }

    /**
     * Use the <code>IPAttribute.equals</code> and a check that the other
     * attribute is a source attribute
     */
    @Override
    public boolean equals(final Object o) {
	return o instanceof Source && super.equals(o);
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.Attribute#getType()
     */
    @Override
    public String getType() {
	return SOURCE_TYPE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.Attribute#getTypeId()
     */
    @Override
    public int getTypeId() {
	return SOURCE_TYPE_ID;
    }

    @Override
    public Source createMutation(final IP ip) {
	return Source.create(ip);
    }

    public static Source create(final IP ip) {
	return createInternal(Objects.requireNonNull(ip));
    }

    public static Source create(final String ip) {
	return createInternal(IP.fromString(ip));
    }

    private static Source createInternal(final IP ip) {
	Source source;
	if (ip instanceof IPv4) {
	    final Cache<Integer, Source> cache = SourceCache.IPv4Cache[ip.prefixLength];
	    final Integer addressInteger = new Integer(((IPv4) ip).m_address);
	    source = cache.get(addressInteger);
	    if (source == null) {
		source = cache.add(addressInteger, new Source(ip));
	    }

	} else if (ip instanceof IPv6) {
	    final Cache<IPv6CacheKey, Source> cache = SourceCache.IPv6Cache[ip.prefixLength];
	    final IPv6CacheKey key = new IPv6CacheKey(((IPv6) ip).m_address);
	    source = cache.get(key);
	    if (source == null) {
		source = cache.add(key, new Source(ip));
	    }

	} else {
	    source = new Source(ip);
	}
	return source;
    }

    private static class SourceCache {

	static final Cache<Integer, Source>[] IPv4Cache;

	static final Cache<IPv6CacheKey, Source>[] IPv6Cache;

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

    private static class AnySource extends Source {

	private AnySource() {
	    super(IP.ANY_IP);
	}

	@Override
	public boolean contains(final Attribute other) {
	    return other instanceof Source;
	}

	@Override
	public boolean equals(Object o) {
	    return o instanceof AnySource || super.equals(o);
	}

	@Override
	public String toString() {
	    return "Any";
	}

    }

}
