package breakingtherules.utilities;

import java.util.function.Function;
import java.util.function.Supplier;

public class MutableInteger implements Comparable<MutableInteger> {

    public int value;

    public static final Supplier<MutableInteger> zeroInitializerSupplier = () -> new MutableInteger();

    public static final Function<Object, MutableInteger> zeroInitializerFunction = ignored -> new MutableInteger();

    public MutableInteger() {
    }

    public MutableInteger(final int value) {
	this.value = value;
    }

    @Override
    public boolean equals(final Object o) {
	if (o == this) {
	    return true;
	}
	if (!(o instanceof MutableInteger)) {
	    return false;
	}

	final MutableInteger other = (MutableInteger) o;
	return value == other.value;
    }

    @Override
    public int hashCode() {
	return value;
    }

    @Override
    public String toString() {
	return Integer.toString(value);
    }

    @Override
    public int compareTo(final MutableInteger other) {
	return Integer.compare(value, other.value);
    }

}
