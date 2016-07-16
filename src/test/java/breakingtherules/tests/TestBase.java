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

    protected static void assertEquals(final boolean expected, final boolean actual) {
	assertEquals(null, expected, actual);
    }

    protected static void assertEquals(final byte expected, final byte actual) {
	assertEquals(null, expected, actual);
    }

    protected static void assertEquals(final char expected, final char actual) {
	assertEquals(null, expected, actual);
    }

    @SuppressWarnings("unused")
    @Deprecated
    protected static void assertEquals(final double expected, final double actual) {
	throw new UnsupportedOperationException("Use assertEquals(double expected, double actual, double delta)");
    }

    protected static void assertEquals(final double expected, final double actual, final double delta) {
	assertEquals(null, expected, actual, delta);
    }

    @SuppressWarnings("unused")
    @Deprecated
    protected static void assertEquals(final float expected, final float actual) {
	throw new UnsupportedOperationException("Use assertEquals(double expected, float actual, float delta)");
    }

    protected static void assertEquals(final float expected, final float actual, final float delta) {
	assertEquals(null, expected, actual, delta);
    }

    protected static void assertEquals(final int expected, final int actual) {
	assertEquals(null, expected, actual);
    }

    protected static void assertEquals(final long expected, final long actual) {
	assertEquals(null, expected, actual);
    }

    protected static void assertEquals(final Object expected, final Object actual) {
	assertEquals(null, expected, actual);
    }

    protected static void assertEquals(final short expected, final short actual) {
	assertEquals(null, expected, actual);
    }

    protected static void assertEquals(final String message, final boolean expected, final boolean actual) {
	assertEquals(message, Boolean.valueOf(expected), Boolean.valueOf(actual));
    }

    protected static void assertEquals(final String message, final byte expected, final byte actual) {
	assertEquals(message, Byte.valueOf(expected), Byte.valueOf(actual));
    }

    protected static void assertEquals(final String message, final char expected, final char actual) {
	assertEquals(message, Character.valueOf(expected), Character.valueOf(actual));
    }

    @SuppressWarnings("unused")
    @Deprecated
    protected static void assertEquals(final String message, final double expected, final double actual) {
	throw new UnsupportedOperationException(
		"Use assertEquals(String message, double expected, double actual, double delta)");
    }

    protected static void assertEquals(final String message, final double expected, final double actual,
	    final double delta) {
	if (Double.compare(expected, actual) != 0 && Math.abs(expected - actual) > delta)
	    failNotEquals(message, Double.valueOf(expected), Double.valueOf(actual));
    }

    @SuppressWarnings("unused")
    @Deprecated
    protected static void assertEquals(final String message, final float expected, final float actual) {
	throw new UnsupportedOperationException(
		"Use assertEquals(String message, float expected, float actual, float delta)");
    }

    protected static void assertEquals(final String message, final float expected, final float actual,
	    final float delta) {
	if (Float.compare(expected, actual) != 0 && Math.abs(expected - actual) > delta)
	    failNotEquals(message, Float.valueOf(expected), Float.valueOf(actual));
    }

    protected static void assertEquals(final String message, final int expected, final int actual) {
	assertEquals(message, Integer.valueOf(expected), Integer.valueOf(actual));
    }

    protected static void assertEquals(final String message, final long expected, final long actual) {
	assertEquals(message, Long.valueOf(expected), Long.valueOf(actual));
    }

    protected static void assertEquals(final String message, final Object expected, final Object actual) {
	if (!Utility.deepEquals(expected, actual))
	    failNotEquals(message, expected, actual);
    }

    protected static void assertEquals(final String message, final short expected, final short actual) {
	assertEquals(message, Short.valueOf(expected), Short.valueOf(actual));
    }

    // ==============

    protected static void assertNotEquals(final boolean unexpected, final boolean actual) {
	assertNotEquals(null, unexpected, actual);
    }

    protected static void assertNotEquals(final byte unexpected, final byte actual) {
	assertNotEquals(null, unexpected, actual);
    }

    protected static void assertNotEquals(final char unexpected, final char actual) {
	assertNotEquals(null, unexpected, actual);
    }

    @SuppressWarnings("unused")
    @Deprecated
    protected static void assertNotEquals(final double unexpected, final double actual) {
	throw new UnsupportedOperationException("Use assertNotEquals(double unexpected, double actual, double delta");
    }

    protected static void assertNotEquals(final double unexpected, final double actual, final double delta) {
	assertNotEquals(null, unexpected, actual, delta);
    }

    @SuppressWarnings("unused")
    @Deprecated
    protected static void assertNotEquals(final float unexpected, final float actual) {
	throw new UnsupportedOperationException("Use assertNotEquals(float unexpected, float actual, float delta");
    }

    protected static void assertNotEquals(final float unexpected, final float actual, final float delta) {
	assertNotEquals(null, unexpected, actual, delta);
    }

    protected static void assertNotEquals(final int unexpected, final int actual) {
	assertNotEquals(null, unexpected, actual);
    }

    protected static void assertNotEquals(final long unexpected, final long actual) {
	assertNotEquals(null, unexpected, actual);
    }

    protected static void assertNotEquals(final Object unexpected, final Object actual) {
	assertNotEquals(null, unexpected, actual);
    }

    protected static void assertNotEquals(final short unexpected, final short actual) {
	assertNotEquals(null, unexpected, actual);
    }

    protected static void assertNotEquals(final String message, final boolean unexpected, final boolean actual) {
	assertNotEquals(message, Boolean.valueOf(unexpected), Boolean.valueOf(actual));
    }

    protected static void assertNotEquals(final String message, final byte unexpected, final byte actual) {
	assertNotEquals(message, Byte.valueOf(unexpected), Byte.valueOf(actual));
    }

    protected static void assertNotEquals(final String message, final char unexpected, final char actual) {
	assertNotEquals(message, Character.valueOf(unexpected), Character.valueOf(actual));
    }

    @SuppressWarnings("unused")
    @Deprecated
    protected static void assertNotEquals(final String message, final double unexpected, final double actual) {
	throw new UnsupportedOperationException(
		"Use assertNotEquals(String message, double unexpected, double actual, double delta");
    }

    protected static void assertNotEquals(final String message, final double unexpected, final double actual,
	    final double delta) {
	if (Double.compare(unexpected, actual) == 0 || Math.abs(unexpected - actual) <= delta)
	    failEquals(message, Double.valueOf(actual));
    }

    @SuppressWarnings("unused")
    @Deprecated
    protected static void assertNotEquals(final String message, final float unexpected, final float actual) {
	throw new UnsupportedOperationException(
		"Use assertNotEquals(String message, float unexpected, float actual, float delta");
    }

    protected static void assertNotEquals(final String message, final float unexpected, final float actual,
	    final float delta) {
	if (Float.compare(unexpected, actual) == 0 || Math.abs(unexpected - actual) <= delta)
	    failEquals(message, Float.valueOf(actual));
    }

    protected static void assertNotEquals(final String message, final int unexpected, final int actual) {
	assertNotEquals(message, Integer.valueOf(unexpected), Integer.valueOf(actual));
    }

    protected static void assertNotEquals(final String message, final long unexpected, final long actual) {
	assertNotEquals(message, Long.valueOf(unexpected), Long.valueOf(actual));
    }

    protected static void assertNotEquals(final String message, final Object unexpected, final Object actual) {
	if (Utility.deepEquals(unexpected, actual))
	    failEquals(message, actual);
    }

    protected static void assertNotEquals(final String message, final short unexpected, final short actual) {
	assertNotEquals(message, Short.valueOf(unexpected), Short.valueOf(actual));
    }

    // ==============

    protected static void tempFileTest(final String prefix, final String suffix, final Consumer<File> test)
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

    private static void failEquals(final String message, final Object actual) {
	Assert.fail((message != null ? message + ": " : "Value should be different: ") + " actual <"
		+ Utility.toString(actual) + ">");
    }

    private static void failNotEquals(final String message, final Object expected, final Object actual) {
	Assert.fail((message != null ? message + ": " : "") + Utility.formatEqual(expected, actual));
    }

}
