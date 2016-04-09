package breakingtherules.utilities;

import java.util.Objects;

/**
 * Pair of two elements
 *
 * @param <A>
 *            type of first element
 * @param <B>
 *            type of second element
 */
public class Pair<A, B> {

    /**
     * First element in the pair
     */
    public A first;

    /**
     * Second element in the pair
     */
    public B second;

    /**
     * Constructor
     * 
     * Build an empty pair
     */
    public Pair() {
	this(null, null);
    }

    /**
     * Constructor
     * 
     * @param first
     *            value of fist element in the pair
     * @param second
     *            value of second element in the pair
     */
    public Pair(A first, B second) {
	this.first = first;
	this.second = second;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
	if (o == null)
	    return false;
	if (o == this)
	    return true;
	if (!(o instanceof Pair<?, ?>))
	    return false;

	Pair<?, ?> other = (Pair<?, ?>) o;
	return Objects.deepEquals(first, other.first) && Objects.deepEquals(second, other.second);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	return Objects.hashCode(first) * (1 << (Integer.SIZE / 2)) + Objects.hashCode(second);
    }

}
