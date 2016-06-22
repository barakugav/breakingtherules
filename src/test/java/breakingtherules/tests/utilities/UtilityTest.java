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
    public void putTest() {
	List<Integer> list = getEmptyList();
	int index = 5;
	Integer value = Integer.valueOf(5);
	Utility.put(list, index, value);
	assertEquals(index + 1, list.size());
	assertEquals(value, list.get(index));
	for (int i = 0; i < index; i++) {
	    assertNull(list.get(i));
	}
    }

    @Test(expected = NullPointerException.class)
    public void putTestNull() {
	Utility.put(null, 0, Integer.valueOf(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void putTestNegativeIndex() {
	List<Integer> list = getEmptyList();
	Utility.put(list, -1, Integer.valueOf(0));
    }

    @Test
    public void subListTest() {
	List<Integer> list = getRandomList(10);
	int offset = 5;
	int size = 3;
	List<Integer> expected = list.subList(offset, offset + size);
	List<Integer> actual = Utility.subList(list, offset, size);
	assertEquals(expected, actual);
    }

    @Test(expected = NullPointerException.class)
    public void subListNullTest() {
	Utility.subList(null, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void subListNegativeOffsetTest() {
	Utility.subList(getEmptyList(), -1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void subListNegativeSizeTest() {
	Utility.subList(getEmptyList(), 0, -1);
    }

    @Test
    public void subListOffsetGreaterThanListSizeTest() {
	List<Integer> list = Utility.subList(getRandomList(10), 15, 15);
	assertEquals(getEmptyList(), list);
    }

    @Test
    public void breakToWordsTest() {
	String text = "Hello \t\tbig\t world";
	String[] expected = new String[] { "Hello", "big", "world" };
	assertEquals(expected, Utility.breakToWords(text));
    }

    @Test
    public void breakToWordsTestOnlyTabs() {
	String text = "Hello\t\tbig\tworld,\t\tsup?";
	String[] expected = new String[] { "Hello", "big", "world,", "sup?" };
	assertEquals(expected, Utility.breakToWords(text));
    }

    @Test
    public void breakToWordsTestOnlySpaces() {
	String text = "Hello  big world,  sup?";
	String[] expected = new String[] { "Hello", "big", "world,", "sup?" };
	assertEquals(expected, Utility.breakToWords(text));
    }

    @Test
    public void breakToWordsTestSpaceInStart() {
	String text = "  Hello \t\tbig\t world";
	String[] expected = new String[] { "Hello", "big", "world" };
	assertEquals(expected, Utility.breakToWords(text));
    }

    @Test
    public void breakToWordsTestSpaceInEnd() {
	String text = "Hello \t\tbig\t world ";
	String[] expected = new String[] { "Hello", "big", "world" };
	assertEquals(expected, Utility.breakToWords(text));
    }

    @Test(expected = NullPointerException.class)
    public void breakToWordsTestNull() {
	Utility.breakToWords(null);
    }

    @Test
    public void breakToWordsTestCustomSeparators() {
	String text = "qHellozqqbigqzworldz[]]";
	String[] expected = new String[] { "Hello", "big", "world", "]" };
	assertEquals(expected, Utility.breakToWords(text, "z", "q", "[]"));
    }

    @Test
    public void addWordTest() {
	String text = "hello big";
	String word = "world";
	String expected = "hello big world";

	String actual = Utility.addWord(text, word);
	assertEquals(expected, actual);
    }

    @Test
    public void addWordTestSpaceInEnd() {
	String text = "hello big ";
	String word = "world";
	String expected = "hello big world";

	String actual = Utility.addWord(text, word);
	assertEquals(expected, actual);
    }

    @Test(expected = NullPointerException.class)
    public void addWordTestNullTest() {
	String text = null;
	String word = "world";
	Utility.addWord(text, word);
    }

    @Test
    public void addWordTestNullWord() {
	String text = "hello big";
	String word = null;
	String expected = text + " null";
	String actual = Utility.addWord(text, word);
	assertEquals(expected, actual);
    }

    @Test
    public void addWordTestTab() {
	String text = "hello big";
	String word = "world";
	String expected = "hello big\tworld";

	String actual = Utility.addWord(text, word, true);
	assertEquals(expected, actual);
    }

    @Test
    public void addWordTestTabInEnd() {
	String text = "hello big\t";
	String word = "world";
	String expected = "hello big\tworld";

	String actual = Utility.addWord(text, word, true);
	assertEquals(expected, actual);
    }

    @Test
    public void toStringTest() {
	Object obj = new Integer(rand.nextInt());
	String expected = obj.toString();
	String actual = Utility.toString(obj);
	assertEquals(expected, actual);
    }

    @Test
    public void toStringTestNull() {
	Object obj = null;
	String expected = "null";
	String actual = Utility.toString(obj);
	assertEquals(expected, actual);
    }

    @Test
    public void toStringTestArray() {
	int[] array = new int[] { 1, 5, 88, -42 };
	String expected = Arrays.toString(array);
	String actual = Utility.toString(array);
	assertEquals(expected, actual);
    }

    @Test
    public void toStringTestObjectsArray() {
	Object[] array = new int[][] { new int[] { 5, -4 }, new int[] { 0, 41 } };
	String expected = Arrays.deepToString(array);
	String actual = Utility.toString(array);
	assertEquals(expected, actual);
    }

    @Test
    public void equalsTest() {
	Object o1 = new Integer(5487);
	Object o2 = new Integer(5487);
	assertTrue(Utility.deepEquals(o1, o2));
	assertTrue(Utility.deepEquals(o2, o1));
    }

    @Test
    public void equalsTestBasicArray() {
	Object o1 = new int[] { 5, 7, 64, 7 };
	Object o2 = new int[] { 5, 7, 64, 7 };
	Object o4 = new int[] { 5, 7, 64, 7, 5 };
	Object o3 = new int[] { 5, 7, 64, 8 };

	assertTrue(Utility.deepEquals(o1, o2));
	assertTrue(Utility.deepEquals(o2, o1));
	assertFalse(Utility.deepEquals(o1, o3));
	assertFalse(Utility.deepEquals(o3, o1));
	assertFalse(Utility.deepEquals(o1, o4));
	assertFalse(Utility.deepEquals(o4, o1));
    }

    @Test
    public void equalsTestComplexArray() {
	Object o1 = new int[][] { new int[] { 5, 4 }, new int[] { 7, 8, 9 } };
	Object o2 = new int[][] { new int[] { 5, 4 }, new int[] { 7, 8, 9 } };
	Object o4 = new int[][] { new int[] { 5, 4 }, new int[] { 7, 7, 9 } };
	Object o3 = new int[][] { new int[] { 5, 4 }, new int[] { 7, 8 } };

	assertTrue(Utility.deepEquals(o1, o2));
	assertTrue(Utility.deepEquals(o2, o1));
	assertFalse(Utility.deepEquals(o1, o3));
	assertFalse(Utility.deepEquals(o3, o1));
	assertFalse(Utility.deepEquals(o1, o4));
	assertFalse(Utility.deepEquals(o4, o1));
    }

    @Test
    public void equalsTestItself() {
	Object o = new Object();
	assertTrue(Utility.deepEquals(o, o));
    }

    @Test
    public void equalsTestOneNull() {
	Object o = new Object();
	assertFalse(Utility.deepEquals(null, o));
	assertFalse(Utility.deepEquals(o, null));
    }

    @Test
    public void equalsTestTwoNull() {
	assertTrue(Utility.deepEquals(null, null));
    }

    @Test
    public void indexOfTestOneSequence() {
	String text = "aabbccadyjsadllad";
	String sequence = "ad";
	int expected = 6;
	int actual = Utility.indexOf(text, sequence);
	assertEquals(expected, actual);
    }

    @Test
    public void indexOfTestMultipleSequences() {
	String text = "12342544897884564488";
	String sequence1 = "89";
	String sequence2 = "44";
	int expected = 6;
	int actual = Utility.indexOf(text, sequence1, sequence2);
	assertEquals(expected, actual);
    }

    @Test
    public void indexOfTestNoMatch() {
	String text = "12342544897884564488";
	String sequence1 = "555";
	String sequence2 = "33";
	int expected = -1;
	int actual = Utility.indexOf(text, sequence1, sequence2);
	assertEquals(expected, actual);
    }

    @Test(expected = NullPointerException.class)
    public void indexOfTestNullText() {
	String text = null;
	String sequence1 = "89";
	String sequence2 = "44";
	Utility.indexOf(text, sequence1, sequence2);
    }

    @Test(expected = NullPointerException.class)
    public void indexOfTestNullSequence() {
	String text = "12342544897884564488";
	String sequence1 = "89";
	String sequence2 = null;
	Utility.indexOf(text, sequence1, sequence2);
    }

    @Test
    public void lastIndexOfTestOneSequence() {
	String text = "aadsabbccadadsyjsadllad";
	String sequence = "ads";
	int expected = 11;
	int actual = Utility.lastIndexOf(text, sequence);
	assertEquals(expected, actual);
    }

    @Test
    public void lastIndexOfTestMultipleSequences() {
	String text = "12342544897884564488";
	String sequence1 = "89";
	String sequence2 = "44";
	int expected = 16;
	int actual = Utility.lastIndexOf(text, sequence1, sequence2);
	assertEquals(expected, actual);
    }

    @Test
    public void lastIndexOfTestNoMatch() {
	String text = "12342544897884564488";
	String sequence1 = "555";
	String sequence2 = "33";
	int expected = -1;
	int actual = Utility.lastIndexOf(text, sequence1, sequence2);
	assertEquals(expected, actual);
    }

    @Test(expected = NullPointerException.class)
    public void lastIndexIfTestNullText() {
	String text = null;
	String sequence1 = "89";
	String sequence2 = "44";
	Utility.lastIndexOf(text, sequence1, sequence2);
    }

    @Test(expected = NullPointerException.class)
    public void lastIndexOfTestNullSequence() {
	String text = "12342544897884564488";
	String sequence1 = "89";
	String sequence2 = null;
	Utility.lastIndexOf(text, sequence1, sequence2);
    }

    @Test
    public void positionOfTestOneSequence() {
	String text = "aabbccadyjsadllad";
	String sequence = "ad";
	int[] expected = new int[] { 6, 2 };
	int[] actual = Utility.positionOf(text, sequence);
	assertEquals(expected, actual);
    }

    @Test
    public void positionOfTestMultipleSequences() {
	String text = "12342544897884564488";
	String sequence1 = "897";
	String sequence2 = "44";
	int[] expected = new int[] { 6, 2 };
	int[] actual = Utility.positionOf(text, sequence1, sequence2);
	assertEquals(expected, actual);
    }

    @Test
    public void positionOfTestNoMatch() {
	String text = "12342544897884564488";
	String sequence1 = "555";
	String sequence2 = "33";
	int[] expected = new int[] { -1, -1 };
	int[] actual = Utility.positionOf(text, sequence1, sequence2);
	assertEquals(expected, actual);
    }

    @Test(expected = NullPointerException.class)
    public void positionOfTestNullText() {
	String text = null;
	String sequence1 = "89";
	String sequence2 = "44";
	Utility.positionOf(text, sequence1, sequence2);
    }

    @Test(expected = NullPointerException.class)
    public void positionOfTestNullSequence() {
	String text = "12342544897884564488";
	String sequence1 = "89";
	String sequence2 = null;
	Utility.positionOf(text, sequence1, sequence2);
    }

    @Test
    public void lastPositionOfTestOneSequence() {
	String text = "aadsabbccadadsyjsadllad";
	String sequence = "ads";
	int[] expected = new int[] { 11, 3 };
	int[] actual = Utility.lastPositionOf(text, sequence);
	assertEquals(expected, actual);
    }

    @Test
    public void lastPositionOfTestMultipleSequences() {
	String text = "12342544897884564488";
	String sequence1 = "897";
	String sequence2 = "44";
	int[] expected = new int[] { 16, 2 };
	int[] actual = Utility.lastPositionOf(text, sequence1, sequence2);
	assertEquals(expected, actual);
    }

    @Test
    public void lastPositionOfTestNoMatch() {
	String text = "12342544897884564488";
	String sequence1 = "555";
	String sequence2 = "33";
	int[] expected = new int[] { -1, -1 };
	int[] actual = Utility.lastPositionOf(text, sequence1, sequence2);
	assertEquals(expected, actual);
    }

    @Test(expected = NullPointerException.class)
    public void lastPositionOfTestNullText() {
	String text = null;
	String sequence1 = "89";
	String sequence2 = "44";
	Utility.lastPositionOf(text, sequence1, sequence2);
    }

    @Test(expected = NullPointerException.class)
    public void lastPositionOfTestNullSequence() {
	String text = "12342544897884564488";
	String sequence1 = "89";
	String sequence2 = null;
	Utility.lastPositionOf(text, sequence1, sequence2);
    }

    @Test
    public void log2Test() {
	double num = rand.nextDouble() * 100 + 2; // Random in range [2, 102]
	double allowedDelta = 0.00001; // Delta is allowed because double
				       // comparison isn't accurate
	assertEquals(Math.log(num) / Math.log(2), Utility.log2(num), allowedDelta);
    }

    private static ArrayList<Integer> getEmptyList() {
	return new ArrayList<>();
    }

    private static List<Integer> getRandomList(int size) {
	if (size < 0) {
	    throw new RuntimeException("Unexpected test internal exception! size = " + size);
	}
	List<Integer> list = getEmptyList();
	while (list.size() < size) {
	    list.add(Integer.valueOf(rand.nextInt()));
	}
	return list;
    }

}
