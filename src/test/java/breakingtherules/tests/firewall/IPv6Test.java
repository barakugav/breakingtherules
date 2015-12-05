package breakingtherules.tests.firewall;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Random;

import org.junit.Test;

import breakingtherules.firewall.IPv6;

public class IPv6Test {

    private static final Random rand = new Random();

    /*--------------------Test Methods--------------------*/

    @Test
    public void constructorTestBasic() {
	try {
	    int[] address = getRandomAddress();
	    new IPv6(address);

	} catch (IllegalArgumentException e) {
	    fail("Excetion should not created: " + e.getMessage());
	}
    }

    @Test
    public void constructorTestBasicWillNullAdressTest() {

	try {
	    int[] address = null;
	    new IPv6(address);
	    fail("Allowed IPv6 creation will null address arg");

	} catch (IllegalArgumentException e) {
	    // Success
	}
    }

    @Test
    public void constructorTestWithPrefixLength() {
	try {
	    int[] address = getRandomAddress();
	    int prefixLength = getRandomPrefixLength();
	    new IPv6(address, prefixLength);

	} catch (IllegalArgumentException e) {
	    fail("Excetion should not created: " + e.getMessage());
	}
    }

    @Test
    public void constructorTestWithPrefixLengthNullAdress() {
	try {
	    int[] address = null;
	    int prefixLength = getRandomPrefixLength();
	    new IPv6(address, prefixLength);
	    fail("Allowed IPv6 creation will null address arg");

	} catch (IllegalArgumentException e) {
	    // Success
	}
    }

    @Test
    public void constructorTestWithNegativePrefixLength() {
	try {
	    int[] address = getRandomAddress();
	    int prefixLength = -1;
	    new IPv6(address, prefixLength);
	    fail("Allowed IPv6 creation will illegal prefix length arg");

	} catch (IllegalArgumentException e) {
	    // Success
	}
    }

    @Test
    public void constructorTestWithPrefixLengthOverMaxLength() {
	try {
	    int[] address = getRandomAddress();
	    int prefixLength = 129;
	    new IPv6(address, prefixLength);
	    fail("Allowed IPv6 creation will illegal prefix length arg ( > 128)");

	} catch (IllegalArgumentException e) {
	    // Success
	}
    }

    @Test
    public void constructorFromStringTest() {
	try {
	    String ipStr = "215:255:457:4966:0:65535:78:1257";
	    new IPv6(ipStr);

	} catch (IllegalArgumentException e) {
	    fail("Excetion should not created: " + e.getMessage());
	}
    }

    @Test
    public void constructorFromStringTest3Blocks() {
	try {
	    String ipStr = "255:0:46:4784:48:74:89";
	    new IPv6(ipStr);
	    fail("Allowed IPv6 creation will illegal format (7 blocks)");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void constructorFromStringTest5Blocks() {
	try {
	    String ipStr = "255:0:2:46:47863:32146:879:11112:30";
	    new IPv6(ipStr);
	    fail("Allowed IPv6 creation will illegal format (9 blocks)");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void constructorFromStringTestBlockOver65535() {
	try {
	    String ipStr = "255:65536:4:46:801:24020:4852:31";
	    new IPv6(ipStr);
	    fail("Allowed IPv6 creation will illegal format (block over 65535)");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void constructorFromStringTestBlockUnder0() {
	try {
	    String ipStr = "255:-55:4:46:44:879:326:15";
	    new IPv6(ipStr);
	    fail("Allowed IPv6 creation will illegal format (block under 0)");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void constructorFromStringTestDoubleDot() {
	try {
	    String ipStr = "255::2:4:46:1:1:1:1";
	    new IPv6(ipStr);
	    fail("Allowed IPv6 creation will illegal format (two dots)");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void constructorFromStringTestNegativePrefixLength() {
	try {
	    String ipStr = "255:2:4:46:4:5:6:1/-1";
	    new IPv6(ipStr);
	    fail("Allowed IPv6 creation will illegal format (prefix length < 0). ");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void constructorFromStringTestPrefixLengthOver32() {
	try {
	    String ipStr = "255:2:549:785:324:7841:4:46/129";
	    new IPv6(ipStr);
	    fail("Allowed IPv6 creation will illegal format (prefix length > 128)");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void constructorFromStringTestExtraNumbers() {
	try {
	    String ipStr = "255:2:4:46:14:48:79:13245/1 5";
	    new IPv6(ipStr);
	    fail("Allowed IPv6 creation will illegal format (extra numbers)");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void getAddressTestWithBasicConstructor() {
	int[] address = getRandomAddress();
	IPv6 ip = new IPv6(address);
	assertEquals(address, ip.getAddress());
    }

    @Test
    public void getAddressTestConstructorWithPrefixLength() {
	int[] address = getRandomAddress();
	IPv6 ip = new IPv6(address, getRandomPrefixLength());
	assertEquals(address, ip.getAddress());
    }

    @Test
    public void getPrefixLengthTestBasicConstructor() {
	IPv6 ip = new IPv6(getRandomAddress());
	assertEquals(128, ip.getConstPrefixLength());
    }

    @Test
    public void getPrefixLengthTestConstructorWithPrefixLength() {
	int prefixLength = getRandomPrefixLength();
	IPv6 ip = new IPv6(getRandomAddress(), prefixLength);
	assertEquals(prefixLength, ip.getConstPrefixLength());
    }

    @Test
    public void getParentTest() {
	IPv6 ip = new IPv6(getRandomAddress());

	for (int expectedLength = 128; expectedLength > 0; expectedLength--) {
	    assertTrue(ip.hasParent());
	    assertNotNull(ip.getParent());
	    ip = ip.getParent();
	}

	assertFalse(ip.hasParent());
	assertNull(ip.getParent());
    }

    @Test
    public void getChildrenTest() {
	IPv6 ip = new IPv6(getRandomAddress(), rand.nextInt(28) + 101);
	assertTrue(childrenTestRecurtion(ip));
    }

    @Test
    public void equalsTestItselfBasicConstructor() {
	int[] address = getRandomAddress();
	IPv6 ip1 = new IPv6(address);
	IPv6 ip2 = new IPv6(address);
	assertTrue(ip1.equals(ip2));
	assertTrue(ip2.equals(ip1));
    }

    @Test
    public void equalsTestItselfOneIpWithConstructorWithPrefixLength() {
	int[] address = getRandomAddress();
	IPv6 ip1 = new IPv6(address);
	IPv6 ip2 = new IPv6(address, 128);
	assertTrue(ip1.equals(ip2));
	assertTrue(ip2.equals(ip1));
    }

    @Test
    public void equalsTestItselfTwoIpwithConstructorWithPrefixLength() {
	int[] address = getRandomAddress();
	int prefix = getRandomPrefixLength();
	IPv6 ip1 = new IPv6(address, prefix);
	IPv6 ip2 = new IPv6(address, prefix);
	assertTrue(ip1.equals(ip2));
	assertTrue(ip2.equals(ip1));
    }

    @Test
    public void equalsTestNotEqualsItselfTwoDifferentPrefixLength() {
	int[] address = getRandomAddress();
	int prefix1 = getRandomPrefixLength();
	int prefix2;
	do {
	    prefix2 = getRandomPrefixLength();
	} while (prefix1 == prefix2);

	IPv6 ip1 = new IPv6(address, prefix1);
	IPv6 ip2 = new IPv6(address, prefix2);
	assertFalse(ip1.equals(ip2));
	assertFalse(ip2.equals(ip1));
    }

    @Test
    public void containsTestContainsItself() {
	IPv6 ip1 = new IPv6(new int[] { 0, 0, 0, 0, 0, 0, 0, 0 }, 0);
	IPv6 ip2 = new IPv6(new int[] { 0, 0, 0, 0, 0, 0, 0, 0 }, 0);
	assertTrue(ip1.contains(ip2));
	assertTrue(ip2.contains(ip1));
    }

    @Test
    public void containsTestContainsItselfNoPrefixLength() {
	IPv6 ip1 = new IPv6(new int[] { 0, 160, 40, 0, 10, 0, 540, 0 });
	IPv6 ip2 = new IPv6(new int[] { 0, 160, 40, 0, 10, 0, 540, 0 });
	assertTrue(ip1.contains(ip2));
	assertTrue(ip2.contains(ip1));
    }

    @Test
    public void containsTestZeroPrefixLengthContainsAll() {
	IPv6 ip1 = new IPv6(new int[] { 0, 0, 0, 0, 0, 0, 0, 0 }, 0);
	IPv6 ip2 = new IPv6(getRandomAddress());
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    @Test
    public void containsTestPrefixLength16() {
	IPv6 ip1 = new IPv6(new int[] { 145, 0, 0, 0, 0, 0, 0, 0 }, 16);
	IPv6 ip2 = new IPv6(new int[] { 145, 55, 0, 0, 0, 0, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = new IPv6(new int[] { 145, 0, 48, 0, 0, 0, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = new IPv6(new int[] { 145, 255, 255, 255, 0, 0, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = new IPv6(new int[] { 146, 0, 0, 0, 0, 0, 0, 0 });
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    @Test
    public void containsTestPrefixLength31() {
	IPv6 ip1 = new IPv6(new int[] { 16, 216, 0, 0, 0, 0, 0, 0 }, 31);
	IPv6 ip2 = new IPv6(new int[] { 16, 217, 11, 7, 0, 0, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = new IPv6(new int[] { 16, 216, 48, 0, 0, 0, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = new IPv6(new int[] { 16, 216, 45, 77, 0, 0, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = new IPv6(new int[] { 17, 216, 14, 42, 0, 0, 0, 0 });
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = new IPv6(new int[] { 16, 218, 36, 38, 0, 0, 0, 0 });
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    @Test
    public void containsTestPrefixLength45() {
	IPv6 ip1 = new IPv6(new int[] { 0, 160, 40, 0, 0, 0, 0, 0 }, 45);
	IPv6 ip2 = new IPv6(new int[] { 0, 160, 47, 7, 0, 0, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = new IPv6(new int[] { 0, 160, 41, 0, 0, 0, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = new IPv6(new int[] { 0, 160, 40, 255, 0, 0, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = new IPv6(new int[] { 0, 160, 96, 0, 0, 0, 0, 0 });
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = new IPv6(new int[] { 0, 160, 7, 44, 0, 0, 0, 0 });
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    @Test
    public void containsTestPrefixLength59() {
	IPv6 ip1 = new IPv6(new int[] { 41, 99, 243, 160, 0, 0, 0, 0 }, 59);
	IPv6 ip2 = new IPv6(new int[] { 41, 99, 243, 160, 0, 0, 0, 0 }, 64);
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = new IPv6(new int[] { 41, 99, 243, 160, 0, 0, 0, 0 }, 59);
	assertTrue(ip1.contains(ip2));
	assertTrue(ip2.contains(ip1));
	ip2 = new IPv6(new int[] { 41, 99, 243, 168, 0, 0, 0, 0 }, 61);
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = new IPv6(new int[] { 41, 99, 243, 176, 0, 0, 0, 0 }, 62);
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = new IPv6(new int[] { 41, 99, 243, 224, 0, 0, 0, 0 }, 59);
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    @Test
    public void containsTestFullIpNotContainsOthers() {
	IPv6 ip1 = new IPv6(new int[] { 0, 160, 40, 0, 47, 8888, 78, 0 });
	IPv6 ip2 = new IPv6(getRandomAddress());
	if (ip1.equals(ip2)) {
	    System.out.print(
		    "*********************************\nThere is no f***ing way!\n*********************************\n");
	    return;
	}

	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    /*--------------------Help Methods--------------------*/

    private static int[] getRandomAddress() {
	int[] address = new int[8];
	for (int i = 0; i < address.length; i++)
	    address[i] = rand.nextInt(1 << 16);
	return address;
    }

    private static int getRandomPrefixLength() {
	return rand.nextInt(129);
    }

    private boolean childrenTestRecurtion(IPv6 ip) {
	if (ip == null)
	    return false;
	if (!ip.hasChildren())
	    return true;

	IPv6[] children = ip.getChildren();
	IPv6 childZero = children[0];
	IPv6 childOne = children[1];

	return childrenTestRecurtion(childZero) && childrenTestRecurtion(childOne);
    }

}
