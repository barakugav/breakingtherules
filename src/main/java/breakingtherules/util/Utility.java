package breakingtherules.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
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
     * Log of 2 in base e.
     */
    private static final double LOG2 = StrictMath.log(2);

    /**
     * The inverse of {@link #LOG2}.
     */
    private static final double LOG2_INVERSE = (1 + 1e-10) / LOG2;

    /**
     * Suppresses default constructor, ensuring non-instantiability.
     */
    private Utility() {
    }

    /**
     * Add a word to a string.
     * <p>
     * Will add 'space' if last character is not space or tab
     *
     * @param s
     *            a string.
     * @param word
     *            next word in the string.
     * @return new string with the word at the end of it
     * @throws NullPointerException
     *             if the string is null.
     */
    public static String addWord(final String s, final String word) {
	return addWord(s, word, false);
    }

    /**
     * Add a word to a string.
     * <p>
     * Will add 'space' or 'tab' (by user choice) if last character is not space
     * or tab.
     *
     * @param s
     *            a string.
     * @param word
     *            next word in the string.
     * @param tab
     *            if true, a tab will be inserted if needed, else space will be
     *            used.
     * @return new string with the word at the end of it.
     * @throws NullPointerException
     *             if the string is null.
     */
    public static String addWord(final String s, final String word, final boolean tab) {
	final StringBuilder builder = new StringBuilder(s);
	addWord(builder, word, tab);
	return builder.toString();
    }

    /**
     * Add a word to a string builder.
     *
     * @param builder
     *            a string builder.
     * @param word
     *            word to add
     * @param tab
     *            if true, will use tab between words, else - space.
     * @throws NullPointerException
     *             if the builder is null.
     */
    public static void addWord(final StringBuilder builder, final String word, final boolean tab) {
	if (builder.length() != 0) {
	    final char lastChar = builder.charAt(builder.length() - 1);
	    final char spacer = tab ? TAB : SPACE;
	    if (lastChar != SPACE && lastChar != TAB)
		builder.append(spacer);
	}
	builder.append(word);
    }

    /**
     * Break string to words.
     * <p>
     * treat tabs as spaces, ignore multiple spaces and tabs in a row.
     *
     * @param s
     *            the text to break
     * @return all words in the text with spaces or tabs between them
     * @throws NullPointerException
     *             if test is null.
     */
    public static String[] breakToWords(final String s) {
	return breakToWords(s, SPACE, TAB);
    }

    /**
     * Break string text to words and separate the words by the input separator.
     *
     * @param text
     *            the text to break.
     * @param separator
     *            the char separator used to separate between words.
     * @return all words in the text with the separator between them.
     * @throws NullPointerException
     *             if test is null.
     */
    public static String[] breakToWords(final String text, final char separator) {
	String[] words = new String[4];
	int wordsCount = 0;

	int fromIndex = 0;
	int separatorIndex = text.indexOf(separator);
	while (separatorIndex >= 0) {
	    if (fromIndex != separatorIndex) {
		if (words.length <= wordsCount)
		    words = expand(words);
		words[wordsCount++] = text.substring(fromIndex, separatorIndex);
	    }
	    fromIndex = separatorIndex + 1;
	    separatorIndex = text.indexOf(separator, fromIndex);
	}
	// Last word
	if (fromIndex != text.length()) {
	    if (words.length <= wordsCount)
		words = expand(words);
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

	int separatorIndex = indexOf(text, 0, separatorChars);
	int fromIndex = 0;

	while (separatorIndex >= 0) {
	    if (fromIndex != separatorIndex) {
		if (words.length <= wordsCount)
		    words = expand(words);
		words[wordsCount++] = text.substring(fromIndex, separatorIndex);
	    }
	    fromIndex = separatorIndex + 1;
	    separatorIndex = indexOf(text, fromIndex, separatorChars);
	}
	// Last word
	if (fromIndex != text.length()) {
	    if (words.length <= wordsCount)
		words = expand(words);
	    words[wordsCount++] = text.substring(fromIndex);
	}

	return words.length == wordsCount ? words : trim(words, wordsCount);
    }

    /**
     * Break string string to words and separate the words by two input
     * character separator.
     *
     * @param s
     *            the string to break.
     * @param ch1
     *            the first separator.
     * @param ch2
     *            the second separator.
     * @return all words in the text with the separators between them.
     */
    public static String[] breakToWords(final String s, final char ch1, final char ch2) {
	String[] words = new String[4];
	int wordsCount = 0;

	int separatorIndex = indexOf(s, 0, ch1, ch2);
	int fromIndex = 0;

	while (separatorIndex >= 0) {
	    if (fromIndex != separatorIndex) {
		if (words.length <= wordsCount)
		    words = expand(words);
		words[wordsCount++] = s.substring(fromIndex, separatorIndex);
	    }
	    fromIndex = separatorIndex + 1;
	    separatorIndex = indexOf(s, fromIndex, ch1, ch2);
	}
	// Last word
	if (fromIndex != s.length()) {
	    if (words.length <= wordsCount)
		words = expand(words);
	    words[wordsCount++] = s.substring(fromIndex);
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
		if (words.length <= wordsCount)
		    words = expand(words);
		words[wordsCount++] = text.substring(fromIndex, separatorIndex);
	    }
	    fromIndex = separatorIndex + separatorLength;
	    separatorIndex = text.indexOf(separator, fromIndex);
	}
	// Last word
	if (fromIndex != text.length()) {
	    if (words.length <= wordsCount)
		words = expand(words);
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
		if (words.length <= wordsCount)
		    words = expand(words);
		words[wordsCount++] = text.substring(fromIndex, separatorIndex);
	    }
	    fromIndex = separatorIndex + separatorLength;
	    nextSeparator = positionOf(text, fromIndex, separatorSequences);
	    separatorIndex = nextSeparator[0];
	    separatorLength = nextSeparator[1];
	}
	// Last word
	if (fromIndex != text.length()) {
	    if (words.length <= wordsCount)
		words = expand(words);
	    words[wordsCount++] = text.substring(fromIndex);
	}

	return words.length == wordsCount ? words : trim(words, wordsCount);
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
     * Get the count of digits of a <b>positive</b> number.
     *
     * @param x
     *            a positive number.
     * @return the number of digits on base 10 of {@code x}.
     * @throws IllegalArgumentException
     *             if {@code x} is negative.
     */
    public static int digitsCount(final int x) {

	/*
	 * This implementation seems not very clever but if the input to this
	 * method is random, the method will finish after 1 operation in 53.5%
	 * of the times, 41.9% in 2 operations, 4.19% in 3 operations, 0.419% in
	 * 4 operations...
	 */

	if (x >= 1000000000)
	    return 10;
	if (x >= 100000000)
	    return 9;
	if (x >= 10000000)
	    return 8;
	if (x >= 1000000)
	    return 7;
	if (x >= 100000)
	    return 6;
	if (x >= 10000)
	    return 5;
	if (x >= 1000)
	    return 4;
	if (x >= 100)
	    return 3;
	if (x >= 10)
	    return 2;
	if (x >= 0)
	    return 1;
	throw new IllegalArgumentException("Negative number: " + x);
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
	return String.format("expected <%s> actual <%s>", toString(expected), toString(actual));
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
	return String.format("expected to be in range [%s, %s], actual <%s>", lower, upper, actual);
    }

    /**
     * Find the first index of one of the input chars in a string.
     *
     * @param s
     *            a string.
     * @param fromIndex
     *            the begin index to start the search from.
     * @param ch1
     *            the first searched char.
     * @param ch2
     *            the second searched char.
     * @return the first index of one of the characters in the string or -1 if
     *         non found.
     * @throws NullPointerException
     *             if the string is null.
     */
    public static int indexOf(final String s, final int fromIndex, final char ch1, final char ch2) {
	final int l = s.length();
	for (int i = fromIndex; i < l; i++) {
	    final char ch = s.charAt(i);
	    if (ch == ch1 || ch == ch2)
		return i;
	}
	return -1;
    }

    /**
     * Find the first index of one of the input chars, starting the search from
     * a specified index.
     *
     * @param s
     *            the text.
     * @param fromIndex
     *            the first index to start the search from.
     * @param chars
     *            the searched chars.
     * @return the first index of one of the characters or -1 if non found.
     */
    public static int indexOf(final String s, final int fromIndex, final char[] chars) {
	int index = -1;
	for (final char ch : chars) {
	    final int seqIndex = s.indexOf(ch, fromIndex);
	    if (seqIndex != -1 && (index == -1 || seqIndex < index))
		index = seqIndex;
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
     * Return pair of index and length of the last sequence out of the input
     * sequences in a string.
     *
     * @param s
     *            the searched string.
     * @param sequences
     *            list of searched sequences
     * @return pair of index and length of the found sequence or -1 in the index
     *         field if non found.
     * @throws NullPointerException
     *             if the string is null or the sequences array is null or one
     *             of the sequences is null.
     */
    public static int[] lastPositionOf(final String s, final String... sequences) {
	int index = -1;
	int length = -1;
	for (final String seq : sequences) {
	    final int seqIndex = s.lastIndexOf(seq);
	    if (seqIndex > index) {
		index = seqIndex;
		length = seq.length();
	    }
	}
	return new int[] { index, length };
    }

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
     * Create new array list and initialize it with elements from an iterable.
     * <p>
     * This method is preferred over
     * {@link ArrayList#ArrayList(java.util.Collection)} for collection iterable
     * because it won't call {@link Collection#toArray()};
     *
     * @param <E>
     *            the type of the elements in the new array list.
     * @param i
     *            the iterable that contains the initialize elements for the
     *            array list.
     * @return new array list with the iterable elements.
     * @throws NullPointerException
     *             if the iterable is null.
     */
    public static <E> ArrayList<E> newArrayList(final Iterable<? extends E> i) {
	final ArrayList<E> arrayList;
	if (i instanceof Collection) {
	    final Collection<? extends E> c = (Collection<? extends E>) i;
	    int s = c.size();
	    arrayList = new ArrayList<>(s);
	    for (final Iterator<? extends E> it = c.iterator(); s-- != 0;)
		arrayList.add(it.next());
	} else {
	    arrayList = new ArrayList<>();
	    for (final E name : i)
		arrayList.add(name);
	}
	return arrayList;
    }

    /**
     * Create new hash set and initialize it with elements from an iterable.
     * <p>
     *
     * @param <E>
     *            the type of the elements in the new hash set.
     * @param i
     *            the iterable that contains the initialize elements for the
     *            hash set.
     * @return new hash set with the iterable elements.
     * @throws NullPointerException
     *             if the iterable is null.
     */
    public static <E> HashSet<E> newHashSet(final Iterable<? extends E> i) {
	final HashSet<E> hashSet;
	if (i instanceof Collection) {
	    final Collection<? extends E> c = (Collection<? extends E>) i;
	    int s = c.size();
	    hashSet = new HashSet<>(s);
	    for (final Iterator<? extends E> it = c.iterator(); s-- != 0;)
		hashSet.add(it.next());
	} else {
	    hashSet = new HashSet<>();
	    for (final E name : i)
		hashSet.add(name);
	}
	return hashSet;
    }

    /**
     * Parses positive integer from a string.
     *
     * @param s
     *            the string.
     * @return integer parse from the string.
     * @throws IllegalArgumentException
     *             if can't parse to integer.
     * @throws NullPointerException
     *             if the string is null.
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
     * @throws NullPointerException
     *             if the string is null.
     */
    public static int parsePositiveInt(final String s, final int fromIndex, final int toIndex) {
	if (fromIndex >= toIndex)
	    throw new IllegalArgumentException("Empty number. " + s);

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
	    if (digit < 0 || digit > 9)
		throw new IllegalArgumentException("invalid string: " + s.substring(fromIndex, toIndex));
	    if (result < -214748364)
		throw new IllegalArgumentException("invalid string: " + s.substring(fromIndex, toIndex));
	    result *= 10;
	    if (result < -Integer.MAX_VALUE + digit)
		throw new IllegalArgumentException("invalid string: " + s.substring(fromIndex, toIndex));
	    result -= digit;
	}
	return -result;
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
	if (fromIndex >= toIndex)
	    throw new IllegalArgumentException("Empty number. " + s);

	int result = 0;
	int i = fromIndex;

	while (i < toIndex) {
	    final int digit = s.charAt(i++) - '0';
	    if (digit < 0 || digit > 9)
		throw new IllegalArgumentException("invalid string: " + s.substring(fromIndex, toIndex));
	    result = result * 10 + digit;
	}
	return result;
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
	if (toIndex - fromIndex > maxDigits)
	    throw new IllegalArgumentException("Invalid string: " + s.substring(fromIndex, toIndex));
	return parsePositiveIntUncheckedOverflow(s, fromIndex, toIndex);
    }

    /**
     * Return pair of index and length of the last sequence out of the input
     * sequences, starting the search from specified index.
     *
     * @param s
     *            the searched string.
     * @param fromIndex
     *            the index to start the search from.
     * @param sequences
     *            list of searched sequences
     * @return pair of index and length of the found sequence or -1 in the index
     *         field if non found.
     * @throws NullPointerException
     *             if the string is null, or the sequences array is null or one
     *             of the sequences is null.
     */
    public static int[] positionOf(final String s, final int fromIndex, final String... sequences) {
	int index = -1;
	int length = -1;
	for (final String seq : sequences) {
	    final int seqIndex = s.indexOf(seq, fromIndex);
	    if (seqIndex != -1 && (index == -1 || seqIndex < index)) {
		index = seqIndex;
		length = seq.length();
	    }
	}
	return new int[] { index, length };
    }

    /**
     * Return pair of index and length of the first sequence out of the input
     * sequences
     *
     * @param s
     *            a string.
     * @param sequences
     *            list of searched sequences
     * @return pair of index and length of the found sequence or -1 in the index
     *         field if non found
     * @throws NullPointerException
     *             if the string is null, or the sequences array is null or one
     *             of the sequences is null.
     */
    public static int[] positionOf(final String s, final String... sequences) {
	return positionOf(s, 0, sequences);
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
     *            new value in the list.
     * @throws NullPointerException
     *             if the list is null.
     * @throws IndexOutOfBoundsException
     *             if the index is negative.
     */
    public static <E> void put(final List<? super E> list, final int index, final E value) {
	if (index < 0)
	    throw new IndexOutOfBoundsException("index must be positive " + index);
	for (int lSize = list.size(); lSize <= index; lSize++)
	    list.add(null);
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
     * @throws NullPointerException
     *             if the iterable is null.
     * @throws IllegalArgumentException
     *             if the offset or the size are negative.
     */
    public static <E> List<E> subList(final Iterable<? extends E> iterable, final int offset, final int size) {
	if (offset < 0 || size < 0)
	    throw new IllegalArgumentException("offset and size should be positive (" + offset + ", " + size + ")");
	if (iterable instanceof List) {
	    final List<? extends E> list = (List<? extends E>) iterable;
	    if (offset >= list.size())
		return new ArrayList<>();

	    // Clone sub list because List.SubList(...) save a reference to the
	    // original list and therefore, the whole list is always kept in
	    // memory.
	    return newArrayList(list.subList(offset, Math.min(list.size(), offset + size)));
	}

	final List<E> list = new ArrayList<>();
	int index;
	final Iterator<? extends E> it = iterable.iterator();
	for (index = 0; it.hasNext() && index < offset; index++)
	    it.next();
	for (final int fence = offset + size; it.hasNext() && index < fence; index++)
	    list.add(it.next());
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
     * @throws NullPointerException
     *             if the list is null.
     * @throws IllegalArgumentException
     *             if the offset or size are negative.
     */
    public static <E> List<E> subListView(final List<E> list, final int offset, final int size) {
	if (offset < 0 || size < 0)
	    throw new IllegalArgumentException("offset and size should be positive (" + offset + ", " + size + ")");
	return list.subList(Math.min(offset, list.size() - 1), Math.min(offset + size, list.size()));
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
     * Doubling the input array length and copy the data to the new array.
     *
     * @param array
     *            the original array.
     * @return new array with twice the size of the original array, with the
     *         data copied from the original array.
     * @throws NullPointerException
     *             if the array is null.
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
     * @throws NullPointerException
     *             if the array is null.
     */
    private static String[] trim(final String[] array, final int newLength) {
	final String[] newArray = new String[newLength];
	System.arraycopy(array, 0, newArray, 0, newLength);
	return newArray;
    }

}
