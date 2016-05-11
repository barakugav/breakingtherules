package breakingtherules.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

/**
 * The Utility class provide a set of static helper methods. All method are
 * static
 */
public class Utility {

    private static final char SPACE = ' ';

    private static final char TAB = '\t';

    /**
     * Clone a list (not a deep clone)
     * 
     * @param list
     *            the list
     * @return clone of the list
     */
    public static <T> List<T> clone(List<? extends T> list) {
	return new ArrayList<>(list);
    }

    /**
     * Get a unmodifiable clone of a list (not a deep clone)
     * 
     * @param list
     *            the list
     * @return clone of the list that can't be modified
     */
    public static <T> List<T> unmodifiableClone(List<? extends T> list) {
	return Collections.unmodifiableList(clone(list));
    }

    /**
     * Put a value in a list at an index even if the list is too small.
     * <p>
     * This method simulate the list as array that allow random access even if
     * the list wasn't grown naturally by <code>List.add(E e)</code>. The list
     * will be appended by nulls up to the desire index if needed
     * 
     * @param list
     *            the list
     * @param index
     *            index in the list
     * @param value
     *            new value in the list
     */
    public static <T> void put(List<T> list, int index, T value) {
	if (list == null) {
	    throw new IllegalArgumentException("list can't be null");
	}
	if (index < 0) {
	    throw new IndexOutOfBoundsException("index must be positive " + index);
	}
	while (list.size() <= index) {
	    list.add(null);
	}
	list.set(index, value);
    }

    /**
     * Clone a list
     * 
     * @param list
     *            the list
     * @return clone of the list
     */
    public static <T> List<T> cloneList(List<? extends T> list) {
	if (list == null)
	    return null;
	return new ArrayList<>(list);
    }

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
	    return new ArrayList<>();
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
	return new ArrayList<>(new HashSet<>(list));
    }

    public static <T> Set<T> ensureUniqueness(Iterable<T> iterable) {
	Set<T> uniqeSet = new HashSet<>();
	for (T t : iterable)
	    uniqeSet.add(t);
	return uniqeSet;
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
	List<T> filteredList = new ArrayList<>();
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
     * Iterate over a list and operate consumers over the elements. The method
     * accept a BiPredicate that indicate if two elements need to be consumed
     * together or not. The BiPredicate is tested only on followings elements.
     * 
     * @param l
     *            the list
     * @param p
     *            the BiPredicate that indicate if two elements need to be
     *            consumed together or not
     * @param c
     *            consumer used to consume single elements if the predicate
     *            returned false on two elements
     * @param bc
     *            BiConsumer used to consume two elements if the predicate
     *            returned true on two elements
     */
    @SuppressWarnings("unchecked")
    public static <T> void forEach(List<? extends T> l, BiPredicate<? super T, ? super T> p, Consumer<? super T> c,
	    BiConsumer<? super T, ? super T> bc) {
	Object[] arr = l.toArray();
	int length = l.size() - 1;
	for (int i = 0; i < length; i++) {
	    T a = (T) arr[i];
	    T b = (T) arr[i + 1];
	    if (p.test(a, b)) {
		bc.accept(a, b);
		i++;
	    } else {
		c.accept(a);
	    }
	}

	T beforeLast = (T) arr[length - 1];
	T last = (T) arr[length];
	if (!p.test(beforeLast, last)) {
	    c.accept(last);
	}
    }

    /**
     * Break string text to words (treat tabs as spaces, ignore multiple spaces
     * and tabs in a row)
     * 
     * @param text
     *            the text to break
     * @return list of all words in the text with spaces or tabs between them
     * @throws IllegalArgumentException
     *             if line is null
     */
    public static List<String> breakToWords(String text) {
	return breakToWords(text, "" + SPACE, "" + TAB);
    }

    /**
     * Break string text to words by input separator sequences (for example,
     * spaces and tabs)
     * 
     * @param text
     *            the text to break
     * @param separatorSequences
     *            list of sequences the method will treat as separators between
     *            words
     * @return list of all words in the text separated by the separatorSequences
     */
    public static List<String> breakToWords(String text, String... separatorSequences) {
	if (text == null) {
	    throw new IllegalArgumentException("line can't be null");
	}
	if (separatorSequences == null) {
	    throw new IllegalArgumentException("Seperator sequences can't be null");
	}

	List<String> words = new ArrayList<>();
	int[] nextSeparator = positionOf(text, separatorSequences);
	int separatorIndex = nextSeparator[0];
	int separatorLength = nextSeparator[1];

	while (separatorIndex >= 0) {
	    String word = text.substring(0, separatorIndex);
	    if (!word.isEmpty())
		words.add(word);
	    text = text.substring(separatorIndex + separatorLength);
	    nextSeparator = positionOf(text, separatorSequences);
	    separatorIndex = nextSeparator[0];
	    separatorLength = nextSeparator[1];
	}
	// Last word
	if (!text.isEmpty()) {
	    words.add(text);
	}

	return words;
    }

    /**
     * Add a word to a text. Will add 'space' if last character is not space or
     * tab
     * 
     * @param text
     *            current text
     * @param word
     *            next word in the text
     * @return new text with the word at the end of it
     */
    public static String addWord(String text, String word) {
	return addWord(text, word, false);
    }

    /**
     * Add a word to a text. Will add 'space' or 'tab' (by user choice) if last
     * character is not space or tab
     * 
     * @param text
     *            current text
     * @param word
     *            next word in the text
     * @param tab
     *            if true, a tab will be inserted if needed, else space will be
     *            used
     * @return new text with the word at the end of it
     */
    public static String addWord(String text, String word, boolean tab) {
	StringBuilder builder = new StringBuilder(text);
	addWord(builder, word, tab);
	return builder.toString();
    }

    /**
     * Add a word to a builder
     * 
     * @param builder
     *            the text builder
     * @param word
     *            word to add
     * @param tab
     *            if true, will use tab between words, else - space
     */
    public static void addWord(StringBuilder builder, String word, boolean tab) {
	if (word == null) {
	    throw new IllegalArgumentException("Arguments can't be null");
	}
	if (builder.length() != 0) {
	    char lastChar = builder.charAt(builder.length() - 1);
	    char spacer = tab ? TAB : SPACE;
	    builder.append((lastChar != SPACE && lastChar != TAB) ? spacer : "");
	}
	builder.append(word);
    }

    /**
     * Generate a string message of 'expected' and 'actual' case
     * 
     * @param expected
     *            the expected value
     * @param actual
     *            the actual value
     * @return string message representing the expectation
     */
    public static String format(Object expected, Object actual) {
	return "Expected <" + toString(expected) + "> actual <" + toString(actual) + ">";
    }

    /**
     * Evaluate the string representation of an object
     * 
     * @param o
     *            the object
     * @return a string representing the object
     */
    public static String toString(Object o) {
	String str = Arrays.deepToString(new Object[] { o });
	str = str.substring(1);
	str = str.substring(0, str.length() - 1);
	return str;
    }

    /**
     * Checks if two objects are equals
     * 
     * @param o1
     *            first object
     * @param o2
     *            second object
     * @return true if the two objects are equal
     */
    public static boolean equals(Object o1, Object o2) {
	return Arrays.deepEquals(new Object[] { o1 }, new Object[] { o2 });
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

    /**
     * Return the first index of one of the sequences
     * 
     * @param text
     *            the searched text
     * @param sequences
     *            list of searched sequences
     * @return first index of one of the sequences in the text or -1 if non
     *         found
     */
    public static int indexOf(String text, String... sequences) {
	return positionOf(text, sequences)[0];
    }

    /**
     * Return the last index of one of the sequences
     * 
     * @param text
     *            the searched text
     * @param sequences
     *            list of searched sequences
     * @return last index of one of the sequences in the text or -1 if non found
     */
    public static int lastIndexOf(String text, String... sequences) {
	return lastPositionOf(text, sequences)[0];
    }

    /**
     * Return pair of index and length of the first sequence out of the input
     * sequences
     * 
     * @param text
     *            the searched text
     * @param sequences
     *            list of searched sequences
     * @return pair of index and length of the found sequence or -1 in the index
     *         field if non found
     */
    public static int[] positionOf(String text, String... sequences) {
	int index = -1;
	int length = -1;
	for (String seq : sequences) {
	    int seqIndex = text.indexOf(seq);
	    if (seqIndex != -1 && (index == -1 || seqIndex < index)) {
		index = seqIndex;
		length = seq.length();
	    }
	}
	return new int[] { index, length };
    }

    /**
     * Return pair of index and length of the last sequence out of the input
     * sequences
     * 
     * @param text
     *            the searched text
     * @param sequences
     *            list of searched sequences
     * @return pair of index and length of the found sequence or -1 in the index
     *         field if non found
     */
    public static int[] lastPositionOf(String text, String... sequences) {
	int index = -1;
	int length = -1;
	for (String seq : sequences) {
	    int seqIndex = text.lastIndexOf(seq);
	    if (seqIndex > index) {
		index = seqIndex;
		length = seq.length();
	    }
	}
	return new int[] { index, length };
    }

}
