package breakingtherules.firewall;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;

import breakingtherules.utilities.Int2ObjectCache;
import breakingtherules.utilities.Int2ObjectSoftHashCache;
import breakingtherules.utilities.SoftCustomHashCache;

/**
 * Source attribute.
 * <p>
 * Defined by it's IP.
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * 
 */
public class Source extends IPAttribute {

    /**
     * Source attribute that represent 'Any' source (contains all others)
     */
    public static final Source ANY_SOURCE = new AnySource();

    /**
     * Construct new source of an IP.
     * 
     * @param ip
     *            IP of the source.
     */
    private Source(final IP ip) {
	super(ip);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final Attribute other) {
	return other instanceof Source && super.contains(other);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
	return SOURCE_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTypeId() {
	return SOURCE_TYPE_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
	return o instanceof Source && super.equals(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Source createMutation(final IP ip) {
	return Source.valueOf(ip, null);
    }

    /**
     * Get Source object parsed from string.
     * 
     * TODO - specified expected input format.
     * 
     * @param s
     *            string representation of a source.
     * @return Source object with the IP parsed from the string.
     * @see IP#valueOf(String)
     */
    public static Source valueOf(final String s) {
	return new Source(IP.valueOf(s, null));
    }

    /**
     * Get Source object parsed from string.
     * <p>
     * If the cache isn't null, will used the cached source from the cache if
     * one exist, or will create a new one and cache it to the cache otherwise.
     * <p>
     * 
     * TODO - specified expected input format.
     * 
     * @param s
     *            string representation of a source.
     * @param cache
     *            the cached containing cached source objects. Can be null.
     * @return Source object with the IP parsed from the string.
     * @see IP#valueOf(String)
     */
    public static Source valueOf(final String s, final Source.Cache cache) {
	return valueOf(IP.valueOf(s, null), cache);
    }

    /**
     * Get Source object with the specified IP.
     * 
     * @param ip
     *            an IP
     * @return Source object with the specified IP.
     * @throws NullPointerException
     *             if the IP is null.
     */
    public static Source valueOf(final IP ip) {
	return new Source(Objects.requireNonNull(ip));
    }

    /**
     * Get Source object with the specified IP.
     * <p>
     * If the cache isn't null, will used the cached source from the cache if
     * one exist, or will create a new one and cache it to the cache otherwise.
     * <p>
     * 
     * @param ip
     *            an IP.
     * @param cache
     *            the cached containing cached source objects. Can be null.
     * @return Source object with the specified IP.
     * @throws NullPointerException
     *             if the IP is null.
     */
    public static Source valueOf(final IP ip, final Source.Cache cache) {
	if (cache != null && ip.m_maskSize == ip.getSize()) {
	    // If ip is a full IP (most common source objects) search it in
	    // cache, or add one if one doesn't exist.
	    if (ip instanceof IPv4) {
		return cache.IPv4Cache.getOrAdd(((IPv4) ip).m_address, cache.ipv4SourcesMappingFunction);
	    }
	    if (ip instanceof IPv6) {
		return cache.IPv6Cache.getOrAdd(((IPv6) ip).m_address, cache.ipv6SourcesMapptingFunction);
	    }
	}
	return new Source(ip);
    }

    /**
     * Cache of {@link Source} objects.
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
	 * Cache of source objects with IPv4 addresses.
	 */
	private final Int2ObjectCache<Source> IPv4Cache;

	/**
	 * Cache of source objects with IPv6 addresses.
	 */
	private final breakingtherules.utilities.Cache<int[], Source> IPv6Cache;

	/**
	 * The mapping function of source object with IPv4 addresses.
	 */
	private final IntFunction<Source> ipv4SourcesMappingFunction;

	/**
	 * The mapping function of source object with IPv6 addresses.
	 */
	private final Function<int[], Source> ipv6SourcesMapptingFunction;

	/**
	 * Construct new source cache.
	 * <p>
	 * If an IP cache exists, the
	 * {@link Source.Cache#Cache(breakingtherules.firewall.IP.Cache)} should
	 * be used.
	 */
	public Cache() {
	    this(null);
	}

	/**
	 * Construct new source cache, built on an existing IPs cache.
	 * <p>
	 * 
	 * @param ipsCache
	 *            the existing IPs cache. (can be null)
	 */
	public Cache(final IP.Cache ipsCache) {
	    this.ipsCache = ipsCache != null ? ipsCache : new IP.Cache();
	    ipv4SourcesMappingFunction = address -> new Source(
		    this.ipsCache.ipv4Cache.cache.getOrAdd(address, IPv4.Cache.supplier));
	    ipv6SourcesMapptingFunction = address -> new Source(
		    this.ipsCache.ipv6Cache.cache.getOrAdd(address, IPv6.Cache.supplier));
	    IPv4Cache = new Int2ObjectSoftHashCache<>();
	    IPv6Cache = new SoftCustomHashCache<>(IPv6.Cache.IPv6AddressesStrategy.INSTANCE);
	}

    }

    /**
     * Any source - contains all other.
     * 
     * @author Barak Ugav
     * @author Yishai Gronich
     *
     */
    private static class AnySource extends Source {

	/**
	 * Construct new AnySource. Called once.
	 */
	AnySource() {
	    super(IP.ANY_IP);
	}

	/**
	 * Contains all sources
	 */
	@Override
	public boolean contains(final Attribute other) {
	    return other instanceof Source;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
	    return o instanceof AnySource || super.equals(o);
	}

    }

}
