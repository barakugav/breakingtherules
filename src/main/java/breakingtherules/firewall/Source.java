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

    public static Source valueOf(final String s) {
	return new Source(IP.valueOf(s, null));
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
    public static Source valueOf(final String s, final Source.Cache cache) {
	return valueOf(IP.valueOf(s, null), cache);
    }

    public static Source valueOf(final IP ip) {
	return new Source(Objects.requireNonNull(ip));
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

	private final IP.Cache ipsCache;

	/**
	 * Cache of source objects with full(not subnetwork) IPv4 ips.
	 */
	private final Int2ObjectCache<Source> IPv4Cache;

	/**
	 * Cache of source objects with full(not subnetwork) IPv6 ips.
	 */
	private final breakingtherules.utilities.Cache<int[], Source> IPv6Cache;

	private final IntFunction<Source> ipv4SourcesMappingFunction;

	private final Function<int[], Source> ipv6SourcesMapptingFunction;

	public Cache(final IP.Cache ipsCache) {
	    this.ipsCache = ipsCache != null ? ipsCache : new IP.Cache();
	    ipv4SourcesMappingFunction = address -> new Source(
		    ipsCache.ipv4Cache.cache.getOrAdd(address, IPv4.Cache.supplier));
	    ipv6SourcesMapptingFunction = address -> new Source(
		    ipsCache.ipv6Cache.cache.getOrAdd(address, IPv6.Cache.supplier));
	    IPv4Cache = new Int2ObjectSoftHashCache<>();
	    IPv6Cache = new SoftCustomHashCache<>(IPv6.Cache.IPv6AddressesStrategy.INSTANCE);
	}

	public IP.Cache getIPsCache() {
	    return ipsCache;
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
