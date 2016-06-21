package breakingtherules.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * The Utility class provide a set of static helper methods.
 * <p>
 * All method are static.
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 */
public class Utility {

    /**
     * Space character.
     */
    public static final char SPACE = ' ';

    /**
     * Tab character.
     */
    public static final char TAB = '\t';

    /**
     * Space string.
     */
    static final String SPACE_STR = String.valueOf(SPACE);

    /**
     * Tab string.
     */
    static final String TAB_STR = String.valueOf(TAB);

    // Suppresses default constructor, ensuring non-instantiability.
    private Utility() {
    }

    /**
     * Put a value in a list at an index even if the list is too small.
     * <p>
     * This method simulate the list as array that allow random access even if
     * the list wasn't grown naturally by <code>List.add(E e)</code>. The list
     * will be appended by nulls up to the desire index if needed
     * 
     * @param <T>
     *            type of list elements
     * 
     * @param list
     *            the list
     * @param index
     *            index in the list
     * @param value
     *            new value in the list
     */
    public static <T> void put(final List<? super T> list, final int index, T value) {
	if (index < 0) {
	    throw new IndexOutOfBoundsException("index must be positive " + index);
	}
	for (int lSize = list.size(); lSize <= index; lSize++) {
	    list.add(null);
	}
	list.set(index, value);
    }

    /**
     * Get a sub list of a list by offset and size
     * 
     * @param <T>
     *            type of list elements
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
    public static <T> List<T> subList(final List<? extends T> list, final int offset, final int size) {
	if (offset < 0 || size < 0) {
	    throw new IllegalArgumentException("offset and size should be positive (" + offset + ", " + size + ")");
	}
	if (offset >= list.size()) {
	    return new ArrayList<>();
	}

	// Clone sub list because List.SubList(...) save a reference to the
	// original list and therefore, the whole list is always kept in memory.
	return newArrayList(list.subList(offset, Math.min(list.size(), offset + size)));
    }

    public static <E> List<E> subList(final Iterable<? extends E> iterable, final int offset, final int size) {
	if (offset < 0 || size < 0) {
	    throw new IllegalArgumentException("offset and size should be positive (" + offset + ", " + size + ")");
	}
	final List<E> list = new ArrayList<>();
	int index;
	final Iterator<? extends E> it = iterable.iterator();
	for (index = 0; it.hasNext() && index < offset; index++) {
	    it.next();
	}
	for (final int fence = offset + size; it.hasNext() && index < fence; index++) {
	    list.add(it.next());
	}
	return list;
    }

    public static <E> List<E> subListView(final List<E> list, final int offset, final int size) {
	if (offset < 0 || size < 0) {
	    throw new IllegalArgumentException("offset and size should be positive (" + offset + ", " + size + ")");
	}
	return list.subList(Math.min(offset, list.size()) - 1, Math.min(offset + size, list.size()));
    }

    /**
     * Create new array list and initialize it with elements from an iterable
     * object.
     * <p>
     * This method is preferred over
     * {@link ArrayList#ArrayList(java.util.Collection)} for collection iterable
     * because it won't call {@link Collection#toArray()};
     * 
     * @param <T>
     *            the type of the elements in the new array list.
     * @param iterable
     *            the iterable that contains the initialize elements of the
     *            array list.
     * @return new array list with the collection elements.
     * @throws NullPointerException
     *             if the collection is null.
     */
    public static <T> ArrayList<T> newArrayList(final Iterable<? extends T> iterable) {
	final ArrayList<T> arrayList;
	if (iterable instanceof Collection) {
	    final Collection<? extends T> coll = (Collection<? extends T>) iterable;
	    int size = coll.size();
	    arrayList = new ArrayList<>(size);
	    for (final Iterator<? extends T> it = coll.iterator(); size-- != 0;) {
		arrayList.add(it.next());
	    }
	} else {
	    arrayList = new ArrayList<>();
	    for (final Iterator<? extends T> it = iterable.iterator(); it.hasNext();) {
		arrayList.add(it.next());
	    }
	}
	return arrayList;
    }

    /**
     * Count the number of occurrences of a character in a string.
     * 
     * @param st
     *            the string.
     * @param ch
     *            the searched character.
     * @return the number of occurrences of the character in the string.
     */
    public static int countOccurrencesOf(final String st, final char ch) {
	int c = 0;
	for (int i = st.length(); i-- > 0;) {
	    if (st.charAt(i) == ch) {
		c++;
	    }
	}
	return c;
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
    public static String[] breakToWords(final String text) {
	return breakToWords(text, SPACE, TAB);
    }

    public static String[] breakToWords(final String text, final char separator) {
	String[] words = new String[4];
	int wordsCount = 0;

	int fromIndex = 0;
	int separatorIndex = text.indexOf(separator);
	while (separatorIndex >= 0) {
	    if (fromIndex != separatorIndex) {
		if (words.length <= wordsCount) {
		    words = expand(words);
		}
		words[wordsCount++] = text.substring(fromIndex, separatorIndex);
	    }
	    fromIndex = separatorIndex + 1;
	    separatorIndex = text.indexOf(separator, fromIndex);
	}
	// Last word
	if (fromIndex != text.length()) {
	    if (words.length <= wordsCount) {
		words = expand(words);
	    }
	    words[wordsCount++] = text.substring(fromIndex);
	}

	return words.length == wordsCount ? words : trim(words, wordsCount);
    }

    public static String[] breakToWords(final String text, final String separator) {
	String[] words = new String[4];
	int wordsCount = 0;

	final int separatorLength = separator.length();
	int fromIndex = 0;
	int separatorIndex = text.indexOf(separator);
	while (separatorIndex >= 0) {
	    if (fromIndex != separatorIndex) {
		if (words.length <= wordsCount) {
		    words = expand(words);
		}
		words[wordsCount++] = text.substring(fromIndex, separatorIndex);
	    }
	    fromIndex = separatorIndex + separatorLength;
	    separatorIndex = text.indexOf(separator, fromIndex);
	}
	// Last word
	if (fromIndex != text.length()) {
	    if (words.length <= wordsCount) {
		words = expand(words);
	    }
	    words[wordsCount++] = text.substring(fromIndex);
	}

	return words.length == wordsCount ? words : trim(words, wordsCount);
    }

    public static String[] breakToWords(final String text, final char... separatorCharss) {
	String[] words = new String[4];
	int wordsCount = 0;

	int separatorIndex = indexOf(text, separatorCharss);
	int fromIndex = 0;

	while (separatorIndex >= 0) {
	    if (fromIndex != separatorIndex) {
		if (words.length <= wordsCount) {
		    words = expand(words);
		}
		words[wordsCount++] = text.substring(fromIndex, separatorIndex);
	    }
	    fromIndex = separatorIndex + 1;
	    separatorIndex = indexOf(text, fromIndex, separatorCharss);
	}
	// Last word
	if (fromIndex != text.length()) {
	    if (words.length <= wordsCount) {
		words = expand(words);
	    }
	    words[wordsCount++] = text.substring(fromIndex);
	}

	return words.length == wordsCount ? words : trim(words, wordsCount);
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
    public static String[] breakToWords(final String text, final String... separatorSequences) {
	String[] words = new String[4];
	int wordsCount = 0;

	int[] nextSeparator = positionOf(text, separatorSequences);
	int separatorIndex = nextSeparator[0];
	int separatorLength = nextSeparator[1];
	int fromIndex = 0;

	while (separatorIndex >= 0) {
	    if (fromIndex != separatorIndex) {
		if (words.length <= wordsCount) {
		    words = expand(words);
		}
		words[wordsCount++] = text.substring(fromIndex, separatorIndex);
	    }
	    fromIndex = separatorIndex + separatorLength;
	    nextSeparator = positionOf(text, fromIndex, separatorSequences);
	    separatorIndex = nextSeparator[0];
	    separatorLength = nextSeparator[1];
	}
	// Last word
	if (fromIndex != text.length()) {
	    if (words.length <= wordsCount) {
		words = expand(words);
	    }
	    words[wordsCount++] = text.substring(fromIndex);
	}

	return words.length == wordsCount ? words : trim(words, wordsCount);
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
    public static String addWord(final String text, final String word) {
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
    public static String addWord(final String text, final String word, final boolean tab) {
	final StringBuilder builder = new StringBuilder(text);
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
    public static void addWord(final StringBuilder builder, final String word, final boolean tab) {
	if (builder.length() != 0) {
	    final char lastChar = builder.charAt(builder.length() - 1);
	    final char spacer = tab ? TAB : SPACE;
	    builder.append((lastChar != SPACE && lastChar != TAB) ? "" + spacer : "");
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
    public static String formatEqual(final int expected, final int actual) {
	return formatEqual(Integer.valueOf(expected), Integer.valueOf(actual));
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
    public static String formatEqual(final Object expected, final Object actual) {
	return "expected <" + toString(expected) + "> actual <" + toString(actual) + ">";
    }

    /**
     * Format a range message.
     * 
     * @param lower
     *            lower bound of the range.
     * @param upper
     *            upper bound of the range.
     * @param actual
     *            actual value.
     * @return string representation of the range expectation.
     */
    public static String formatRange(final int lower, final int upper, final int actual) {
	return formatRange(Integer.valueOf(lower), Integer.valueOf(upper), Integer.valueOf(actual));
    }

    /**
     * Format a range message.
     * 
     * @param lower
     *            lower bound of the range.
     * @param upper
     *            upper bound of the range.
     * @param actual
     *            actual value.
     * @return string representation of the range expectation.
     */
    public static String formatRange(final Number lower, final Number upper, final Number actual) {
	return "expected to be in range [" + toString(lower) + ", " + toString(upper) + "], actual <" + toString(actual)
		+ ">";
    }

    /**
     * Evaluate the string representation of an object
     * 
     * @param o
     *            the object
     * @return a string representing the object
     */
    public static String toString(final Object o) {
	String str = Arrays.deepToString(new Object[] { o });
	str = str.substring(1);
	str = str.substring(0, str.length() - 1);
	return str;
    }

    /**
     * Return a string representation of sum elements as they where in array.
     * <p>
     * For example:<br>
     * input: 1,3,0,5<br>
     * output: [1, 3, 0, 5]<br>
     * 
     * @param os
     *            the input objects.
     * @return string representation of the objects as they where in a array.
     */
    public static String toStringArray(final Object... os) {
	return Arrays.deepToString(os);
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
    public static boolean deepEquals(final Object o1, final Object o2) {
	return Arrays.deepEquals(new Object[] { o1 }, new Object[] { o2 });
    }

    /**
     * Log of 2 in base e.
     */
    private static final double LOG2 = StrictMath.log(2);

    private static final double LOG2_INVERSE = (1 + 1e-10) / LOG2;

    /**
     * Log 2 of a number
     * 
     * @param num
     *            the number
     * @return log of base 2 of the number
     */
    public static double log2(final double num) {
	return StrictMath.log(num) * LOG2_INVERSE;
    }

    public static int indexOf(final String text, final char... chars) {
	int index = -1;
	for (final char ch : chars) {
	    final int seqIndex = text.indexOf(ch);
	    if (seqIndex != -1 && (index == -1 || seqIndex < index)) {
		index = seqIndex;
	    }
	}
	return index;
    }

    public static int indexOf(final String text, final int fromIndex, final char... chars) {
	int index = -1;
	for (final char ch : chars) {
	    final int seqIndex = text.indexOf(ch, fromIndex);
	    if (seqIndex != -1 && (index == -1 || seqIndex < index)) {
		index = seqIndex;
	    }
	}
	return index;
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
    public static int indexOf(final String text, final String... sequences) {
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
    public static int lastIndexOf(final String text, final String... sequences) {
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
    public static int[] positionOf(final String text, final String... sequences) {
	int index = -1;
	int length = -1;
	for (final String seq : sequences) {
	    final int seqIndex = text.indexOf(seq);
	    if (seqIndex != -1 && (index == -1 || seqIndex < index)) {
		index = seqIndex;
		length = seq.length();
	    }
	}
	return new int[] { index, length };
    }

    public static int[] positionOf(final String text, final int fromIndex, final String... sequences) {
	int index = -1;
	int length = -1;
	for (final String seq : sequences) {
	    final int seqIndex = text.indexOf(seq, fromIndex);
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
    public static int[] lastPositionOf(final String text, final String... sequences) {
	int index = -1;
	int length = -1;
	for (final String seq : sequences) {
	    final int seqIndex = text.lastIndexOf(seq);
	    if (seqIndex > index) {
		index = seqIndex;
		length = seq.length();
	    }
	}
	return new int[] { index, length };
    }

    private static String[] expand(final String[] array) {
	final String[] newArray = new String[array.length * 2 + 1];
	System.arraycopy(array, 0, newArray, 0, array.length);
	return newArray;
    }

    private static String[] trim(final String[] array, final int newLength) {
	final String[] newArray = new String[newLength];
	System.arraycopy(array, 0, newArray, 0, newLength);
	return newArray;
    }

}
