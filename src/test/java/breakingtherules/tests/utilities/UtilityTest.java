package breakingtherules.tests.utilities;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import breakingtherules.tests.TestBase;
import breakingtherules.utilities.Utility;

@SuppressWarnings("javadoc")
public class UtilityTest extends TestBase {

    @Test
    public void addWordTest() {
	final String text = "hello big";
	final String word = "world";
	final String expected = "hello big world";

	final String actual = Utility.addWord(text, word);
	assertEquals(expected, actual);
    }

    @Test(expected = NullPointerException.class)
    public void addWordTestNullTest() {
	final String text = null;
	final String word = "world";
	Utility.addWord(text, word);
    }

    @Test
    public void addWordTestNullWord() {
	final String text = "hello big";
	final String word = null;
	final String expected = text + " null";
	final String actual = Utility.addWord(text, word);
	assertEquals(expected, actual);
    }

    @Test
    public void addWordTestSpaceInEnd() {
	final String text = "hello big ";
	final String word = "world";
	final String expected = "hello big world";

	final String actual = Utility.addWord(text, word);
	assertEquals(expected, actual);
    }

    @Test
    public void addWordTestTab() {
	final String text = "hello big";
	final String word = "world";
	final String expected = "hello big\tworld";

	final String actual = Utility.addWord(text, word, true);
	assertEquals(expected, actual);
    }

    @Test
    public void addWordTestTabInEnd() {
	final String text = "hello big\t";
	final String word = "world";
	final String expected = "hello big\tworld";

	final String actual = Utility.addWord(text, word, true);
	assertEquals(expected, actual);
    }

    @Test
    public void breakToWordsTest() {
	final String text = "Hello \t\tbig\t world";
	final String[] expected = new String[] { "Hello", "big", "world" };
	assertEquals(expected, Utility.breakToWords(text));
    }

    @Test
    public void breakToWordsTestCustomSeparators() {
	final String text = "qHellozqqbigqzworldz[]]";
	final String[] expected = new String[] { "Hello", "big", "world", "]" };
	assertEquals(expected, Utility.breakToWords(text, "z", "q", "[]"));
    }

    @Test(expected = NullPointerException.class)
    public void breakToWordsTestNull() {
	Utility.breakToWords(null);
    }

    @Test
    public void breakToWordsTestOnlySpaces() {
	final String text = "Hello  big world,  sup?";
	final String[] expected = new String[] { "Hello", "big", "world,", "sup?" };
	assertEquals(expected, Utility.breakToWords(text));
    }

    @Test
    public void breakToWordsTestOnlyTabs() {
	final String text = "Hello\t\tbig\tworld,\t\tsup?";
	final String[] expected = new String[] { "Hello", "big", "world,", "sup?" };
	assertEquals(expected, Utility.breakToWords(text));
    }

    @Test
    public void breakToWordsTestSpaceInEnd() {
	final String text = "Hello \t\tbig\t world ";
	final String[] expected = new String[] { "Hello", "big", "world" };
	assertEquals(expected, Utility.breakToWords(text));
    }

    @Test
    public void breakToWordsTestSpaceInStart() {
	final String text = "  Hello \t\tbig\t world";
	final String[] expected = new String[] { "Hello", "big", "world" };
	assertEquals(expected, Utility.breakToWords(text));
    }

    @Test
    public void equalsTest() {
	final Object o1 = new Integer(5487);
	final Object o2 = new Integer(5487);
	assertTrue(Utility.deepEquals(o1, o2));
	assertTrue(Utility.deepEquals(o2, o1));
    }

    @Test
    public void equalsTestBasicArray() {
	final Object o1 = new int[] { 5, 7, 64, 7 };
	final Object o2 = new int[] { 5, 7, 64, 7 };
	final Object o4 = new int[] { 5, 7, 64, 7, 5 };
	final Object o3 = new int[] { 5, 7, 64, 8 };

	assertTrue(Utility.deepEquals(o1, o2));
	assertTrue(Utility.deepEquals(o2, o1));
	assertFalse(Utility.deepEquals(o1, o3));
	assertFalse(Utility.deepEquals(o3, o1));
	assertFalse(Utility.deepEquals(o1, o4));
	assertFalse(Utility.deepEquals(o4, o1));
    }

    @Test
    public void equalsTestComplexArray() {
	final Object o1 = new int[][] { new int[] { 5, 4 }, new int[] { 7, 8, 9 } };
	final Object o2 = new int[][] { new int[] { 5, 4 }, new int[] { 7, 8, 9 } };
	final Object o4 = new int[][] { new int[] { 5, 4 }, new int[] { 7, 7, 9 } };
	final Object o3 = new int[][] { new int[] { 5, 4 }, new int[] { 7, 8 } };

	assertTrue(Utility.deepEquals(o1, o2));
	assertTrue(Utility.deepEquals(o2, o1));
	assertFalse(Utility.deepEquals(o1, o3));
	assertFalse(Utility.deepEquals(o3, o1));
	assertFalse(Utility.deepEquals(o1, o4));
	assertFalse(Utility.deepEquals(o4, o1));
    }

    @Test
    public void equalsTestItself() {
	final Object o = new Object();
	assertTrue(Utility.deepEquals(o, o));
    }

    @Test
    public void equalsTestOneNull() {
	final Object o = new Object();
	assertFalse(Utility.deepEquals(null, o));
	assertFalse(Utility.deepEquals(o, null));
    }

    @Test
    public void equalsTestTwoNull() {
	assertTrue(Utility.deepEquals(null, null));
    }

    @Test
    public void indexOfTestMultipleSequences() {
	final String text = "12342544897884564488";
	final String sequence1 = "89";
	final String sequence2 = "44";
	final int expected = 6;
	final int actual = Utility.indexOf(text, sequence1, sequence2);
	assertEquals(expected, actual);
    }

    @Test
    public void indexOfTestNoMatch() {
	final String text = "12342544897884564488";
	final String sequence1 = "555";
	final String sequence2 = "33";
	final int expected = -1;
	final int actual = Utility.indexOf(text, sequence1, sequence2);
	assertEquals(expected, actual);
    }

    @Test(expected = NullPointerException.class)
    public void indexOfTestNullSequence() {
	final String text = "12342544897884564488";
	final String sequence1 = "89";
	final String sequence2 = null;
	Utility.indexOf(text, sequence1, sequence2);
    }

    @Test(expected = NullPointerException.class)
    public void indexOfTestNullText() {
	final String text = null;
	final String sequence1 = "89";
	final String sequence2 = "44";
	Utility.indexOf(text, sequence1, sequence2);
    }

    @Test
    public void indexOfTestOneSequence() {
	final String text = "aabbccadyjsadllad";
	final String sequence = "ad";
	final int expected = 6;
	final int actual = Utility.indexOf(text, sequence);
	assertEquals(expected, actual);
    }

    @Test(expected = NullPointerException.class)
    public void lastIndexIfTestNullText() {
	final String text = null;
	final String sequence1 = "89";
	final String sequence2 = "44";
	Utility.lastIndexOf(text, sequence1, sequence2);
    }

    @Test
    public void lastIndexOfTestMultipleSequences() {
	final String text = "12342544897884564488";
	final String sequence1 = "89";
	final String sequence2 = "44";
	final int expected = 16;
	final int actual = Utility.lastIndexOf(text, sequence1, sequence2);
	assertEquals(expected, actual);
    }

    @Test
    public void lastIndexOfTestNoMatch() {
	final String text = "12342544897884564488";
	final String sequence1 = "555";
	final String sequence2 = "33";
	final int expected = -1;
	final int actual = Utility.lastIndexOf(text, sequence1, sequence2);
	assertEquals(expected, actual);
    }

    @Test(expected = NullPointerException.class)
    public void lastIndexOfTestNullSequence() {
	final String text = "12342544897884564488";
	final String sequence1 = "89";
	final String sequence2 = null;
	Utility.lastIndexOf(text, sequence1, sequence2);
    }

    @Test
    public void lastIndexOfTestOneSequence() {
	final String text = "aadsabbccadadsyjsadllad";
	final String sequence = "ads";
	final int expected = 11;
	final int actual = Utility.lastIndexOf(text, sequence);
	assertEquals(expected, actual);
    }

    @Test
    public void lastPositionOfTestMultipleSequences() {
	final String text = "12342544897884564488";
	final String sequence1 = "897";
	final String sequence2 = "44";
	final int[] expected = new int[] { 16, 2 };
	final int[] actual = Utility.lastPositionOf(text, sequence1, sequence2);
	assertEquals(expected, actual);
    }

    @Test
    public void lastPositionOfTestNoMatch() {
	final String text = "12342544897884564488";
	final String sequence1 = "555";
	final String sequence2 = "33";
	final int[] expected = new int[] { -1, -1 };
	final int[] actual = Utility.lastPositionOf(text, sequence1, sequence2);
	assertEquals(expected, actual);
    }

    @Test(expected = NullPointerException.class)
    public void lastPositionOfTestNullSequence() {
	final String text = "12342544897884564488";
	final String sequence1 = "89";
	final String sequence2 = null;
	Utility.lastPositionOf(text, sequence1, sequence2);
    }

    @Test(expected = NullPointerException.class)
    public void lastPositionOfTestNullText() {
	final String text = null;
	final String sequence1 = "89";
	final String sequence2 = "44";
	Utility.lastPositionOf(text, sequence1, sequence2);
    }

    @Test
    public void lastPositionOfTestOneSequence() {
	final String text = "aadsabbccadadsyjsadllad";
	final String sequence = "ads";
	final int[] expected = new int[] { 11, 3 };
	final int[] actual = Utility.lastPositionOf(text, sequence);
	assertEquals(expected, actual);
    }

    @Test
    public void log2Test() {
	final double num = rand.nextDouble() * 100 + 2; // Random in range [2,
							// 102]
	final double allowedDelta = 0.00001; // Delta is allowed because double
	// comparison isn't accurate
	assertEquals(Math.log(num) / Math.log(2), Utility.log2(num), allowedDelta);
    }

    @Test
    public void positionOfTestMultipleSequences() {
	final String text = "12342544897884564488";
	final String sequence1 = "897";
	final String sequence2 = "44";
	final int[] expected = new int[] { 6, 2 };
	final int[] actual = Utility.positionOf(text, sequence1, sequence2);
	assertEquals(expected, actual);
    }

    @Test
    public void positionOfTestNoMatch() {
	final String text = "12342544897884564488";
	final String sequence1 = "555";
	final String sequence2 = "33";
	final int[] expected = new int[] { -1, -1 };
	final int[] actual = Utility.positionOf(text, sequence1, sequence2);
	assertEquals(expected, actual);
    }

    @Test(expected = NullPointerException.class)
    public void positionOfTestNullSequence() {
	final String text = "12342544897884564488";
	final String sequence1 = "89";
	final String sequence2 = null;
	Utility.positionOf(text, sequence1, sequence2);
    }

    @Test(expected = NullPointerException.class)
    public void positionOfTestNullText() {
	final String text = null;
	final String sequence1 = "89";
	final String sequence2 = "44";
	Utility.positionOf(text, sequence1, sequence2);
    }

    @Test
    public void positionOfTestOneSequence() {
	final String text = "aabbccadyjsadllad";
	final String sequence = "ad";
	final int[] expected = new int[] { 6, 2 };
	final int[] actual = Utility.positionOf(text, sequence);
	assertEquals(expected, actual);
    }

    @Test
    public void putTest() {
	final List<Integer> list = getEmptyList();
	final int index = 5;
	final Integer value = Integer.valueOf(5);
	Utility.put(list, index, value);
	assertEquals(index + 1, list.size());
	assertEquals(value, list.get(index));
	for (int i = 0; i < index; i++)
	    assertNull(list.get(i));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void putTestNegativeIndex() {
	final List<Integer> list = getEmptyList();
	Utility.put(list, -1, Integer.valueOf(0));
    }

    @Test(expected = NullPointerException.class)
    public void putTestNull() {
	Utility.put(null, 0, Integer.valueOf(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void subListNegativeOffsetTest() {
	Utility.subList(getEmptyList(), -1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void subListNegativeSizeTest() {
	Utility.subList(getEmptyList(), 0, -1);
    }

    @Test(expected = NullPointerException.class)
    public void subListNullTest() {
	Utility.subList(null, 0, 0);
    }

    @Test
    public void subListOffsetGreaterThanListSizeTest() {
	final List<Integer> list = Utility.subList(getRandomList(10), 15, 15);
	assertEquals(getEmptyList(), list);
    }

    @Test
    public void subListTest() {
	final List<Integer> list = getRandomList(10);
	final int offset = 5;
	final int size = 3;
	final List<Integer> expected = list.subList(offset, offset + size);
	final List<Integer> actual = Utility.subList(list, offset, size);
	assertEquals(expected, actual);
    }

    @Test
    public void toStringTest() {
	final Object obj = new Integer(rand.nextInt());
	final String expected = obj.toString();
	final String actual = Utility.toString(obj);
	assertEquals(expected, actual);
    }

    @Test
    public void toStringTestArray() {
	final int[] array = new int[] { 1, 5, 88, -42 };
	final String expected = Arrays.toString(array);
	final String actual = Utility.toString(array);
	assertEquals(expected, actual);
    }

    @Test
    public void toStringTestNull() {
	final Object obj = null;
	final String expected = "null";
	final String actual = Utility.toString(obj);
	assertEquals(expected, actual);
    }

    @Test
    public void toStringTestObjectsArray() {
	final Object[] array = new int[][] { new int[] { 5, -4 }, new int[] { 0, 41 } };
	final String expected = Arrays.deepToString(array);
	final String actual = Utility.toString(array);
	assertEquals(expected, actual);
    }

    private static ArrayList<Integer> getEmptyList() {
	return new ArrayList<>();
    }

    private static List<Integer> getRandomList(final int size) {
	if (size < 0)
	    throw new RuntimeException("Unexpected test internal exception! size = " + size);
	final List<Integer> list = getEmptyList();
	while (list.size() < size)
	    list.add(Integer.valueOf(rand.nextInt()));
	return list;
    }

}
