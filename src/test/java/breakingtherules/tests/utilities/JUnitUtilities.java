package breakingtherules.tests.utilities;

import org.junit.Assert;

import breakingtherules.utilities.Utility;

public class JUnitUtilities {

    public static void advanceAssertEquals(Object o1, Object o2) {
	if (!Utility.equals(o1, o2)) {
	    Assert.fail(Utility.format(o1, o2));
	}
    }

}
