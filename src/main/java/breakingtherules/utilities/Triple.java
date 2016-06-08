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
    private A m_first;

    /**
     * Second element in the triple
     */
    private B m_second;

    /**
     * Third element in the triple
     */
    private C m_third;

    public Triple() {
    }

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
    public Triple(final A a, final B b, final C c) {
	m_first = a;
	m_second = b;
	m_third = c;
    }

    public A getFirst() {
	return m_first;
    }

    public B getSecond() {
	return m_second;
    }

    public C getThird() {
	return m_third;
    }

    public void setFirst(final A first) {
	m_first = first;
    }

    public void setSecond(final B second) {
	m_second = second;
    }

    public void setThird(final C third) {
	m_third = third;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object o) {
	if (o == this) {
	    return true;
	} else if (!(o instanceof Triple)) {
	    return false;
	}

	final Triple<?, ?, ?> other = (Triple<?, ?, ?>) o;
	return Objects.equals(m_first, other.m_first) && Objects.equals(m_second, other.m_second)
		&& Objects.equals(m_third, other.m_third);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	return Objects.hash(m_first, m_second, m_third);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return Utility.toStringArray(m_first, m_second, m_third);
    }

    public static class UnmodifiableTriple<A, B, C> extends Triple<A, B, C> {

	public UnmodifiableTriple(final A a, final B b, final C c) {
	    super(a, b, c);
	}

	@Override
	public void setFirst(final A first) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public void setSecond(final B second) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public void setThird(final C third) {
	    throw new UnsupportedOperationException();
	}

    }

}
