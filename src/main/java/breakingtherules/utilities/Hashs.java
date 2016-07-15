package breakingtherules.utilities;

/**
 * Utility class for hash tables.
 * <p>
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 */
public class Hashs {

    /**
     * The default load factor used by hash tables.
     */
    public static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * Load factor for fast hash tables.
     * <p>
     * Uses approximately 1.5 times more memory then
     * {@link #DEFAULT_LOAD_FACTOR} but create faster hash tables.
     */
    public static final float FAST_LOAD_FACTOR = 0.5f;

    /**
     * Load factor for very fast hash tables.
     * <p>
     * Use approximately 3 times more memory then {@link #DEFAULT_LOAD_FACTOR}
     * (or 2 times more memory then {@link #FAST_LOAD_FACTOR}) but create very
     * fast tables.
     */
    public static final float VERY_FAST_LOAD_FACTOR = 0.25f;

    /**
     * The default init capacity for hash tables.
     */
    public static final int DEFAULT_INIT_CAPACITY = 8;

    /**
     * 2<sup>32</sup> &middot; &phi;, &phi; = (&#x221A;5 &minus; 1)/2.
     * <p>
     * Used to mix hash code bits.
     *
     * @see #mix(int)
     */
    private static final int PHI = 0x9E3779B9;

    /**
     * Suppresses default constructor, ensuring non-instantiability.
     */
    private Hashs() {
    }

    /**
     * Compute the hash code for an object by a strategy and mix the result
     * bits.
     *
     * @param <K>
     *            type of the object.
     * @param k
     *            non null object.
     * @param strategy
     *            the strategy which compute the hash code.
     * @return a hash value obtained by mixing the bits of the object's hash
     *         code by the strategy.
     */
    public static <K> int hash(final K k, final Strategy<? super K> strategy) {
	return mix(strategy.hashCode(k));
    }

    /**
     * Compute the hash code for an object and mix the result bits.
     *
     * @param o
     *            non null object.
     * @return a hash value obtained by mixing the bits of the object's hash
     *         code.
     */
    public static int hash(final Object o) {
	return mix(o.hashCode());
    }

    /**
     * Quickly mixes the bits of an integer.
     * <p>
     * This method mixes the bits of the argument by multiplying by the golden
     * ratio and xorshifting the result. It is borrowed from
     * <a href="https://github.com/OpenHFT/Koloboke">Koloboke</a>.
     *
     * @param x
     *            an integer.
     * @return a hash value obtained by mixing the bits of {@code x}.
     */
    public final static int mix(final int x) {
	final int h = x * PHI;
	return h ^ h >>> 16;
    }

    /**
     * Compute first power of 2 equal or greater then a number.
     * <p>
     *
     *
     * @param x
     *            the number
     * @return first power of 2 equal or greater then {@code x}
     */
    static int nextPowerOfTwo(int x) {
	if (x == 0)
	    return 1;
	/*
	 * Implementation notes:
	 *
	 * The method fill the number's lower bits with ones, for example:
	 *
	 * 0b010011 to 0b011111
	 *
	 * and then add one, so:
	 *
	 * 0b011111 to 0b100000
	 */
	x--;
	x |= x >> 1;
	x |= x >> 2;
	x |= x >> 4;
	x |= x >> 8;
	return (x | x >> 16) + 1;
    }

    /**
     * A hash strategy deciding equality and computing hash codes.
     * <p>
     *
     * @author Barak Ugav
     * @author Yishai Gronich
     *
     * @param <K>
     *            type of object the strategy is working on.
     */
    public static interface Strategy<K> {

	/**
	 * Check if two <bold>non-nulls</bold> objects are equals by the
	 * strategy.
	 * <p>
	 *
	 * @param a
	 *            first compared object.
	 * @param b
	 *            second compared object.
	 * @return true if the two objects are equals by the strategy, else -
	 *         false.
	 */
	public boolean equals(K a, K b);

	/**
	 * Compute the hash code for an object.
	 * <p>
	 *
	 * @param k
	 *            non-null object.
	 * @return hash code of the object by the strategy.
	 */
	public int hashCode(K k);

    }

}
