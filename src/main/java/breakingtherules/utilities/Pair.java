package breakingtherules.utilities;

import java.util.Objects;

public class Pair<A, B> {

    public A first;

    public B second;

    public Pair() {
	this(null, null);
    }

    public Pair(A first, B second) {
	this.first = first;
	this.second = second;
    }

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

    @Override
    public int hashCode() {
	return Objects.hashCode(first) * (1 << (Integer.SIZE / 2)) + Objects.hashCode(second);
    }

}
