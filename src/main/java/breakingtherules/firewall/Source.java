package breakingtherules.firewall;

import java.util.Objects;
import java.util.function.Function;

import breakingtherules.utilities.Cache;
import breakingtherules.utilities.Caches;
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
	return Source.create(ip);
    }

    /**
     * Create a source from an IP.
     * 
     * @param ip
     *            an IP
     * @return Source object with the specified IP.
     * @throws NullPointerException
     *             if the IP is null.
     */
    public static Source create(final IP ip) {
	return createInternal(Objects.requireNonNull(ip));
    }

    /**
     * Create a source from a string representation of an I
     * 
     * @param ip
     *            string IP
     * @return Source object with the IP parsed from the string.
     * @see IP#createFromString(String)
     */
    public static Source createFromString(final String ip) {
	return createInternal(IP.createFromString(ip));
    }

    /**
     * Create a source, used internally.
     * 
     * @param ip
     *            an IP.
     * @return Source object with the specified IP.
     */
    private static Source createInternal(final IP ip) {
	if (ip.m_maskSize == ip.getSize()) {
	    // If ip is a full IP (most common source objects) search it in
	    // cache, or add one if one doesn't exist.
	    if (ip instanceof IPv4) {
		return SourceCache.IPv4Cache.getOrAdd((IPv4) ip, SourceCache.supplier);
	    }
	    if (ip instanceof IPv6) {
		return SourceCache.IPv6Cache.getOrAdd((IPv6) ip, SourceCache.supplier);
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
	static final Cache<IPv4, Source> IPv4Cache;

	/**
	 * Cache of source objects with full(not subnetwork) IPv6 ips.
	 */
	static final Cache<IPv6, Source> IPv6Cache;

	/**
	 * Supplier used to supply new source objects to the cache in case they
	 * are missing.
	 * <p>
	 * Used by {@link Cache#getOrAdd(Object, Function)}.
	 */
	static final Function<IP, Source> supplier;

	static {
	    IPv4Cache = Caches.synchronizedCache(new SoftCustomHashCache<>(IPv4AddressStrategy.INSTANCE));
	    IPv6Cache = Caches.synchronizedCache(new SoftCustomHashCache<>(IPv6AddressStrategy.INSTANCE));
	    supplier = ip -> new Source(ip);
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
