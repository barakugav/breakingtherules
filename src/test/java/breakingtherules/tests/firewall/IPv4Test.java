package breakingtherules.tests.firewall;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Random;

import org.junit.Test;

import breakingtherules.firewall.IPv4;
import breakingtherules.firewall.IPv6;

public class IPv4Test {

    private static final Random rand = new Random();

    /*--------------------Test Methods--------------------*/

    @Test
    public void constructorTestBasic() {
	try {
	    int[] address = getRandomAddress();
	    new IPv4(address);

	} catch (IllegalArgumentException e) {
	    fail("Excetion should not created: " + e.getMessage());
	}
    }

    @Test
    public void constructorTestBasicWillNullAdressTest() {

	try {
	    int[] address = null;
	    new IPv4(address);
	    fail("Allowed IPv4 creation will null address arg");

	} catch (IllegalArgumentException e) {
	    // Success
	}
    }

    @Test
    public void constructorTestWithPrefixLength() {
	try {
	    int[] address = getRandomAddress();
	    int prefixLength = getRandomPrefixLength();
	    new IPv4(address, prefixLength);

	} catch (IllegalArgumentException e) {
	    fail("Excetion should not created: " + e.getMessage());
	}
    }

    @Test
    public void constructorTestWithPrefixLengthNullAdress() {
	try {
	    int[] address = null;
	    int prefixLength = getRandomPrefixLength();
	    new IPv4(address, prefixLength);
	    fail("Allowed IPv4 creation will null address arg");

	} catch (IllegalArgumentException e) {
	    // Success
	}
    }

    @Test
    public void constructorTestWithNegativePrefixLength() {
	try {
	    int[] address = getRandomAddress();
	    int prefixLength = -1;
	    new IPv4(address, prefixLength);
	    fail("Allowed IPv4 creation will illegal prefix length arg");

	} catch (IllegalArgumentException e) {
	    // Success
	}
    }

    @Test
    public void constructorTestWithPrefixLengthOverMaxLength() {
	try {
	    int[] address = getRandomAddress();
	    int prefixLength = 33;
	    new IPv4(address, prefixLength);
	    fail("Allowed IPv4 creation will illegal prefix length arg");

	} catch (IllegalArgumentException e) {
	    // Success
	}
    }

    @Test
    public void constructorFromStringTest() {
	try {
	    String ipStr = "215.255.0.46";
	    new IPv4(ipStr);

	} catch (IllegalArgumentException e) {
	    fail("Excetion should not created: " + e.getMessage());
	}
    }

    @Test
    public void constructorFromStringTest3Blocks() {
	try {
	    String ipStr = "255.0.46";
	    new IPv4(ipStr);
	    fail("Allowed IPv4 creation will illegal format (3 blocks)");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void constructorFromStringTest5Blocks() {
	try {
	    String ipStr = "255.0.2.2.46";
	    new IPv4(ipStr);
	    fail("Allowed IPv4 creation will illegal format (5 blocks)");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void constructorFromStringTestBlockOver255() {
	try {
	    String ipStr = "255.300.4.46";
	    new IPv4(ipStr);
	    fail("Allowed IPv4 creation will illegal format (block over 255)");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void constructorFromStringTestBlockUnder0() {
	try {
	    String ipStr = "255.-55.4.46";
	    new IPv4(ipStr);
	    fail("Allowed IPv4 creation will illegal format (block under 0)");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void constructorFromStringTestDoubleDot() {
	try {
	    String ipStr = "255..2.4.46";
	    new IPv4(ipStr);
	    fail("Allowed IPv4 creation will illegal format (two dots)");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void constructorFromStringTestNegativePrefixLength() {
	try {
	    String ipStr = "255.2.4.46/-1";
	    new IPv4(ipStr);
	    fail("Allowed IPv4 creation will illegal format (prefix length < 0). ");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void constructorFromStringTestPrefixLengthOver32() {
	try {
	    String ipStr = "255.2.4.46/33";
	    new IPv4(ipStr);
	    fail("Allowed IPv4 creation will illegal format (prefix length > 32)");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void constructorFromStringTestExtraNumbers() {
	try {
	    String ipStr = "255.2.4.46/1 5";
	    new IPv4(ipStr);
	    fail("Allowed IPv4 creation will illegal format (extra numbers)");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void getAddressTestWithBasicConstructor() {
	int[] address = getRandomAddress();
	IPv4 ip = new IPv4(address);
	assertEquals(address, ip.getAddress());
    }

    @Test
    public void getAddressTestConstructorWithPrefixLength() {
	int[] address = getRandomAddress();
	IPv4 ip = new IPv4(address, getRandomPrefixLength());
	assertEquals(address, ip.getAddress());
    }

    @Test
    public void getPrefixLengthTestBasicConstructor() {
	IPv4 ip = new IPv4(getRandomAddress());
	assertEquals(32, ip.getConstPrefixLength());
    }

    @Test
    public void getPrefixLengthTestConstructorWithPrefixLength() {
	int prefixLength = getRandomPrefixLength();
	IPv4 ip = new IPv4(getRandomAddress(), prefixLength);
	assertEquals(prefixLength, ip.getConstPrefixLength());
    }

    @Test
    public void getParentTest() {
	IPv4 ip = new IPv4(getRandomAddress());

	for (int expectedLength = 32; expectedLength > 0; expectedLength--) {
	    assertTrue(ip.hasParent());
	    assertNotNull(ip.getParent());
	    ip = ip.getParent();
	}

	assertFalse(ip.hasParent());
	assertNull(ip.getParent());
    }

    @Test
    public void getChildrenTest() {
	IPv4 ip = new IPv4(getRandomAddress(), rand.nextInt(11) + 22);
	assertTrue(childrenTestRecurtion(ip));
    }

    @Test
    public void equalsTestItselfBasicConstructor() {
	int[] address = getRandomAddress();
	IPv4 ip1 = new IPv4(address);
	IPv4 ip2 = new IPv4(address);
	assertTrue(ip1.equals(ip2));
	assertTrue(ip2.equals(ip1));
    }

    @Test
    public void equalsTestItselfOneIpWithConstructorWithPrefixLength() {
	int[] address = getRandomAddress();
	IPv4 ip1 = new IPv4(address);
	IPv4 ip2 = new IPv4(address, 32);
	assertTrue(ip1.equals(ip2));
	assertTrue(ip2.equals(ip1));
    }

    @Test
    public void equalsTestItselfTwoIpwithConstructorWithPrefixLength() {
	int[] address = getRandomAddress();
	int prefix = getRandomPrefixLength();
	IPv4 ip1 = new IPv4(address, prefix);
	IPv4 ip2 = new IPv4(address, prefix);
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

	IPv4 ip1 = new IPv4(address, prefix1);
	IPv4 ip2 = new IPv4(address, prefix2);
	assertFalse(ip1.equals(ip2));
	assertFalse(ip2.equals(ip1));
    }

    @Test
    public void containsTestContainsItself() {
	IPv4 ip1 = new IPv4(new int[] { 0, 0, 0, 0 }, 0);
	IPv4 ip2 = new IPv4(new int[] { 0, 0, 0, 0 }, 0);
	assertTrue(ip1.contains(ip2));
	assertTrue(ip2.contains(ip1));
    }

    @Test
    public void containsTestNotContainsNull() {
	int[] address = getRandomAddress();
	int prefixLength = getRandomPrefixLength();
	IPv4 ip1 = new IPv4(address, prefixLength);
	IPv4 ip2 = null;
	assertFalse(ip1.contains(ip2));
    }

    @Test
    public void containsTestContainsItselfNoPrefixLength() {
	IPv4 ip1 = new IPv4(new int[] { 0, 160, 40, 0 });
	IPv4 ip2 = new IPv4(new int[] { 0, 160, 40, 0 });
	assertTrue(ip1.contains(ip2));
	assertTrue(ip2.contains(ip1));
    }

    @Test
    public void containsTestZeroPrefixLengthContainsAll() {
	IPv4 ip1 = new IPv4(new int[] { 0, 0, 0, 0 }, 0);
	IPv4 ip2 = new IPv4(getRandomAddress());
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    @Test
    public void containsTestPrefixLength8() {
	IPv4 ip1 = new IPv4(new int[] { 145, 0, 0, 0 }, 8);
	IPv4 ip2 = new IPv4(new int[] { 145, 55, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = new IPv4(new int[] { 145, 0, 48, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = new IPv4(new int[] { 145, 255, 255, 255 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = new IPv4(new int[] { 146, 0, 0, 0 });
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    @Test
    public void containsTestPrefixLength15() {
	IPv4 ip1 = new IPv4(new int[] { 16, 216, 0, 0 }, 15);
	IPv4 ip2 = new IPv4(new int[] { 16, 217, 11, 7 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = new IPv4(new int[] { 16, 216, 48, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = new IPv4(new int[] { 16, 216, 45, 77 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = new IPv4(new int[] { 17, 216, 14, 42 });
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = new IPv4(new int[] { 16, 218, 36, 38 });
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    @Test
    public void containsTestPrefixLength21() {
	IPv4 ip1 = new IPv4(new int[] { 0, 160, 40, 0 }, 21);
	IPv4 ip2 = new IPv4(new int[] { 0, 160, 47, 7 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = new IPv4(new int[] { 0, 160, 41, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = new IPv4(new int[] { 0, 160, 40, 255 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = new IPv4(new int[] { 0, 160, 96, 0 });
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = new IPv4(new int[] { 0, 160, 7, 44 });
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    @Test
    public void containsTestPrefixLength27() {
	IPv4 ip1 = new IPv4(new int[] { 41, 99, 243, 160 }, 27);
	IPv4 ip2 = new IPv4(new int[] { 41, 99, 243, 160 }, 32);
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = new IPv4(new int[] { 41, 99, 243, 160 }, 27);
	assertTrue(ip1.contains(ip2));
	assertTrue(ip2.contains(ip1));
	ip2 = new IPv4(new int[] { 41, 99, 243, 168 }, 29);
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = new IPv4(new int[] { 41, 99, 243, 176 }, 30);
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = new IPv4(new int[] { 41, 99, 243, 224 }, 27);
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    @Test
    public void containsTestFullIpNotContainsOthers() {
	IPv4 ip1 = new IPv4(new int[] { 0, 160, 40, 0 });
	IPv4 ip2 = new IPv4(getRandomAddress());
	if (ip1.equals(ip2)) {
	    System.out.print(
		    "*********************************\nThere is no f***ing way!\n*********************************\n");
	    return;
	}

	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    @Test
    public void containsTestNotContainsIPv6() {
	IPv4 ip4 = new IPv4(getRandomAddress(), getRandomPrefixLength());
	IPv6 ip6 = getRandomIPv6();
	assertFalse(ip4.contains(ip6));
    }

    /*--------------------Help Methods--------------------*/

    private static int[] getRandomAddress() {
	int[] address = new int[4];
	for (int i = 0; i < address.length; i++)
	    address[i] = rand.nextInt(256);
	return address;
    }

    private static int getRandomPrefixLength() {
	return rand.nextInt(33);
    }

    private static IPv6 getRandomIPv6() {
	int[] address = new int[8];
	for (int i = 0; i < address.length; i++)
	    address[i] = rand.nextInt(1 << 16);
	int prefixLength = rand.nextInt(128);
	return new IPv6(address, prefixLength);
    }

    private boolean childrenTestRecurtion(IPv4 ip) {
	if (ip == null)
	    return false;
	if (!ip.hasChildren())
	    return true;

	IPv4[] children = ip.getChildren();
	IPv4 childZero = children[0];
	IPv4 childOne = children[1];

	return childrenTestRecurtion(childZero) && childrenTestRecurtion(childOne);
    }

}
