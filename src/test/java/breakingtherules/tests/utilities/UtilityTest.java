package breakingtherules.tests.utilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import breakingtherules.utilities.Utility;

public class UtilityTest {

    private static final Random rand = new Random();

    @Test
    public void subListTest() {
	List<Integer> list = getRandomList(10);
	int offset = 5;
	int size = 3;
	List<Integer> expected = list.subList(offset, offset + size);
	List<Integer> actual = Utility.subList(list, offset, size);
	assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void subListNullTest() {
	Utility.subList(null, 0, 0);
	fail("Allowed sub list of null list");
    }

    @Test(expected = IllegalArgumentException.class)
    public void subListNegativeOffsetTest() {
	Utility.subList(getEmptyList(), -1, 0);
	fail("Allowed sub list with negative offset");
    }

    @Test(expected = IllegalArgumentException.class)
    public void subListNegativeSizeTest() {
	Utility.subList(getEmptyList(), 0, -1);
	fail("Allowed sub list with negative size");
    }

    @Test
    public void subListOffsetGreaterThanListSizeTest() {
	List<Integer> list = Utility.subList(getRandomList(10), 15, 15);
	assertEquals(getEmptyList(), list);
    }

    @Test
    public void ensureUniquenessTest() {
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
	Utility.ensureUniqueness(null);
    }

    @Test
    public void ensureUniquenessCustomTest() {
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
	Utility.ensureUniqueness(getEmptyList(), null);
    }

    @Test
    public void log2Test() {
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
