package breakingtherules.firewall;

import java.util.Objects;

import breakingtherules.utilities.WeakCache;

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

    public static void refreshCache() {
	for (WeakCache<Integer, Source> cache : SourceCache.IPv4cache)
	    cache.cleanCache();
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
	    WeakCache<Integer, Source> cache = SourceCache.IPv4cache[ip.prefixLength];

	    Integer addressInteger = new Integer(((IPv4) ip).address());
	    source = cache.get(addressInteger);
	    if (source == null) {
		source = new Source(ip);
		cache.add(addressInteger, source);
	    }
	} else {
	    source = new Source(ip);
	}
	return source;
    }

    private static class SourceCache {

	static final WeakCache<Integer, Source>[] IPv4cache;

	static {
	    @SuppressWarnings({ "unchecked", "unused" })
	    Object dummy = IPv4cache = new WeakCache[IPv4.MAX_LENGTH + 1];

	    for (int i = IPv4cache.length; i-- != 0;)
		IPv4cache[i] = new WeakCache<>();
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
