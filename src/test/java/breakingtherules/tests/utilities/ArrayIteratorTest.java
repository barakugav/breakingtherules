package breakingtherules.tests.utilities;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Test;

import breakingtherules.tests.TestBase;
import breakingtherules.utilities.ArrayIterator;

public class ArrayIteratorTest extends TestBase {

    @Test(expected = NoSuchElementException.class)
    public void arrayIteratorTestEmptyArray() {
	Integer[] array = new Integer[] {};
	Iterator<Integer> it = new ArrayIterator<>(array);
	assertFalse(it.hasNext());
	it.next();
    }

    @Test
    public void arrayIteratorTest() {
	final int size = 50;
	Integer[] array = new Integer[size];
	for (int i = 0; i < array.length; i++)
	    array[i] = Integer.valueOf(rand.nextInt());
	Iterator<Integer> it = new ArrayIterator<>(array);

	for (int i = 0; i < array.length; i++) {
	    assertTrue(it.hasNext());
	    assertEquals(array[i], it.next());
	}
	assertFalse(it.hasNext());
    }

    @Test
    public void arrayIteratorTestSkipNulls() {
	final int size = 50;
	Integer[] array = new Integer[size];
	for (int i = 0; i < array.length - 1; i++) {
	    array[i] = rand.nextBoolean() ? Integer.valueOf(rand.nextInt()) : null;
	}
	array[array.length - 1] = Integer.valueOf(87);
	Iterator<Integer> it = new ArrayIterator<>(array, true);

	for (int i = 0; i < array.length - 1; i++) {
	    assertTrue(it.hasNext());
	    if (array[i] != null) {
		assertEquals(array[i], it.next());
	    }
	}
	assertTrue(it.hasNext());
	assertEquals(array[array.length - 1], it.next());
	assertFalse(it.hasNext());
    }

}
