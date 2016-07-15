package breakingtherules.tests;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.function.Consumer;

import org.junit.Assert;

import breakingtherules.utilities.Utility;

@SuppressWarnings("javadoc")
public class TestBase {

    protected static final Random rand = new Random();

    public static void assertEquals(final boolean expected, final boolean actual) {
	assertEquals(null, Boolean.valueOf(expected), Boolean.valueOf(actual));
    }

    public static void assertEquals(final byte expected, final byte actual) {
	assertEquals(null, Byte.valueOf(expected), Byte.valueOf(actual));
    }

    public static void assertEquals(final char expected, final char actual) {
	assertEquals(null, Character.valueOf(expected), Character.valueOf(actual));
    }

    public static void assertEquals(final double expected, final double actual, final double delta) {
	assertEquals(null, expected, actual, delta);
    }

    public static void assertEquals(final int expected, final int actual) {
	assertEquals(null, Integer.valueOf(expected), Integer.valueOf(actual));
    }

    public static void assertEquals(final long expected, final long actual) {
	assertEquals(null, Long.valueOf(expected), Long.valueOf(actual));
    }

    public static void assertEquals(final Object expected, final Object actual) {
	assertEquals(null, expected, actual);
    }

    public static void assertEquals(final short expected, final short actual) {
	assertEquals(null, Short.valueOf(expected), Short.valueOf(actual));
    }

    public static void assertEquals(final String message, final boolean expected, final boolean actual) {
	assertEquals(message, Boolean.valueOf(expected), Boolean.valueOf(actual));
    }

    public static void assertEquals(final String message, final byte expected, final byte actual) {
	assertEquals(message, Byte.valueOf(expected), Byte.valueOf(actual));
    }

    public static void assertEquals(final String message, final char expected, final char actual) {
	assertEquals(message, Character.valueOf(expected), Character.valueOf(actual));
    }

    public static void assertEquals(final String message, final double expected, final double actual,
	    final double delta) {
	if (Double.compare(expected, actual) != 0 && Math.abs(expected - actual) > delta)
	    failNotEqual(message, Double.valueOf(expected), Double.valueOf(actual));
    }

    public static void assertEquals(final String message, final float expected, final float actual, final float delta) {
	if (Float.compare(expected, actual) != 0 && Math.abs(expected - actual) > delta)
	    failNotEqual(message, Float.valueOf(expected), Float.valueOf(actual));
    }

    public static void assertEquals(final String message, final int expected, final int actual) {
	assertEquals(message, Integer.valueOf(expected), Integer.valueOf(actual));
    }

    public static void assertEquals(final String message, final long expected, final long actual) {
	assertEquals(message, Long.valueOf(expected), Long.valueOf(actual));
    }

    public static void assertEquals(final String message, final Object expected, final Object actual) {
	if (!Utility.deepEquals(expected, actual))
	    failNotEqual(message, expected, actual);
    }

    public static void assertEquals(final String message, final short expected, final short actual) {
	assertEquals(message, Short.valueOf(expected), Short.valueOf(actual));
    }

    public static void assertEqualsfinal(final float expected, final float actual, final float delta) {
	assertEquals(null, expected, actual, delta);
    }

    public static void tempFileTest(final String prefix, final String suffix, final Consumer<File> test)
	    throws IOException {
	File tempFile = null;
	try {
	    tempFile = File.createTempFile("breakingtherules_tests_" + prefix, suffix);
	    tempFile.deleteOnExit();
	    test.accept(tempFile);
	} finally {
	    tempFile.delete();
	}
    }

    private static void failNotEqual(final String message, final Object expected, final Object actual) {
	Assert.fail((message != null ? message + ": " : "") + Utility.formatEqual(expected, actual));
    }

}
