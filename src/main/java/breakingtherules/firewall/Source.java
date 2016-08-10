package breakingtherules.firewall;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;

import breakingtherules.util.Int2ObjectCache;
import breakingtherules.util.Int2ObjectOpenAddressingHashCache;
import breakingtherules.util.Object2ObjectCache;
import breakingtherules.util.Object2ObjectCustomBucketHashCache;

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
    public Source createMutation(final IP ip) {
	return Source.valueOf(ip);
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
    public AttributeType getType() {
	return AttributeType.SOURCE;
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
	return new Source(IP.valueOf(s));
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
	private final Object2ObjectCache<int[], Source> IPv6Cache;

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
	    IPv4Cache = new Int2ObjectOpenAddressingHashCache<>();
	    IPv6Cache = new Object2ObjectCustomBucketHashCache<>(IPv6.Cache.IPv6AddressesStrategy.INSTANCE);
	}

	public Source add(final Source source) {
	    final IP ip = source.getIp();
	    if (ip.m_maskSize == ip.getSize()) {
		// If ip is a full IP (most common source objects) search it in
		// cache, or add one if one doesn't exist.
		if (ip instanceof IPv4)
		    return IPv4Cache.add(((IPv4) ip).m_address, source);
		if (ip instanceof IPv6)
		    return IPv6Cache.add(((IPv6) ip).m_address, source);
	    }
	    return source;
	}

	public Source valueOf(final IP ip) {
	    if (ip.m_maskSize == ip.getSize()) {
		// If ip is a full IP (most common source objects) search it in
		// cache, or add one if one doesn't exist.
		if (ip instanceof IPv4)
		    return IPv4Cache.getOrAdd(((IPv4) ip).m_address, ipv4SourcesMappingFunction);
		if (ip instanceof IPv6)
		    return IPv6Cache.getOrAdd(((IPv6) ip).m_address, ipv6SourcesMapptingFunction);
	    }
	    return new Source(ip);
	}

	public Source valueOf(final String s) {
	    return valueOf(IP.valueOf(s));
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
	public boolean equals(final Object o) {
	    return o instanceof AnySource || super.equals(o);
	}

    }

}
