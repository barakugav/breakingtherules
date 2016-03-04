package breakingtherules.utilities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * The Utility class provide a set of static helper methods. All method are
 * static
 */
public class Utility {

    /**
     * Get a sub list of a list by offset and size
     * 
     * @param list
     *            the list
     * @param offset
     *            the offset of the desire sub list
     * @param size
     *            the size of the desire sub list
     * @return sub list of the list in range [offset, min(list.size, offset +
     *         size))
     * @throws IllegalArgumentException
     *             if list is null, offset < 0, size < 0
     */
    public static <T> List<T> subList(List<T> list, int offset, int size) {
	if (list == null)
	    throw new IllegalArgumentException("list can't be null");
	if (offset < 0 || size < 0)
	    throw new IllegalArgumentException("offset and size should be positive (" + offset + ", " + size + ")");
	if (offset >= list.size())
	    return new ArrayList<T>();
	return list.subList(offset, Math.min(list.size(), offset + size));
    }

    /**
     * Ensure the uniqueness of a list. Uses <code>T.equals()</code>
     * 
     * @param list
     *            the list
     * @return new list with unique elements from the original list
     */
    public static <T> List<T> ensureUniqueness(List<T> list) {
	return ensureUniqueness(list, new Comparator<T>() {

	    @Override
	    public int compare(T o1, T o2) {
		return Objects.equals(o1, o2) ? 0 : 1;
	    }
	});
    }

    /**
     * Ensure the uniqueness of the list by custom comparator (used, only for
     * equals comparisons)
     * 
     * @param list
     *            the list
     * @param comparator
     *            the comparator used to equal the elements
     * @return new list with unique elements from the original list using the
     *         custom comparator
     */
    public static <T> List<T> ensureUniqueness(List<T> list, Comparator<T> comparator) {
	if (list == null || comparator == null) {
	    throw new IllegalArgumentException("Arguments can't be null");
	}
	List<T> filteredList = new ArrayList<T>();
	for (T e : list) {
	    if (e == null) {
		if (!filteredList.contains(null)) {
		    filteredList.add(null);
		}
		continue;
	    }

	    boolean found = false;
	    for (T existE : filteredList) {
		if (comparator.compare(e, existE) == 0) {
		    found = true;
		    break;
		}
	    }
	    if (!found) {
		filteredList.add(e);
	    }
	}
	return filteredList;
    }

    /**
     * Log 2 of a number
     * 
     * @param num
     *            the number
     * @return log of base 2 of the number
     */
    public static double log2(double num) {
	return StrictMath.log(num) / StrictMath.log(2);
    }

}
