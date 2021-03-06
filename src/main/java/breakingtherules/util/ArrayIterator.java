package breakingtherules.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The ArrayIterator is an iterator used to iterate over arrays.
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @param <E>
 *            type of elements the iterator will iterate over
 */
public class ArrayIterator<E> implements Iterator<E> {

    /**
     * The length of the array.
     */
    private final int length;

    /**
     * The array the iterator is iterating on.
     */
    private final E[] array;

    /**
     * The next index of the next element to return.
     */
    private int nextIndex;

    /**
     * If true, the iterator will skip nulls.
     */
    private final boolean skipNull;

    /**
     * Construct new ArrayIterator on an array.
     * <p>
     * The iterator will not skip nulls.
     *
     * @param array
     *            the array to iterate on.
     * @throws NullPointerException
     *             if the array is null.
     */
    public ArrayIterator(final E[] array) {
	this(array, false);
    }

    /**
     * Construct new ArrayIterator on an array.
     * <p>
     * The iterator will skip nulls if the the {@code skipNull} parameter is
     * true.
     *
     * @param array
     *            the array to iterate on.
     * @param skipNull
     *            if true, the iterator will skip nulls.
     * @throws NullPointerException
     *             if the array is null.
     */
    public ArrayIterator(final E[] array, final boolean skipNull) {
	this.array = array;
	length = array.length;
	this.skipNull = skipNull;
	advanceIndex();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
	return nextIndex < length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E next() {
	final int i = nextIndex;
	if (i >= length)
	    throw new NoSuchElementException();
	nextIndex = i + 1;
	advanceIndex();
	return array[i];
    }

    /**
     * Advance the index of the iterator to the next valid element.
     * <p>
     * Will skip nulls if the {@link #skipNull} flag is true.
     */
    private void advanceIndex() {
	while (nextIndex < length && array[nextIndex] == null && skipNull)
	    nextIndex++;
    }

}
