package breakingtherules.firewall;

import breakingtherules.utilities.Hashs.Strategy;
import breakingtherules.utilities.SoftCustomHashCache;

/**
 * A strategy the treats only to the address of an IPv4.
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
 * @see SoftCustomHashCache
 */
class IPv4AddressStrategy implements Strategy<IPv4> {

    /**
     * The single instance of this strategy.
     */
    static final IPv4AddressStrategy INSTANCE = new IPv4AddressStrategy();

    /**
     * Construct new IPv4AddressStrategy, call once.
     * <p>
     * 
     * @see #INSTANCE
     */
    private IPv4AddressStrategy() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final IPv4 a, final IPv4 b) {
	return a.m_address == b.m_address;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode(final IPv4 k) {
	return k.m_address;
    }

}
