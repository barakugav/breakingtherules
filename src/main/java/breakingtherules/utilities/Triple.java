package breakingtherules.utilities;

import java.util.Objects;

/**
 * Triple of three elements
 *
 * @param <A>
 *            type of first element
 * @param <B>
 *            type of second element
 * @param <C>
 *            type of third element
 */
public class Triple<A, B, C> {

    /**
     * First element in the triple
     */
    public A first;

    /**
     * Second element in the triple
     */
    public B second;

    /**
     * Third element in the triple
     */
    public C third;

    /**
     * Constructor
     * 
     * @param a
     *            value of first element
     * @param b
     *            value of second element
     * @param c
     *            value of third element
     */
    public Triple(A a, B b, C c) {
	first = a;
	second = b;
	third = c;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
	if (o == null) {
	    return false;
	} else if (o == this) {
	    return true;
	} else if (!(o instanceof Triple<?, ?, ?>)) {
	    return false;
	}

	Triple<?, ?, ?> other = (Triple<?, ?, ?>) o;
	return Objects.equals(first, other.first) && Objects.equals(second, other.second)
		&& Objects.equals(third, other.third);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	int hash = 0;
	hash += Objects.hashCode(first);
	hash <<= 10;
	hash += Objects.hashCode(second);
	hash <<= 10;
	hash += Objects.hashCode(third);
	return hash;
    }

}
