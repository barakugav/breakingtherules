package breakingtherules.util;

import java.util.function.Function;

/**
 * A mutable version of {@link Integer}.
 * <p>
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 */
public class MutableInteger implements Comparable<MutableInteger>, Cloneable {

    /**
     * The value of the integer.
     */
    public int value;

    /**
     * Function that always return new {@link MutableInteger} with the value 0.
     */
    private static final Function<Object, MutableInteger> zeroFunction = ignored -> new MutableInteger();

    /**
     * Construct new {@link MutableInteger} with the value 0.
     */
    public MutableInteger() {
    }

    /**
     * Construct new {@link MutableInteger} with a specified value.
     *
     * @param value
     *            initialize value.
     */
    public MutableInteger(final int value) {
	this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MutableInteger clone() {
	try {
	    return (MutableInteger) super.clone();
	} catch (final CloneNotSupportedException e) {
	    // Shouldn't happen since we are cloneable
	    throw new InternalError(e);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final MutableInteger other) {
	return Integer.compare(value, other.value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
	if (o == this)
	    return true;
	if (!(o instanceof MutableInteger))
	    return false;

	final MutableInteger other = (MutableInteger) o;
	return other.value == value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
	return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
	return Integer.toString(value);
    }

    /**
     * Get a function that always return new {@link MutableInteger} with the
     * value 0.
     *
     * @param <T>
     *            the type of the function input.
     * @return function that always return new {@link MutableInteger} with the
     *         value 0.
     */
    @SuppressWarnings("unchecked")
    public static <T> Function<T, MutableInteger> zeroFunction() {
	return (Function<T, MutableInteger>) zeroFunction;
    }

}
