package breakingtherules.firewall;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;

import breakingtherules.utilities.Cache;
import breakingtherules.utilities.Caches;
import breakingtherules.utilities.Int2ObjectCache;
import breakingtherules.utilities.Int2ObjectCaches;
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
	return Source.valueOf(ip);
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
	final IP ip = IP.valueOf(s, false);
	if (ip.m_maskSize == ip.getSize()) {
	    if (ip instanceof IPv4) {
		return SourceCache.IPv4Cache.getOrAdd(((IPv4) ip).m_address, SourceCache.IPv4AddressToSourceSupplier);
	    }
	    if (ip instanceof IPv6) {
		return SourceCache.IPv6Cache.getOrAdd((IPv6) ip, SourceCache.uncachedIPv6ToSourceSupplier);
	    }
	}
	return new Source(ip);
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
	return valueOfInternal(Objects.requireNonNull(ip));
    }

    /**
     * Get Source object with the specified IP, used internally.
     * 
     * @param ip
     *            an IP.
     * @return Source object with the specified IP.
     */
    private static Source valueOfInternal(final IP ip) {
	if (ip.m_maskSize == ip.getSize()) {
	    // If ip is a full IP (most common source objects) search it in
	    // cache, or add one if one doesn't exist.
	    if (ip instanceof IPv4) {
		return SourceCache.IPv4Cache.getOrAdd(((IPv4) ip).m_address, SourceCache.IPv4AddressToSourceSupplier);
	    }
	    if (ip instanceof IPv6) {
		return SourceCache.IPv6Cache.getOrAdd((IPv6) ip, SourceCache.cachedIPv6ToSourceSupplier);
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
    private static class SourceCache {

	/**
	 * Cache of source objects with full(not subnetwork) IPv4 ips.
	 */
	static final Int2ObjectCache<Source> IPv4Cache;

	/**
	 * Cache of source objects with full(not subnetwork) IPv6 ips.
	 */
	static final Cache<IPv6, Source> IPv6Cache;

	/**
	 * Supplier used to supply new source objects to the cache in case they
	 * are missing.
	 * <p>
	 * The supplier is used when the IP key is cached IP.
	 * <p>
	 * Used by {@link Cache#getOrAdd(Object, Function)}.
	 */
	static final Function<IPv6, Source> cachedIPv6ToSourceSupplier;

	/**
	 * Supplier used to supply new source objects to the cache in case they
	 * are missing.
	 * <p>
	 * The supplier is used when the IP key is uncached IP.
	 * <p>
	 * Used by {@link Cache#getOrAdd(Object, Function)}.
	 */
	static final Function<IPv6, Source> uncachedIPv6ToSourceSupplier;

	static final IntFunction<Source> IPv4AddressToSourceSupplier;

	static {
	    IPv4Cache = Int2ObjectCaches.synchronizedCache(new Int2ObjectSoftHashCache<>());
	    IPv6Cache = Caches.synchronizedCache(new SoftCustomHashCache<>(IPv6AddressStrategy.INSTANCE));
	    cachedIPv6ToSourceSupplier = ip -> new Source(ip);
	    uncachedIPv6ToSourceSupplier = ip -> new Source(ip.cache());
	    IPv4AddressToSourceSupplier = address -> new Source(IPv4.valueOfBits(address));
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
