package breakingtherules.tests.utilities;

import static breakingtherules.tests.JUnitUtilities.advanceAssertEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import breakingtherules.utilities.Pair;
import breakingtherules.utilities.Utility;

public class UtilityTest {

    private static final Random rand = new Random();

    @Test
    public void putTest() {
	System.out.println("# UtilityTest putTest");
	List<Integer> list = getEmptyList();
	int index = 5;
	Integer value = 5;
	Utility.put(list, index, value);
	assertEquals(index + 1, list.size());
	assertEquals(value, list.get(index));
	for (int i = 0; i < index; i++) {
	    assertNull(list.get(i));
	}
    }

    @Test(expected = IllegalArgumentException.class)
    public void putTestNull() {
	System.out.println("# UtilityTest putTestNull");
	Utility.put(null, 0, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void putTestNegativeIndex() {
	System.out.println("# UtilityTest putTestNegativeIndex");
	List<Integer> list = getEmptyList();
	Utility.put(list, -1, 0);
    }

    @Test
    public void cloneListTest() {
	System.out.println("# UtilityTest cloneListTest");
	List<Integer> list = new ArrayList<>();
	for (int i = 0; i < 100; i++) {
	    list.add(i);
	}
	List<Integer> listClone = Utility.cloneList(list);
	assertFalse(list == listClone);
	assertEquals(list, listClone);
    }

    @Test
    public void cloneListTestNull() {
	System.out.println("# UtilityTest cloneListTestNull");
	List<?> list = Utility.cloneList(null);
	assertNull(list);
    }

    @Test
    public void subListTest() {
	System.out.println("# UtilityTest subListTest");
	List<Integer> list = getRandomList(10);
	int offset = 5;
	int size = 3;
	List<Integer> expected = list.subList(offset, offset + size);
	List<Integer> actual = Utility.subList(list, offset, size);
	assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void subListNullTest() {
	System.out.println("# UtilityTest subListNullTest");
	Utility.subList(null, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void subListNegativeOffsetTest() {
	System.out.println("# UtilityTest subListNegativeOffsetTest");
	Utility.subList(getEmptyList(), -1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void subListNegativeSizeTest() {
	System.out.println("# UtilityTest subListNegativeSizeTest");
	Utility.subList(getEmptyList(), 0, -1);
    }

    @Test
    public void subListOffsetGreaterThanListSizeTest() {
	System.out.println("# UtilityTest subListOffsetGreaterThanListSizeTest");
	List<Integer> list = Utility.subList(getRandomList(10), 15, 15);
	assertEquals(getEmptyList(), list);
    }

    @Test
    public void ensureUniquenessTest() {
	System.out.println("# UtilityTest ensureUniquenessTest");
	ArrayList<Integer> list = getEmptyList();
	list.add(0);
	list.add(1);
	list.add(1);
	list.add(2);
	list.add(3);
	list.add(4);
	list.add(4);

	@SuppressWarnings("unchecked")
	List<Integer> expected = (ArrayList<Integer>) list.clone();
	expected.remove(2);
	expected.remove(5);
	List<Integer> actual = Utility.ensureUniqueness(list);

	actual.sort(null);
	expected.sort(null);
	assertEquals(expected, actual);
    }

    @Test
    public void ensureUniquenessNullElementsTest() {
	System.out.println("# UtilityTest ensureUniquenessNullElementsTest");
	ArrayList<Integer> list = getEmptyList();
	list.add(0);
	list.add(1);
	list.add(1);
	list.add(null);
	list.add(3);
	list.add(4);
	list.add(null);

	@SuppressWarnings("unchecked")
	List<Integer> expected = (ArrayList<Integer>) list.clone();
	expected.remove(2);
	expected.remove(5);
	List<Integer> actual = Utility.ensureUniqueness(list);

	// Special comparator is needed because there are some null elements
	Comparator<Integer> c = new Comparator<Integer>() {

	    @Override
	    public int compare(Integer o1, Integer o2) {
		if (o1 == null) {
		    return o2 == null ? 0 : 1;
		}
		if (o2 == null) {
		    return o1 == null ? 0 : -1;
		}
		return Integer.compare(o1, o2);
	    }
	};
	actual.sort(c);
	expected.sort(c);
	assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureUniquenessNullListTest() {
	System.out.println("# UtilityTest ensureUniquenessNullListTest");
	Utility.ensureUniqueness(null);
    }

    @Test
    public void ensureUniquenessCustomTest() {
	System.out.println("# UtilityTest ensureUniquenessCustomTest");
	ArrayList<Integer> list = getEmptyList();
	list.add(0);
	list.add(11);
	list.add(10);
	list.add(2);
	list.add(8);
	list.add(4);
	list.add(21);
	list.add(51);
	list.add(50);

	@SuppressWarnings("unchecked")
	List<Integer> expected = (ArrayList<Integer>) list.clone();
	expected.remove(2);
	expected.remove(7);
	List<Integer> actual = Utility.ensureUniqueness(list, new Comparator<Integer>() {

	    @Override
	    public int compare(Integer o1, Integer o2) {
		return o1 == o2 - 1 || o2 == o1 - 1 ? 0 : 1;
	    }
	});

	actual.sort(null);
	expected.sort(null);
	assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureUniquenessNullComparatorTest() {
	System.out.println("# UtilityTest ensureUniquenessNullComparatorTest");
	Utility.ensureUniqueness(getEmptyList(), null);
    }

    @Test
    public void getDoubleIteratorOneListTest() {
	System.out.println("# UtilityTest getDoubleIteratorOneListTest");
	final int size = 10;
	List<Integer> list = getRandomList(size);
	Iterator<Pair<Integer, Integer>> it = Utility.getDoubleIterator(list);
	assertNotNull(it);
	for (int i = 0; i < size - 1; i++) {
	    assertTrue(it.hasNext());
	    Pair<Integer, Integer> pair = it.next();
	    assertNotNull(pair);
	}
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDoubleIteratorOneListTestNull() {
	System.out.println("# UtilityTest getDoubleIteratorOneListTestNull");
	Utility.getDoubleIterator(null);
    }

    @Test
    public void getDoubleIteratorTwoListsTest() {
	System.out.println("# UtilityTest getDoubleIteratorTwoListsTest");
	List<Integer> listA = getRandomList(5);
	List<Integer> listB = getRandomList(5);
	Iterator<Pair<Integer, Integer>> it = Utility.getDoubleIterator(listA, listB);

	assertNotNull(it);

	List<Integer> actualListA = getEmptyList();
	List<Integer> actualListB = getEmptyList();
	while (it.hasNext()) {
	    Pair<Integer, Integer> pair = it.next();
	    actualListA.add(pair.first);
	    actualListB.add(pair.second);
	}

	assertEquals(listA, actualListA);
	assertEquals(listB, actualListB);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDoubleIteratorTwoListsTestListANull() {
	System.out.println("# UtilityTest getDoubleIteratorTwoListsTestListANull");
	List<Integer> listA = null;
	List<Integer> listB = getRandomList(5);
	Utility.getDoubleIterator(listA, listB);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDoubleIteratorTwoListsTestListBNull() {
	System.out.println("# UtilityTest getDoubleIteratorTwoListsTestListBNull");
	List<Integer> listA = getRandomList(5);
	List<Integer> listB = null;
	Utility.getDoubleIterator(listA, listB);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDoubleIteratorTwoListsTestDifferentSize() {
	System.out.println("# UtilityTest getDoubleIteratorTwoListsTestDifferentSize");
	List<Integer> listA = getRandomList(5);
	List<Integer> listB = getRandomList(6);
	Utility.getDoubleIterator(listA, listB);
    }

    @Test
    public void breakToWordsTest() {
	System.out.println("# UtilityTest breakToWordsTest");
	String text = "Hello \t\tbig\t world";
	List<String> expected = new ArrayList<String>();
	expected.add("Hello");
	expected.add("big");
	expected.add("world");
	assertEquals(expected, Utility.breakToWords(text));
    }

    @Test
    public void breakToWordsTestOnlyTabs() {
	System.out.println("# UtilityTest breakToWordsTestOnlyTabs");
	String text = "Hello\t\tbig\tworld,\t\tsup?";
	List<String> expected = new ArrayList<String>();
	expected.add("Hello");
	expected.add("big");
	expected.add("world,");
	expected.add("sup?");
	assertEquals(expected, Utility.breakToWords(text));

    }

    @Test
    public void breakToWordsTestOnlySpaces() {
	System.out.println("# UtilityTest breakToWordsTestOnlySpaces");
	String text = "Hello  big world,  sup?";
	List<String> expected = new ArrayList<String>();
	expected.add("Hello");
	expected.add("big");
	expected.add("world,");
	expected.add("sup?");
	assertEquals(expected, Utility.breakToWords(text));
    }

    @Test
    public void breakToWordsTestSpaceInStart() {
	System.out.println("# UtilityTest breakToWordsTestSpaceInStart");
	String text = "  Hello \t\tbig\t world";
	List<String> expected = new ArrayList<String>();
	expected.add("Hello");
	expected.add("big");
	expected.add("world");
	assertEquals(expected, Utility.breakToWords(text));
    }

    @Test
    public void breakToWordsTestSpaceInEnd() {
	System.out.println("# UtilityTest breakToWordsTestSpaceInEnd");
	String text = "Hello \t\tbig\t world ";
	List<String> expected = new ArrayList<String>();
	expected.add("Hello");
	expected.add("big");
	expected.add("world");
	assertEquals(expected, Utility.breakToWords(text));
    }

    @Test(expected = IllegalArgumentException.class)
    public void breakToWordsTestNull() {
	System.out.println("# UtilityTest breakToWordsTestNull");
	Utility.breakToWords(null);
    }

    @Test
    public void breakToWordsTestCustomSeparators() {
	System.out.println("# UtilityTest breakToWordsTestCustomSeparators");
	String text = "qHellozqqbigqzworldz[]]";
	List<String> expected = new ArrayList<String>();
	expected.add("Hello");
	expected.add("big");
	expected.add("world");
	expected.add("]");
	assertEquals(expected, Utility.breakToWords(text, "z", "q", "[]"));
    }

    @Test
    public void addWordTest() {
	System.out.println("# UtilityTest addWordTest");
	String text = "hello big";
	String word = "world";
	String expected = "hello big world";

	String actual = Utility.addWord(text, word);
	assertEquals(expected, actual);
    }

    @Test
    public void addWordTestSpaceInEnd() {
	System.out.println("# UtilityTest addWordTestSpaceInEnd");
	String text = "hello big ";
	String word = "world";
	String expected = "hello big world";

	String actual = Utility.addWord(text, word);
	assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addWordTestNullTest() {
	System.out.println("# UtilityTest addWordTestNullTest");
	String text = null;
	String word = "world";
	Utility.addWord(text, word);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addWordTestNullWord() {
	System.out.println("# UtilityTest addWordTestNullWord");
	String text = "hello big";
	String word = null;
	Utility.addWord(text, word);
    }

    @Test
    public void addWordTestTab() {
	System.out.println("# UtilityTest addWordTestTab");
	String text = "hello big";
	String word = "world";
	String expected = "hello big\tworld";

	String actual = Utility.addWord(text, word, true);
	assertEquals(expected, actual);
    }

    @Test
    public void addWordTestTabInEnd() {
	System.out.println("# UtilityTest addWordTestTabInEnd");
	String text = "hello big\t";
	String word = "world";
	String expected = "hello big\tworld";

	String actual = Utility.addWord(text, word, true);
	assertEquals(expected, actual);
    }

    @Test
    public void toStringTest() {
	System.out.println("# UtilityTest toStringTest");
	Object obj = new Integer(rand.nextInt());
	String expected = obj.toString();
	String actual = Utility.toString(obj);
	assertEquals(expected, actual);
    }

    @Test
    public void toStringTestNull() {
	System.out.println("# UtilityTest toStringTestNull");
	Object obj = null;
	String expected = "null";
	String actual = Utility.toString(obj);
	assertEquals(expected, actual);
    }

    @Test
    public void toStringTestArray() {
	System.out.println("# UtilityTest toStringTestArray");
	int[] array = new int[] { 1, 5, 88, -42 };
	String expected = Arrays.toString(array);
	String actual = Utility.toString(array);
	assertEquals(expected, actual);
    }

    @Test
    public void toStringTestObjectsArray() {
	System.out.println("# UtilityTest toStringTestObjectsArray");
	Object[] array = new int[][] { new int[] { 5, -4 }, new int[] { 0, 41 } };
	String expected = Arrays.deepToString(array);
	String actual = Utility.toString(array);
	assertEquals(expected, actual);
    }

    @Test
    public void equalsTest() {
	System.out.println("# UtilityTest equalsTest");
	Object o1 = new Integer(5487);
	Object o2 = new Integer(5487);
	assertTrue(Utility.equals(o1, o2));
	assertTrue(Utility.equals(o2, o1));
    }

    @Test
    public void equalsTestBasicArray() {
	System.out.println("# UtilityTest equalsTestBasicArray");
	Object o1 = new int[] { 5, 7, 64, 7 };
	Object o2 = new int[] { 5, 7, 64, 7 };
	Object o4 = new int[] { 5, 7, 64, 7, 5 };
	Object o3 = new int[] { 5, 7, 64, 8 };

	assertTrue(Utility.equals(o1, o2));
	assertTrue(Utility.equals(o2, o1));
	assertFalse(Utility.equals(o1, o3));
	assertFalse(Utility.equals(o3, o1));
	assertFalse(Utility.equals(o1, o4));
	assertFalse(Utility.equals(o4, o1));
    }

    @Test
    public void equalsTestComplexArray() {
	System.out.println("# UtilityTest equalsTestComplexArray");
	Object o1 = new int[][] { new int[] { 5, 4 }, new int[] { 7, 8, 9 } };
	Object o2 = new int[][] { new int[] { 5, 4 }, new int[] { 7, 8, 9 } };
	Object o4 = new int[][] { new int[] { 5, 4 }, new int[] { 7, 7, 9 } };
	Object o3 = new int[][] { new int[] { 5, 4 }, new int[] { 7, 8 } };

	assertTrue(Utility.equals(o1, o2));
	assertTrue(Utility.equals(o2, o1));
	assertFalse(Utility.equals(o1, o3));
	assertFalse(Utility.equals(o3, o1));
	assertFalse(Utility.equals(o1, o4));
	assertFalse(Utility.equals(o4, o1));
    }

    @Test
    public void equalsTestItself() {
	System.out.println("# UtilityTest equalsTestItself");
	Object o = new Object();
	assertTrue(Utility.equals(o, o));
    }

    @Test
    public void equalsTestOneNull() {
	System.out.println("# UtilityTest equalsTestOneNull");
	Object o = new Object();
	assertFalse(Utility.equals(null, o));
	assertFalse(Utility.equals(o, null));
    }

    @Test
    public void equalsTestTwoNull() {
	System.out.println("# UtilityTest equalsTestTwoNull");
	assertTrue(Utility.equals(null, null));
    }

    @Test
    public void indexOfTestOneSequence() {
	System.out.println("# UtilityTest indexOfTestOneSequence");
	String text = "aabbccadyjsadllad";
	String sequence = "ad";
	int expected = 6;
	int actual = Utility.indexOf(text, sequence);
	assertEquals(expected, actual);
    }

    @Test
    public void indexOfTestMultipleSequences() {
	System.out.println("# UtilityTest indexOfTestMultipleSequences");
	String text = "12342544897884564488";
	String sequence1 = "89";
	String sequence2 = "44";
	int expected = 6;
	int actual = Utility.indexOf(text, sequence1, sequence2);
	assertEquals(expected, actual);
    }

    @Test
    public void indexOfTestNoMatch() {
	System.out.println("# UtilityTest indexOfTestNoMatch");
	String text = "12342544897884564488";
	String sequence1 = "555";
	String sequence2 = "33";
	int expected = -1;
	int actual = Utility.indexOf(text, sequence1, sequence2);
	assertEquals(expected, actual);
    }

    @Test(expected = NullPointerException.class)
    public void indexOfTestNullText() {
	System.out.println("# UtilityTest indexOfTestNullText");
	String text = null;
	String sequence1 = "89";
	String sequence2 = "44";
	Utility.indexOf(text, sequence1, sequence2);
    }

    @Test(expected = NullPointerException.class)
    public void indexOfTestNullSequence() {
	System.out.println("# UtilityTest indexOfTestNullSequence");
	String text = "12342544897884564488";
	String sequence1 = "89";
	String sequence2 = null;
	Utility.indexOf(text, sequence1, sequence2);
    }

    @Test
    public void lastIndexOfTestOneSequence() {
	System.out.println("# UtilityTest lastIndexOfTestOneSequence");
	String text = "aadsabbccadadsyjsadllad";
	String sequence = "ads";
	int expected = 11;
	int actual = Utility.lastIndexOf(text, sequence);
	assertEquals(expected, actual);
    }

    @Test
    public void lastIndexOfTestMultipleSequences() {
	System.out.println("# UtilityTest lastIndexOfTestMultipleSequences");
	String text = "12342544897884564488";
	String sequence1 = "89";
	String sequence2 = "44";
	int expected = 16;
	int actual = Utility.lastIndexOf(text, sequence1, sequence2);
	assertEquals(expected, actual);
    }

    @Test
    public void lastIndexOfTestNoMatch() {
	System.out.println("# UtilityTest lastIndexOfTestNoMatch");
	String text = "12342544897884564488";
	String sequence1 = "555";
	String sequence2 = "33";
	int expected = -1;
	int actual = Utility.lastIndexOf(text, sequence1, sequence2);
	assertEquals(expected, actual);
    }

    @Test(expected = NullPointerException.class)
    public void lastIndexIfTestNullText() {
	System.out.println("# UtilityTest lastIndexIfTestNullText");
	String text = null;
	String sequence1 = "89";
	String sequence2 = "44";
	Utility.lastIndexOf(text, sequence1, sequence2);
    }

    @Test(expected = NullPointerException.class)
    public void lastIndexOfTestNullSequence() {
	System.out.println("# UtilityTest lastIndexOfTestNullSequence");
	String text = "12342544897884564488";
	String sequence1 = "89";
	String sequence2 = null;
	Utility.lastIndexOf(text, sequence1, sequence2);
    }

    @Test
    public void positionOfTestOneSequence() {
	System.out.println("# UtilityTest positionOfTestOneSequence");
	String text = "aabbccadyjsadllad";
	String sequence = "ad";
	int[] expected = new int[] { 6, 2 };
	int[] actual = Utility.positionOf(text, sequence);
	advanceAssertEquals(expected, actual);
    }

    @Test
    public void positionOfTestMultipleSequences() {
	System.out.println("# UtilityTest positionOfTestMultipleSequences");
	String text = "12342544897884564488";
	String sequence1 = "897";
	String sequence2 = "44";
	int[] expected = new int[] { 6, 2 };
	int[] actual = Utility.positionOf(text, sequence1, sequence2);
	advanceAssertEquals(expected, actual);
    }

    @Test
    public void positionOfTestNoMatch() {
	System.out.println("# UtilityTest positionOfTestNoMatch");
	String text = "12342544897884564488";
	String sequence1 = "555";
	String sequence2 = "33";
	int[] expected = new int[] { -1, -1 };
	int[] actual = Utility.positionOf(text, sequence1, sequence2);
	advanceAssertEquals(expected, actual);
    }

    @Test(expected = NullPointerException.class)
    public void positionOfTestNullText() {
	System.out.println("# UtilityTest positionOfTestNullText");
	String text = null;
	String sequence1 = "89";
	String sequence2 = "44";
	Utility.positionOf(text, sequence1, sequence2);
    }

    @Test(expected = NullPointerException.class)
    public void positionOfTestNullSequence() {
	System.out.println("# UtilityTest positionOfTestNullSequence");
	String text = "12342544897884564488";
	String sequence1 = "89";
	String sequence2 = null;
	Utility.positionOf(text, sequence1, sequence2);
    }

    @Test
    public void lastPositionOfTestOneSequence() {
	System.out.println("# UtilityTest lastPositionOfTestOneSequence");
	String text = "aadsabbccadadsyjsadllad";
	String sequence = "ads";
	int[] expected = new int[] { 11, 3 };
	int[] actual = Utility.lastPositionOf(text, sequence);
	advanceAssertEquals(expected, actual);
    }

    @Test
    public void lastPositionOfTestMultipleSequences() {
	System.out.println("# UtilityTest lastPositionOfTestMultipleSequences");
	String text = "12342544897884564488";
	String sequence1 = "897";
	String sequence2 = "44";
	int[] expected = new int[] { 16, 2 };
	int[] actual = Utility.lastPositionOf(text, sequence1, sequence2);
	advanceAssertEquals(expected, actual);
    }

    @Test
    public void lastPositionOfTestNoMatch() {
	System.out.println("# UtilityTest lastPositionOfTestNoMatch");
	String text = "12342544897884564488";
	String sequence1 = "555";
	String sequence2 = "33";
	int[] expected = new int[] { -1, -1 };
	int[] actual = Utility.lastPositionOf(text, sequence1, sequence2);
	advanceAssertEquals(expected, actual);
    }

    @Test(expected = NullPointerException.class)
    public void lastPositionOfTestNullText() {
	System.out.println("# UtilityTest lastPositionOfTestNullText");
	String text = null;
	String sequence1 = "89";
	String sequence2 = "44";
	Utility.lastPositionOf(text, sequence1, sequence2);
    }

    @Test(expected = NullPointerException.class)
    public void lastPositionOfTestNullSequence() {
	System.out.println("# UtilityTest lastPositionOfTestNullSequence");
	String text = "12342544897884564488";
	String sequence1 = "89";
	String sequence2 = null;
	Utility.lastPositionOf(text, sequence1, sequence2);
    }

    @Test
    public void log2Test() {
	System.out.println("# UtilityTest log2Test");
	double num = rand.nextDouble() * 100 + 2; // Random in range [2, 102]
	double allowedDelta = 0.00001; // Delta is allowed because double
				       // comparison isn't accurate
	assertEquals(Math.log(num) / Math.log(2), Utility.log2(num), allowedDelta);
    }

    private static ArrayList<Integer> getEmptyList() {
	return new ArrayList<Integer>();
    }

    private static List<Integer> getRandomList(int size) {
	if (size < 0) {
	    throw new RuntimeException("Unexpected test internal exception! size = " + size);
	}
	List<Integer> list = getEmptyList();
	while (list.size() < size) {
	    list.add(rand.nextInt());
	}
	return list;
    }

}
