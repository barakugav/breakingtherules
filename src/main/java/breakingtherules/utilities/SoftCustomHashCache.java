package breakingtherules.utilities;

import java.util.Objects;

import breakingtherules.utilities.Hashs.Strategy;

/**
 * TODO
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * 
 * @see SoftHashCache
 * @see Strategy
 *
 * @param <K>
 *            type of keys
 * @param <E>
 */
public class SoftCustomHashCache<K, E> extends SoftHashCache<K, E> {

    /**
     * The strategy used by this cache.
     */
    private final Strategy<? super K> strategy;

    /**
     * Construct new SoftCustomHashCache with default init capacity and default
     * load factor.
     * <p>
     * 
     * @param strategy
     *            The strategy used by this cache.
     * @throws NullPointerException
     *             if the strategy is null.
     */
    public SoftCustomHashCache(final Strategy<? super K> strategy) {
	this(strategy, Hashs.DEFAULT_INIT_CAPACITY, Hashs.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Construct new SoftCustomHashCache with init capacity parameter and
     * default load factor.
     * <p>
     * 
     * @param strategy
     *            The strategy used by this cache.
     * @param initCapacity
     *            the initialize capacity of the cache, can be zero
     * @throws IllegalArgumentException
     *             if init capacity is negative.
     * @throws NullPointerException
     *             if the strategy is null.
     */
    public SoftCustomHashCache(final Strategy<? super K> strategy, final int initCapacity) {
	this(strategy, initCapacity, Hashs.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Construct new SoftCustomHashCache with init capacity parameter and load
     * factor parameter.
     * <p>
     * 
     * @param strategy
     *            The strategy used by this cache.
     * @param initCapacity
     *            the initialize capacity of the cache, can be zero
     * @param loadFactor
     *            the load factor of the cache, see {@link #loadFactor}
     * @throws IllegalArgumentException
     *             if init capacity is negative, load factor is negative, 0 or
     *             NaN.
     * @throws NullPointerException
     *             if the strategy is null.
     */
    public SoftCustomHashCache(final Strategy<? super K> strategy, final int initCapacity, final float loadFactor) {
	super(initCapacity, loadFactor);
	this.strategy = Objects.requireNonNull(strategy, "Null strategy");
    }

    /**
     * Determines if the two keys are equals by the strategy.
     */
    @Override
    boolean determineEquals(final K k1, final K k2) {
	return strategy.equals(k1, k2);
    }

    /**
     * Compute the hash code of a key by the strategy.
     */
    @Override
    int computeHashCode(final K key) {
	return strategy.hashCode(key);
    }

}