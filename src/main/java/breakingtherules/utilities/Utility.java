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
 * 
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

    /**
     * Suppresses default constructor, ensuring non-instantiability.
     */
    private Utility() {
    }

    /**
     * Put a value in a list at an index even if the list is too small.
     * <p>
     * This method simulate the list as array that allow random access even if
     * the list wasn't grown naturally by <code>List.add(E e)</code>. The list
     * will be appended by nulls up to the desire index if needed
     * 
     * @param <E>
     *            type of list elements
     * @param list
     *            the list
     * @param index
     *            index in the list
     * @param value
     *            new value in the list
     */
    public static <E> void put(final List<? super E> list, final int index, E value) {
	if (index < 0) {
	    throw new IndexOutOfBoundsException("index must be positive " + index);
	}
	for (int lSize = list.size(); lSize <= index; lSize++) {
	    list.add(null);
	}
	list.set(index, value);
    }

    /**
     * Create a new sub list of an iterable.
     * 
     * @param <E>
     *            type of element.
     * @param iterable
     *            the iterable source.
     * @param offset
     *            the offset in the iterable.
     * @param size
     *            the requested sub list size.
     * @return new sub list of the iterable source.
     * @throws IllegalArgumentException
     *             if the offset or the size are negative.
     */
    public static <E> List<E> subList(final Iterable<? extends E> iterable, final int offset, final int size) {
	if (offset < 0 || size < 0) {
	    throw new IllegalArgumentException("offset and size should be positive (" + offset + ", " + size + ")");
	}
	if (iterable instanceof List) {
	    List<? extends E> list = (List<? extends E>) iterable;
	    if (offset >= list.size()) {
		return new ArrayList<>();
	    }

	    // Clone sub list because List.SubList(...) save a reference to the
	    // original list and therefore, the whole list is always kept in
	    // memory.
	    return newArrayList(list.subList(offset, Math.min(list.size(), offset + size)));
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

    /**
     * Create a sub list view on existing list.
     * <p>
     * The user of this method should know that this method doesn't create new
     * list and modifies on the returned list will modified the input list. Also
     * the return list will keep the original list in the memory as long as the
     * sublist is in use.
     * 
     * @param <E>
     *            type of the list elements.
     * @param list
     *            the input list.
     * @param offset
     *            offset in the original list.
     * @param size
     *            requested size of the sublist view.
     * @return sublist view on the original list.
     */
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
     * Parses positive integer from a string.
     * 
     * @param s
     *            the string.
     * @return integer parse from the string.
     * @throws IllegalArgumentException
     *             if can't parse to integer.
     */
    public static int parsePositiveInt(final String s) {
	return parsePositiveInt(s, 0, s.length());
    }

    /**
     * Parses positive integer from a string.
     * 
     * @param s
     *            the string.
     * @param fromIndex
     *            the index from which to start the search.
     * @param toIndex
     *            the index where to end the search.
     * @return integer parse from the specified interval in the text.
     * @throws IllegalArgumentException
     *             if can't parse to integer.
     */
    public static int parsePositiveInt(final String s, final int fromIndex, final int toIndex) {
	if (fromIndex >= toIndex) {
	    throw new IllegalArgumentException("Empty number. " + s);
	}

	/*
	 * Most of this method's code is copied from Integer.parseInt(String),
	 * but this code is essential for fast parsing of large strings without
	 * using the String.substring(int, int) method which create new string
	 * and can slow the parsing. Also this method is less generic then
	 * Integer.parseInt(String) therefore can be faster.
	 */

	int result = 0;
	int i = fromIndex;

	while (i < toIndex) {
	    final int digit = s.charAt(i++) - '0';
	    if (digit < 0 || digit > 9) {
		throw new IllegalArgumentException("invalid string: " + s.substring(fromIndex, toIndex));
	    }
	    if (result < -214748364) {
		throw new IllegalArgumentException("invalid string: " + s.substring(fromIndex, toIndex));
	    }
	    result *= 10;
	    if (result < -Integer.MAX_VALUE + digit) {
		throw new IllegalArgumentException("invalid string: " + s.substring(fromIndex, toIndex));
	    }
	    result -= digit;
	}
	return -result;
    }

    /**
     * Parses positive integer from a string without checking overflows, but
     * with limitation on the number digits count.
     * <p>
     * This method is safer then
     * {@link #parsePositiveIntUncheckedOverflow(String, int, int)} because it's
     * limits the number of digits of the number, therefore can avoid overflows.
     * This method still needs to be used carefully because if the
     * {@code maxDitigs} is more then 9, then overflow still can happen.
     * 
     * @param s
     *            the string.
     * @param fromIndex
     *            the index from which to start the search.
     * @param toIndex
     *            the index where to end the search.
     * @param maxDigits
     *            max allowed digits. Should be less or equal to 9.
     * @return integer parse from the specified interval in the text.
     * @throws IllegalArgumentException
     *             if {@code maxDigits} is greater then 9, or the specified
     *             interval is bigger then {@code maxDigits} or failed to parse
     *             to integer.
     */
    public static int parsePositiveIntUncheckedOverflow(final String s, final int fromIndex, final int toIndex,
	    final int maxDigits) {
	if (toIndex - fromIndex > maxDigits) {
	    throw new IllegalArgumentException("Invalid string: " + s.substring(fromIndex, toIndex));
	}
	return parsePositiveIntUncheckedOverflow(s, fromIndex, toIndex);
    }

    /**
     * Parses positive integer from a string without checking overflows.
     * <p>
     * An <b>UNSAFE</b> parsing method of positive integers that doesn't checks
     * for overflows of the result value.
     * 
     * @param s
     *            the string.
     * @param fromIndex
     *            the index from which to start the search.
     * @param toIndex
     *            the index where to end the search.
     * @return integer parse from the specified interval in the text.
     * @throws IllegalArgumentException
     *             if can't parse to integer.
     * @deprecated this method is not safe for the general use of parsing
     *             integers. This method should be used carefully and only if
     *             the input string length is known and it's doesn't overflow.
     *             Use
     *             {@link #parsePositiveIntUncheckedOverflow(String, int, int, int)}
     *             for safer parsing with less checks.
     */
    @Deprecated
    public static int parsePositiveIntUncheckedOverflow(final String s, final int fromIndex, final int toIndex) {
	if (fromIndex >= toIndex) {
	    throw new IllegalArgumentException("Empty number. " + s);
	}

	int result = 0;
	int i = fromIndex;

	while (i < toIndex) {
	    final int digit = s.charAt(i++) - '0';
	    if (digit < 0 || digit > 9) {
		throw new IllegalArgumentException("invalid string: " + s.substring(fromIndex, toIndex));
	    }
	    result = result * 10 + digit;
	}
	return result;
    }

    /**
     * Get the count of digits of a <b>positive</b> number.
     * 
     * @param x
     *            a positive number.
     * @return the number of digits on base 10 of {@code x}.
     */
    public static int digitsCount(final int x) {

	/*
	 * This implementation is not readable, but it's the fastest one we
	 * found.
	 */

	if (x < 100000) {
	    // 5 or less
	    if (x < 100) {
		// 1 or 2
		if (x < 10)
		    return 1;
		return 2;
	    }
	    // 3 or 4 or 5
	    if (x < 1000)
		return 3;
	    // 4 or 5
	    if (x < 10000)
		return 4;
	    return 5;
	}
	// 6 or more
	if (x < 10000000) {
	    // 6 or 7
	    if (x < 1000000)
		return 6;
	    return 7;
	}
	// 8 to 10
	if (x < 100000000)
	    return 8;
	// 9 or 10
	if (x < 1000000000)
	    return 9;
	return 10;
    }

    /**
     * Break string text to words (treat tabs as spaces, ignore multiple spaces
     * and tabs in a row)
     * 
     * @param text
     *            the text to break
     * @return all words in the text with spaces or tabs between them
     * @throws NullPointerException
     *             if line is null
     */
    public static String[] breakToWords(final String text) {
	return breakToWords(text, SPACE, TAB);
    }

    /**
     * Break string text to words and separate the words by the input separator.
     * 
     * @param text
     *            the text to break.
     * @param separator
     *            the char separator used to separate between words.
     * @return all words in the text with the separator between them.
     */
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

    /**
     * Break string text to words and separate the words by the input separator.
     * 
     * @param text
     *            the text to break.
     * @param separator
     *            the string separator used to separate between words.
     * @return all words in the text with the separator between them.
     */
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

    /**
     * Break string text to words and separate the words by the input
     * separators.
     * 
     * @param text
     *            the text to break.
     * @param separatorChars
     *            the chars separator used to separate between words.
     * @return all words in the text with one of the separators between them.
     */
    public static String[] breakToWords(final String text, final char... separatorChars) {
	String[] words = new String[4];
	int wordsCount = 0;

	int separatorIndex = indexOf(text, separatorChars);
	int fromIndex = 0;

	while (separatorIndex >= 0) {
	    if (fromIndex != separatorIndex) {
		if (words.length <= wordsCount) {
		    words = expand(words);
		}
		words[wordsCount++] = text.substring(fromIndex, separatorIndex);
	    }
	    fromIndex = separatorIndex + 1;
	    separatorIndex = indexOf(text, fromIndex, separatorChars);
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

    /**
     * The inverse of {@link #LOG2}.
     */
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

    /**
     * Find the first index of one of the input chars.
     * 
     * @param text
     *            the text.
     * @param chars
     *            the searched chars.
     * @return the first index of one of the characters or -1 if non found.
     */
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

    /**
     * Find the first index of one of the input chars, starting the search from
     * a specified index.
     * 
     * @param text
     *            the text.
     * @param fromIndex
     *            the first index to start the search from.
     * @param chars
     *            the searched chars.
     * @return the first index of one of the characters or -1 if non found.
     */
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

    /**
     * Return pair of index and length of the last sequence out of the input
     * sequences, starting the search from specified index.
     * 
     * @param text
     *            the searched text
     * @param fromIndex
     *            the index to start the search from.
     * @param sequences
     *            list of searched sequences
     * @return pair of index and length of the found sequence or -1 in the index
     *         field if non found
     */
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

    /**
     * Doubling the input array length and copy the date to the new array.
     * 
     * @param array
     *            the original array.
     * @return new array with twice the size of the original array, with the
     *         data copied from the original array.
     */
    private static String[] expand(final String[] array) {
	final String[] newArray = new String[array.length * 2 + 1];
	System.arraycopy(array, 0, newArray, 0, array.length);
	return newArray;
    }

    /**
     * Trim an array to a new length.
     * 
     * @param array
     *            the original array.
     * @param newLength
     *            the new length of the array.
     * @return smaller array with the specified length and first
     *         {@code newLength} elements from the original array copied.
     */
    private static String[] trim(final String[] array, final int newLength) {
	final String[] newArray = new String[newLength];
	System.arraycopy(array, 0, newArray, 0, newLength);
	return newArray;
    }

}
