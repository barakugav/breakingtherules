package breakingtherules.utilities;

import java.util.List;

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

}
