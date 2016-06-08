package breakingtherules.utilities;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The ArrayIterator is an iterator used to iterate over arrays.
 * 
 * @param <T>
 *            type of elements the iterator will iterate over
 */
public class ArrayIterator<T> implements Iterator<T> {

    private final int length;
    private final T[] array;
    private int index;

    public ArrayIterator(final T[] array) {
	this.array = array;
	length = array.length;
    }

    @Override
    public boolean hasNext() {
	return index < length;
    }

    @Override
    public T next() {
	final int i = index;
	if (i >= length)
	    throw new NoSuchElementException();
	index = i + 1;
	return array[i];

    }

}
