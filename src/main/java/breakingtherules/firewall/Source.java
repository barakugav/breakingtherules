package breakingtherules.firewall;

import java.util.Objects;
import java.util.function.Function;

import breakingtherules.utilities.Cache;
import breakingtherules.utilities.Caches;
import breakingtherules.utilities.Caches.CacheSupplierPair;
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * breakingtherules.firewall.IPAttribute#contains(breakingtherules.firewall.
     * Attribute)
     */
    @Override
    public boolean contains(final Attribute other) {
	return other instanceof Source && super.contains(other);
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

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.IPAttribute#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object o) {
	return o instanceof Source && super.equals(o);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * breakingtherules.firewall.IPAttribute#createMutation(breakingtherules.
     * firewall.IP)
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
	    if (ip instanceof IPv4) {
		return SourceCache.IPv4Cache.getOrAdd((IPv4) ip);
	    } else if (ip instanceof IPv6) {
		return SourceCache.IPv6Cache.getOrAdd((IPv6) ip);
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
     */
    private static class SourceCache {

	static final CacheSupplierPair<IPv4, Source> IPv4Cache;

	static final CacheSupplierPair<IPv6, Source> IPv6Cache;

	static {
	    final Cache<IPv4, Source> cache4 = Caches
		    .synchronizedCache(new SoftCustomHashCache<>(IPv4AddressStrategy.INSTANCE));
	    final Function<IPv4, Source> supplier4 = (final IPv4 ip) -> {
		return new Source(ip);
	    };
	    IPv4Cache = Caches.cacheSupplierPair(cache4, supplier4);

	    final Cache<IPv6, Source> cache6 = Caches
		    .synchronizedCache(new SoftCustomHashCache<>(IPv6AddressStrategy.INSTANCE));
	    final Function<IPv6, Source> supplier6 = (final IPv6 ip) -> {
		return new Source(ip);
	    };
	    IPv6Cache = Caches.cacheSupplierPair(cache6, supplier6);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see breakingtherules.firewall.Source#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
	    return o instanceof AnySource || super.equals(o);
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
