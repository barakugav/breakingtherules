package breakingtherules.tests;

import java.util.Random;

import org.junit.Assert;

import breakingtherules.utilities.Utility;

public class TestBase {
    
    protected static final Random rand = new Random();
    
    public static void assertEquals(byte expected, byte actual) {
	assertEquals(null, Byte.valueOf(expected), Byte.valueOf(actual));
    }

    public static void assertEquals(String message, byte expected, byte actual) {
	assertEquals(message, Byte.valueOf(expected), Byte.valueOf(actual));
    }

    public static void assertEquals(short expected, short actual) {
	assertEquals(null, Short.valueOf(expected), Short.valueOf(actual));
    }

    public static void assertEquals(String message, short expected, short actual) {
	assertEquals(message, Short.valueOf(expected), Short.valueOf(actual));
    }

    public static void assertEquals(int expected, int actual) {
	assertEquals(null, Integer.valueOf(expected), Integer.valueOf(actual));
    }

    public static void assertEquals(String message, int expected, int actual) {
	assertEquals(message, Integer.valueOf(expected), Integer.valueOf(actual));
    }

    public static void assertEquals(long expected, long actual) {
	assertEquals(null, Long.valueOf(expected), Long.valueOf(actual));
    }

    public static void assertEquals(String message, long expected, long actual) {
	assertEquals(message, Long.valueOf(expected), Long.valueOf(actual));
    }

    public static void assertEquals(char expected, char actual) {
	assertEquals(null, Character.valueOf(expected), Character.valueOf(actual));
    }

    public static void assertEquals(String message, char expected, char actual) {
	assertEquals(message, Character.valueOf(expected), Character.valueOf(actual));
    }

    public static void assertEquals(float expected, float actual, float delta) {
	assertEquals(null, expected, actual, delta);
    }

    public static void assertEquals(String message, float expected, float actual, float delta) {
	if (Float.compare(expected, actual) != 0 && Math.abs(expected - actual) > delta) {
	    failNotEqual(message, Float.valueOf(expected), Float.valueOf(actual));
	}
    }

    public static void assertEquals(double expected, double actual, double delta) {
	assertEquals(null, expected, actual, delta);
    }

    public static void assertEquals(String message, double expected, double actual, double delta) {
	if (Double.compare(expected, actual) != 0 && Math.abs(expected - actual) > delta) {
	    failNotEqual(message, Double.valueOf(expected), Double.valueOf(actual));
	}
    }

    public static void assertEquals(boolean expected, boolean actual) {
	assertEquals(null, Boolean.valueOf(expected), Boolean.valueOf(actual));
    }

    public static void assertEquals(String message, boolean expected, boolean actual) {
	assertEquals(message, Boolean.valueOf(expected), Boolean.valueOf(actual));
    }

    public static void assertEquals(Object expected, Object actual) {
	assertEquals(null, expected, actual);
    }

    public static void assertEquals(String message, Object expected, Object actual) {
	if (!Utility.equals(expected, actual)) {
	    failNotEqual(message, expected, actual);
	}
    }

    private static void failNotEqual(String message, Object expected, Object actual) {
	message = message != null ? message + ": " : "";
	message += Utility.formatEqual(expected, actual);
	Assert.fail(message);
    }

}
