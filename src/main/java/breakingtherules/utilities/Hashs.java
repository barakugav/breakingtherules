package breakingtherules.utilities;

public class Hashs {

    private Hashs() {
    }

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
     * @see #hash(Object)
     */
    private static final int PHI = 0x9E3779B9;

    /**
     * Compute first power of 2 equal or greater then a number.
     * <p>
     * Implementation notes:<br>
     * The method fill the number's lower bits with ones, for example:<br>
     * <code>0b010011</code> to <code>0b011111</code><br>
     * and then and one, so:<br>
     * <code>0b011111</code> to <code>0b100000</code>
     * 
     * @param x
     *            the number
     * @return first power of 2 equal or greater then {@code x}
     */
    static int nextPowerOfTwo(int x) {
	if (x == 0)
	    return 1;
	x--;
	x |= x >> 1;
	x |= x >> 2;
	x |= x >> 4;
	x |= x >> 8;
	return (x | x >> 16) + 1;
    }

    /**
     * Compute the hash code for an object and mix the result bits.
     * 
     * <p>
     * Compute the object hash and mixes the bits of the result by multiplying
     * by the golden ratio and xorshifting the result. It is borrowed from
     * <a href="https://github.com/OpenHFT/Koloboke">Koloboke</a>.
     * 
     * @param o
     *            non null object
     * @return a hash value obtained by mixing the bits of the object's hash
     *         code.
     */
    static int hash(final Object o) {
	final int h = o.hashCode() * PHI;
	return h ^ (h >>> 16);
    }

}
