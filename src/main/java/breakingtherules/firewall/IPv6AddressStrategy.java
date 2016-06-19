package breakingtherules.firewall;

import breakingtherules.utilities.SoftCustomHashCache;
import breakingtherules.utilities.WeakCustomHashCache;
import breakingtherules.utilities.Hashs.Strategy;

/**
 * A strategy the treats only to the address of an IPv6.
 * <p>
 * This strategy is used by {@link Source} class' cache and {@link Destination}
 * class' cache.
 * <p>
 * This class is singleton.
 * <p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * 
 * @see WeakCustomHashCache
 * @see SoftCustomHashCache
 *
 */
class IPv6AddressStrategy implements Strategy<IPv6> {

    /**
     * The single instance of this class.
     */
    static final IPv6AddressStrategy INSTANCE = new IPv6AddressStrategy();

    /**
     * Construct new IPv6AddressStrategy, called once.
     */
    private IPv6AddressStrategy() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.utilities.Hashs.Strategy#equals(java.lang.Object,
     * java.lang.Object)
     */
    @Override
    public boolean equals(final IPv6 a, final IPv6 b) {
	for (int i = IPv6.ADDRESS_ARRAY_SIZE; i-- != 0;)
	    if (a.m_address[i] != b.m_address[i])
		return false;
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.utilities.Hashs.Strategy#hashCode(java.lang.Object)
     */
    @Override
    public int hashCode(final IPv6 k) {
	int h = 17;
	for (int i = IPv6.ADDRESS_ARRAY_SIZE; i-- != 0;)
	    h = h * 31 + k.m_address[i];
	return h;
    }

}
