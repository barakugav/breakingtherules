package breakingtherules.tests;

import org.junit.Assert;

import breakingtherules.utilities.Utility;

public class JUnitUtilities {

    public static void deepAssertEquals(Object expected, Object actual) {
	deepAssertEquals(null, expected, actual);
    }

    public static void deepAssertEquals(String message, Object expected, Object actual) {
	if (!Utility.equals(expected, actual)) {
	    message = message != null ? message + ": " : "";
	    message += Utility.format(expected, actual);
	    Assert.fail(message);
	}
    }

}
