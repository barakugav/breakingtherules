package breakingtherules.utilities;

import com.google.common.base.Objects;

public class Pair<A, B> {

    private A m_first;

    private B m_second;

    public Pair() {
    }

    public Pair(final A first, final B second) {
	m_first = first;
	m_second = second;
    }

    public A getFirst() {
	return m_first;
    }

    public B getSecond() {
	return m_second;
    }

    public void setFirst(final A first) {
	m_first = first;
    }

    public void setSecond(final B second) {
	m_second = second;
    }

    @Override
    public boolean equals(final Object o) {
	if (o == this) {
	    return true;
	}
	if (!(o instanceof Pair)) {
	    return false;
	}

	final Pair<?, ?> other = (Pair<?, ?>) o;
	return Objects.equal(m_first, other.m_first) && Objects.equal(m_second, other.m_second);
    }

    @Override
    public int hashCode() {
	return Objects.hashCode(m_first, m_second);
    }

    @Override
    public String toString() {
	return Utility.toStringArray(m_first, m_second);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Pair<A, B> clone() throws CloneNotSupportedException {
	return (Pair<A, B>) super.clone();
    }

    public static <A, B> Pair<A, B> of() {
	return new Pair<>();
    }

    public static <A, B> Pair<A, B> of(final A first, final B second) {
	return new Pair<>(first, second);
    }

}
