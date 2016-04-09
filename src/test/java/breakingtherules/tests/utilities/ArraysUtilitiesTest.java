package breakingtherules.tests.utilities;

import static breakingtherules.tests.utilities.JUnitUtilities.advanceAssertEquals;

import java.util.Arrays;

import org.junit.Test;

import breakingtherules.utilities.ArraysUtilities;

public class ArraysUtilitiesTest {

    private static final boolean T = true;
    private static final boolean F = false;

    @Test
    public void mergeBooleansTest() {
	System.out.println("# ArraysUtilitiesTest mergeBooleansTest");
	boolean[] a = new boolean[] { T, T, F, T, F };
	boolean[] b = new boolean[] { F, T, T, F, T };
	boolean[] expected = new boolean[] { T, T, F, T, F, F, T, T, F, T };
	boolean[] actual = ArraysUtilities.merge(a, b);
	advanceAssertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mergeBooleansTestNullArray() {
	System.out.println("# ArraysUtilitiesTest mergeBooleansTestNullArray");
	ArraysUtilities.merge(new boolean[] { T, F }, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mergeBooleansTestNullArrays() {
	System.out.println("# ArraysUtilitiesTest mergeBooleansTestNullArrays");
	boolean[][] arrays = null;
	ArraysUtilities.merge(arrays);
    }

    @Test
    public void intToBooleansTest() {
	System.out.println("# ArraysUtilitiesTest intToBooleansTest");
	int length = 6;
	int num = 0b011001;
	boolean[] expected = new boolean[] { F, T, T, F, F, T };
	boolean[] actual = ArraysUtilities.intToBooleans(num, length);
	advanceAssertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void intToBooleansTestNegativeLength() {
	System.out.println("# ArraysUtilitiesTest intToBooleansTestNegativeLength");
	int length = -1;
	int num = 0b011001;
	ArraysUtilities.intToBooleans(num, length);
    }

    public void toArrayTest() {
	System.out.println("# ArraysUtilitiesTest toArrayTest");
	String[] expected = new String[] { "hello", "big", "world" };
	String[] actual = ArraysUtilities.toArray(Arrays.asList(expected));
	advanceAssertEquals(expected, actual);
    }

}
