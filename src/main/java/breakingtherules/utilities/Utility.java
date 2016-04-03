package breakingtherules.utilities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * The Utility class provide a set of static helper methods. All method are
 * static
 */
public class Utility {

    private static final char SPACE = ' ';

    private static final char TAB = '\t';

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
	if (text == null) {
	    throw new IllegalArgumentException("line can't be null");
	}
	List<String> words = new ArrayList<String>();
	int spaceIndex = nextSpaceOrTab(text);

	// First word
	if (spaceIndex >= 1) {
	    words.add(text.substring(0, spaceIndex));
	}

	// Middle words
	while (spaceIndex >= 0) {
	    int nextSpaceIndex = nextSpaceOrTab(text.substring(spaceIndex + 1));

	    if (nextSpaceIndex > 0) {
		String word = text.substring(spaceIndex + 1, spaceIndex + nextSpaceIndex + 1);
		words.add(word);
	    }

	    text = text.substring(spaceIndex + 1);
	    spaceIndex = nextSpaceOrTab(text);
	}

	// End word
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
	if (text == null || word == null) {
	    throw new IllegalArgumentException("Arguments can't be null");
	}
	if (text.isEmpty()) {
	    return word;
	}
	char lastChar = text.charAt(text.length() - 1);
	char spacer = tab ? TAB : SPACE;
	return text + ((lastChar != SPACE && lastChar != TAB) ? spacer : "") + word;
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

    public static <T> Iterator<Pair<T, T>> getDoubleIterator(final List<T> list) {
	return new Iterator<Pair<T, T>>() {

	    Iterator<T> iteratorToNextElement;

	    Iterator<T> iteratorToCurrentElement;

	    {
		iteratorToCurrentElement = list.iterator();
		iteratorToNextElement = list.iterator();
		if (iteratorToNextElement.hasNext()) {
		    iteratorToNextElement.next();
		}
	    }

	    @Override
	    public boolean hasNext() {
		return iteratorToNextElement.hasNext();
	    }

	    @Override
	    public Pair<T, T> next() {
		T first = iteratorToCurrentElement.next();
		T second = iteratorToNextElement.next();
		return new Pair<T, T>(first, second);
	    }
	};
    }

    /**
     * Get the index of the next space or tab
     * 
     * @param text
     *            the text
     * @return index of the first space or tab in the text, -1 if non found
     */
    private static int nextSpaceOrTab(String text) {
	int space = text.indexOf(SPACE);
	int tab = text.indexOf(TAB);

	int min = Math.min(space, tab);
	return min != -1 ? min : Math.max(space, tab);
    }

}
