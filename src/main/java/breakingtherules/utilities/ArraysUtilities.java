package breakingtherules.utilities;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * The ArraysUtilities class is a set of array helper methods. All methods are
 * static
 */
public class ArraysUtilities {

    /**
     * Merge a set of boolean arrays to one
     * 
     * @param arrays
     *            some boolean arrays
     * @return new boolean array that is a merge of all others
     */
    public static boolean[] merge(final boolean[]... arrays) {
	int length = 0;
	for (final boolean[] array : arrays) {
	    length += array.length;
	}

	final boolean[] result = new boolean[length];
	int offset = 0;
	for (final boolean[] array : arrays) {
	    System.arraycopy(array, 0, result, offset, array.length);
	    offset += array.length;
	}

	return result;
    }

    /**
     * Convert an <code>int</code> number to a booleans array that represents
     * the number by bits
     * 
     * @param num
     *            the number to convert
     * @param length
     *            requested boolean array length
     * @return boolean array that represents the number
     */
    public static boolean[] intToBooleans(int num, final int length) {
	if (length < 0) {
	    throw new IllegalArgumentException("length can't be negaive " + length);
	}

	final boolean[] result = new boolean[length];
	for (int i = 0; i < length; i++) {
	    result[result.length - i - 1] = (num & 1) == 1;
	    num >>= 1;
	}

	return result;
    }

    /**
     * Convert a list of strings to an array of strings
     * 
     * @param list
     *            list of strings to convert
     * @return an array with the strings out of the list
     */
    public static String[] toArray(final List<String> list) {
	return list.toArray(new String[list.size()]);
    }

    public static boolean[] toArrayBoolean(final List<Boolean> list) {
	final boolean[] arr = new boolean[list.size()];
	int i = 0;
	for (final Iterator<Boolean> it = list.iterator(); it.hasNext();) {
	    arr[i++] = it.next();
	}
	return arr;
    }

    public static class ArrayIterator<T> implements Iterator<T> {

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

}
