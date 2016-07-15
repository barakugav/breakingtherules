package breakingtherules.utilities;

import java.util.Objects;

/**
 * Triple of three elements.
 *
 * @author Barak Ugav
 * @author Yishai Gronich
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
    private A m_first;

    /**
     * Second element in the triple
     */
    private B m_second;

    /**
     * Third element in the triple
     */
    private C m_third;

    /**
     * Construct new empty triple
     */
    public Triple() {
    }

    /**
     * Construct new triple
     *
     * @param a
     *            value of first element
     * @param b
     *            value of second element
     * @param c
     *            value of third element
     */
    public Triple(final A a, final B b, final C c) {
	m_first = a;
	m_second = b;
	m_third = c;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
	if (o == this)
	    return true;
	if (!(o instanceof Triple))
	    return false;

	final Triple<?, ?, ?> other = (Triple<?, ?, ?>) o;
	return Objects.equals(m_first, other.m_first) && Objects.equals(m_second, other.m_second)
		&& Objects.equals(m_third, other.m_third);
    }

    /**
     * Get the first element
     *
     * @return the triple's first element
     */
    public A getFirst() {
	return m_first;
    }

    /**
     * Get the second element
     *
     * @return the triple's second element
     */
    public B getSecond() {
	return m_second;
    }

    /**
     * Get the third element
     *
     * @return the triple's third element
     */
    public C getThird() {
	return m_third;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
	return Objects.hash(m_first, m_second, m_third);
    }

    /**
     * Set the value of the first element
     *
     * @param first
     *            new value for the tupl's first element
     */
    public void setFirst(final A first) {
	m_first = first;
    }

    /**
     * Set the value of the second element
     *
     * @param second
     *            new value for the tuple's second element
     */
    public void setSecond(final B second) {
	m_second = second;
    }

    /**
     * Set the value of the third element
     *
     * @param third
     *            new value for the tuple's third element
     */
    public void setThird(final C third) {
	m_third = third;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
	return Utility.toStringArray(m_first, m_second, m_third);
    }

    /**
     * Unmodifiable version of triple.
     *
     * @author Barak Ugav
     * @author Yishai Gronich
     *
     * @param <A>
     *            type of first element
     * @param <B>
     *            type of second element
     * @param <C>
     *            type of third element
     */
    public static class UnmodifiableTriple<A, B, C> extends Triple<A, B, C> {

	/**
	 * Construct new UnmodifiableTriple
	 *
	 * @param a
	 *            first element
	 * @param b
	 *            second element
	 * @param c
	 *            third element
	 */
	public UnmodifiableTriple(final A a, final B b, final C c) {
	    super(a, b, c);
	}

	/**
	 * @throws UnsupportedOperationException
	 *             (always)
	 */
	@Override
	public void setFirst(final A first) {
	    throw new UnsupportedOperationException();
	}

	/**
	 * @throws UnsupportedOperationException
	 *             (always)
	 */
	@Override
	public void setSecond(final B second) {
	    throw new UnsupportedOperationException();
	}

	/**
	 * @throws UnsupportedOperationException
	 *             (always)
	 */
	@Override
	public void setThird(final C third) {
	    throw new UnsupportedOperationException();
	}

    }

}
