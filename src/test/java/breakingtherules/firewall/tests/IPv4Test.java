package breakingtherules.firewall.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Random;

import org.junit.Test;

import breakingtherules.firewall.IPv4;

public class IPv4Test {

    private static final Random rand = new Random();

    /*--------------------Test Methods--------------------*/

    @Test
    public void ipConstructorFullyTest() {
	try {
	    int[] address = getRandomAddress();
	    new IPv4(address);

	} catch (IllegalArgumentException e) {
	    fail("Excetion should not created: " + e.getMessage());
	}

	try {
	    int[] address = null;
	    new IPv4(address);
	    fail("Allowed IPv4 creation will null address arg");

	} catch (IllegalArgumentException e) {
	    // Success
	}
    }

    @Test
    public void ipConstructorWillPrefixTest() {
	try {
	    int[] address = getRandomAddress();
	    int prefixLength = rand.nextInt(33);
	    new IPv4(address, prefixLength);

	} catch (IllegalArgumentException e) {
	    fail("Excetion should not created: " + e.getMessage());
	}

	try {
	    int[] address = null;
	    int prefixLength = rand.nextInt(33);
	    new IPv4(address, prefixLength);
	    fail("Allowed IPv4 creation will null address arg");

	} catch (IllegalArgumentException e) {
	    // Success
	}

	try {
	    int[] address = getRandomAddress();
	    int prefixLength = -1;
	    new IPv4(address, prefixLength);
	    fail("Allowed IPv4 creation with illegal prefix length arg");

	} catch (IllegalArgumentException e) {
	    // Success
	}

	try {
	    int[] address = getRandomAddress();
	    int prefixLength = 33;
	    new IPv4(address, prefixLength);
	    fail("Allowed IPv4 creation with illegal prefix length arg");

	} catch (IllegalArgumentException e) {
	    // Success
	}
    }

    @Test
    public void ipConstructorFromStringTest() {
	try {
	    String ipStr = "IPv4 215.255.0.46";
	    new IPv4(ipStr);

	} catch (IllegalArgumentException e) {
	    fail("Exception should not created: " + e.getMessage());
	}

	try {
	    String ipStr = "IPv5 215.255.0.46";
	    new IPv4(ipStr);
	    fail("Allowed IPv4 creation will unknow format (IPv5)");

	} catch (IllegalArgumentException e) {
	    // success
	}

	try {
	    String ipStr = "IPv4 255.0.46";
	    new IPv4(ipStr);
	    fail("Allowed IPv4 creation with illegal format (3 blocks)");

	} catch (IllegalArgumentException e) {
	    // success
	}

	try {
	    String ipStr = "IPv4 255.0.2.2.46";
	    new IPv4(ipStr);
	    fail("Allowed IPv4 creation with illegal format (5 blocks)");

	} catch (IllegalArgumentException e) {
	    // success
	}

	try {
	    String ipStr = "IPv4 255.300.4.46";
	    new IPv4(ipStr);
	    fail("Allowed IPv4 creation with illegal format (block over 255)");

	} catch (IllegalArgumentException e) {
	    // success
	}

	try {
	    String ipStr = "IPv4 255.-55.4.46";
	    new IPv4(ipStr);
	    fail("Allowed IPv4 creation with illegal format (block under 0)");

	} catch (IllegalArgumentException e) {
	    // success
	}

	try {
	    String ipStr = "IPv4 255..2.4.46";
	    new IPv4(ipStr);
	    fail("Allowed IPv4 creation with illegal format (two dots)");

	} catch (IllegalArgumentException e) {
	    // success
	}

	try {
	    String ipStr = "IPv4 255.2.4.46/-1";
	    new IPv4(ipStr);
	    fail("Allowed IPv4 creation with illegal format (prefix length < 0). ");

	} catch (IllegalArgumentException e) {
	    // success
	}

	try {
	    String ipStr = "IPv4 255.2.4.46/33";
	    new IPv4(ipStr);
	    fail("Allowed IPv4 creation with illegal format (prefix length > 32)");

	} catch (IllegalArgumentException e) {
	    // success
	}

	try {
	    String ipStr = "IPv4 255.2.4.46/1 5";
	    new IPv4(ipStr);
	    fail("Allowed IPv4 creation with illegal format (extra numbers)");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void IPv4GetAddressTest() {
	int[] address = getRandomAddress();
	IPv4 ip;

	ip = new IPv4(address);
	assertEquals(address, ip.getAddress());

	ip = new IPv4(address, getRandomPrefixLength());
	assertEquals(address, ip.getAddress());
    }

    @Test
    public void IPv4GetPrefixLengthTest() {
	int prefixLength = getRandomPrefixLength();
	IPv4 ip;

	ip = new IPv4(getRandomAddress());
	assertEquals(32, ip.getConstPrefixLength());

	ip = new IPv4(getRandomAddress(), prefixLength);
	assertEquals(prefixLength, ip.getConstPrefixLength());
    }

    @Test
    public void IPv4ParentTest() {
	IPv4 ip = new IPv4(getRandomAddress());

	while (ip.hasParent()) {
	    ip = ip.getParent();
	    assertNotNull(ip);
	}
    }

    @Test
    public void IPv4ChildrenTest() {
	IPv4 ip = new IPv4(getRandomAddress(), rand.nextInt(11) + 22);
	assertTrue(childrenTestRecurtion(ip));
    }

    @Test
    public void IPv4EqualsTest() {
	int[] address = getRandomAddress();
	int prefix = rand.nextInt(33);
	IPv4 ip1, ip2;

	ip1 = new IPv4(address);
	ip2 = new IPv4(address);
	assertTrue(ip1.equals(ip2));
	assertTrue(ip2.equals(ip1));

	ip1 = new IPv4(address);
	ip2 = new IPv4(address, 32);
	assertTrue(ip1.equals(ip2));
	assertTrue(ip2.equals(ip1));

	ip1 = new IPv4(address, prefix);
	ip2 = new IPv4(address, prefix);
	assertTrue(ip1.equals(ip2));
	assertTrue(ip2.equals(ip1));

	ip1 = new IPv4(address, prefix);
	ip2 = new IPv4(address, prefix == 32 ? prefix - 1 : prefix + 1);
	assertFalse(ip1.equals(ip2));
	assertFalse(ip2.equals(ip1));
    }

    @Test
    public void IPv4ContainsTest() {
	IPv4 ip1, ip2;

	ip1 = new IPv4(new int[] { 0, 0, 0, 0 }, 0);
	ip2 = new IPv4(new int[] { 0, 0, 0, 0 }, 0);
	assertTrue(ip1.contain(ip2));
	assertTrue(ip2.contain(ip1));

	ip1 = new IPv4(new int[] { 0, 0, 0, 0 }, 0);
	ip2 = new IPv4(getRandomAddress());
	assertTrue(ip1.contain(ip2));
	assertFalse(ip2.contain(ip1));

	ip1 = new IPv4(new int[] { 145, 0, 0, 0 }, 8);
	ip2 = new IPv4(new int[] { 145, 55, 0, 0 });
	assertTrue(ip1.contain(ip2));
	assertFalse(ip2.contain(ip1));
	ip2 = new IPv4(new int[] { 145, 0, 48, 0 });
	assertTrue(ip1.contain(ip2));
	assertFalse(ip2.contain(ip1));
	ip2 = new IPv4(new int[] { 145, 255, 255, 255 });
	assertTrue(ip1.contain(ip2));
	assertFalse(ip2.contain(ip1));
	ip2 = new IPv4(new int[] { 146, 0, 0, 0 });
	assertFalse(ip1.contain(ip2));
	assertFalse(ip2.contain(ip1));

	ip1 = new IPv4(new int[] { 16, 216, 0, 0 }, 15);
	ip2 = new IPv4(new int[] { 16, 217, 11, 7 });
	assertTrue(ip1.contain(ip2));
	assertFalse(ip2.contain(ip1));
	ip2 = new IPv4(new int[] { 16, 216, 48, 0 });
	assertTrue(ip1.contain(ip2));
	assertFalse(ip2.contain(ip1));
	ip2 = new IPv4(new int[] { 16, 216, 45, 77 });
	assertTrue(ip1.contain(ip2));
	assertFalse(ip2.contain(ip1));
	ip2 = new IPv4(new int[] { 17, 216, 14, 42 });
	assertFalse(ip1.contain(ip2));
	assertFalse(ip2.contain(ip1));
	ip2 = new IPv4(new int[] { 16, 218, 36, 38 });
	assertFalse(ip1.contain(ip2));
	assertFalse(ip2.contain(ip1));

	ip1 = new IPv4(new int[] { 0, 160, 40, 0 }, 21);
	ip2 = new IPv4(new int[] { 0, 160, 47, 7 });
	assertTrue(ip1.contain(ip2));
	assertFalse(ip2.contain(ip1));
	ip2 = new IPv4(new int[] { 0, 160, 41, 0 });
	assertTrue(ip1.contain(ip2));
	assertFalse(ip2.contain(ip1));
	ip2 = new IPv4(new int[] { 0, 160, 40, 255 });
	assertTrue(ip1.contain(ip2));
	assertFalse(ip2.contain(ip1));
	ip2 = new IPv4(new int[] { 0, 160, 96, 0 });
	assertFalse(ip1.contain(ip2));
	assertFalse(ip2.contain(ip1));
	ip2 = new IPv4(new int[] { 0, 160, 7, 44 });
	assertFalse(ip1.contain(ip2));
	assertFalse(ip2.contain(ip1));

	ip1 = new IPv4(new int[] { 0, 160, 40, 0 });
	ip2 = new IPv4(new int[] { 0, 160, 40, 0 });
	assertTrue(ip1.contain(ip2));
	assertTrue(ip2.contain(ip1));
	ip2 = new IPv4(getRandomAddress());
	if (ip1.equals(ip2)) {
	    System.out.print("*********************************\n" + "There is no f***ing way!\n"
		    + "*********************************\n");
	} else {
	    assertFalse(ip1.contain(ip2));
	    assertFalse(ip2.contain(ip1));
	}

	ip1 = new IPv4(new int[] { 41, 99, 243, 160 }, 27);
	ip2 = new IPv4(new int[] { 41, 99, 243, 160 }, 32);
	assertTrue(ip1.contain(ip2));
	assertFalse(ip2.contain(ip1));
	ip2 = new IPv4(new int[] { 41, 99, 243, 160 }, 27);
	assertTrue(ip1.contain(ip2));
	assertTrue(ip2.contain(ip1));
	ip2 = new IPv4(new int[] { 41, 99, 243, 168 }, 29);
	assertTrue(ip1.contain(ip2));
	assertFalse(ip2.contain(ip1));
	ip2 = new IPv4(new int[] { 41, 99, 243, 176 }, 30);
	assertTrue(ip1.contain(ip2));
	assertFalse(ip2.contain(ip1));
	ip2 = new IPv4(new int[] { 41, 99, 243, 224 }, 27);
	assertFalse(ip1.contain(ip2));
	assertFalse(ip2.contain(ip1));
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
