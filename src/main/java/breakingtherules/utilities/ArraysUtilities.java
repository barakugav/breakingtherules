package breakingtherules.utilities;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

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
    public static boolean[] merge(boolean[]... arrays) {
	if (arrays == null) {
	    throw new IllegalArgumentException("Arrays can't be null");
	}
	for (boolean[] array : arrays) {
	    if (array == null) {
		throw new IllegalArgumentException("Non of the arrays can be null");
	    }
	}

	int length = 0;
	for (boolean[] array : arrays) {
	    length += array.length;
	}

	boolean[] result = new boolean[length];
	int offset = 0;
	for (boolean[] array : arrays) {
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
    public static boolean[] intToBooleans(int num, int length) {
	if (length < 0) {
	    throw new IllegalArgumentException("length can't be negaive " + length);
	}
	boolean[] result = new boolean[length];

	for (int i = 0; i < length; i++) {
	    result[result.length - i - 1] = num % 2 == 1;
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
    public static String[] toArray(List<String> list) {
	return list.toArray(new String[list.size()]);
    }

    public static boolean[] toArrayBoolean(List<Boolean> list) {
	boolean[] arr = new boolean[list.size()];
	for (int i = 0; i < arr.length; i++)
	    arr[i] = list.get(i);
	return arr;
    }

    public static <T> Iterator<T> iterator(T[] array) {
	return new ArrayIterator<>(array);
    }

    private static class ArrayIterator<T> implements Iterator<T> {

	private final T[] array;
	private int index;

	private ArrayIterator(T[] array) {
	    this.array = Objects.requireNonNull(array);
	    index = 0;
	}

	@Override
	public boolean hasNext() {
	    return index < array.length;
	}

	@Override
	public T next() {
	    return array[index++];
	}

    }

}
