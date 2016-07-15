package breakingtherules.tests.utilities;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Test;

import breakingtherules.tests.TestBase;
import breakingtherules.utilities.ArrayIterator;

@SuppressWarnings("javadoc")
public class ArrayIteratorTest extends TestBase {

    @Test
    public void arrayIteratorTest() {
	final int size = 50;
	final Integer[] array = randomIntegerArray(size);
	final Iterator<Integer> it = new ArrayIterator<>(array);
	for (final Integer element : array) {
	    assertTrue(it.hasNext());
	    assertEquals(element, it.next());
	}
	assertFalse(it.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void arrayIteratorTestAdvanceOverLimitation() {
	final int SIZE = rand.nextInt(100) + 100;
	final Integer[] array = randomIntegerArray(SIZE);
	final Iterator<Integer> it = new ArrayIterator<>(array);
	for (int i = 0; i < SIZE; i++) {
	    assertTrue(it.hasNext());
	    it.next();
	}
	assertFalse(it.hasNext());
	it.next();
    }

    @Test
    public void arrayIteratorTestEmptyArray() {
	final Integer[] array = new Integer[] {};
	final Iterator<Integer> it = new ArrayIterator<>(array);
	assertFalse(it.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void arrayIteratorTestEmptyArrayAdvanceOverLimitation() {
	final Integer[] array = new Integer[] {};
	final Iterator<Integer> it = new ArrayIterator<>(array);
	assertFalse(it.hasNext());
	it.next();
    }

    @Test
    public void arrayIteratorTestSkipNulls() {
	final int size = 50;
	final Integer[] array = new Integer[size];
	for (int i = 0; i < array.length - 1; i++)
	    array[i] = rand.nextBoolean() ? Integer.valueOf(rand.nextInt()) : null;
	array[array.length - 1] = Integer.valueOf(87);
	final Iterator<Integer> it = new ArrayIterator<>(array, true);

	for (int i = 0; i < array.length - 1; i++) {
	    assertTrue(it.hasNext());
	    if (array[i] != null)
		assertEquals(array[i], it.next());
	}
	assertTrue(it.hasNext());
	assertEquals(array[array.length - 1], it.next());
	assertFalse(it.hasNext());
    }

    private static Integer[] randomIntegerArray(final int size) {
	final Integer[] array = new Integer[size];
	for (int i = 0; i < array.length; i++)
	    array[i] = Integer.valueOf(rand.nextInt());
	return array;
    }

}
