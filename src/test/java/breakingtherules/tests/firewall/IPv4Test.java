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

    @Test
    public void constructorTestBasic() {
	int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	new IPv4(address);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestBasicWillNullAdressTest() {
	int[] address = null;
	new IPv4(address);
	fail("Allowed IPv4 creation will null address arg");
    }

    @Test
    public void constructorTestWithPrefixLength() {
	int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	int prefixLength = FirewallTestsUtility.getRandomPrefixLengthIPv4();
	new IPv4(address, prefixLength);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestWithPrefixLengthNullAdress() {
	int[] address = null;
	int prefixLength = FirewallTestsUtility.getRandomPrefixLengthIPv4();
	new IPv4(address, prefixLength);
	fail("Allowed IPv4 creation will null address arg");
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestWithNegativePrefixLength() {
	int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	int prefixLength = -1;
	new IPv4(address, prefixLength);
	fail("Allowed IPv4 creation will illegal prefix length arg");
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestWithPrefixLengthOverMaxLength() {
	int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	int prefixLength = 33;
	new IPv4(address, prefixLength);
	fail("Allowed IPv4 creation will illegal prefix length arg");
    }

    @Test
    public void constructorFromStringTest() {
	String ipStr = "215.255.0.46";
	new IPv4(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTest3Blocks() {
	String ipStr = "255.0.46";
	new IPv4(ipStr);
	fail("Allowed IPv4 creation will illegal format (3 blocks)");
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTest5Blocks() {
	String ipStr = "255.0.2.2.46";
	new IPv4(ipStr);
	fail("Allowed IPv4 creation will illegal format (5 blocks)");
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestBlockOver255() {
	String ipStr = "255.300.4.46";
	new IPv4(ipStr);
	fail("Allowed IPv4 creation will illegal format (block over 255)");
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestBlockUnder0() {
	String ipStr = "255.-55.4.46";
	new IPv4(ipStr);
	fail("Allowed IPv4 creation will illegal format (block under 0)");
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestDoubleDot() {
	String ipStr = "255..2.4.46";
	new IPv4(ipStr);
	fail("Allowed IPv4 creation will illegal format (two dots)");
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestNegativePrefixLength() {
	String ipStr = "255.2.4.46/-1";
	new IPv4(ipStr);
	fail("Allowed IPv4 creation will illegal format (prefix length < 0). ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestPrefixLengthOver32() {
	String ipStr = "255.2.4.46/33";
	new IPv4(ipStr);
	fail("Allowed IPv4 creation will illegal format (prefix length > 32)");
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestExtraNumbers() {
	String ipStr = "255.2.4.46/1 5";
	new IPv4(ipStr);
	fail("Allowed IPv4 creation will illegal format (extra numbers)");
    }

    @Test
    public void getAddressTestWithBasicConstructor() {
	int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	IPv4 ip = new IPv4(address);
	assertEquals(address, ip.getAddress());
    }

    @Test
    public void getAddressTestConstructorWithPrefixLength() {
	int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	IPv4 ip = new IPv4(address, FirewallTestsUtility.getRandomPrefixLengthIPv4());
	assertEquals(address, ip.getAddress());
    }

    @Test
    public void getPrefixLengthTestBasicConstructor() {
	IPv4 ip = new IPv4(FirewallTestsUtility.getRandomAddressIPv4());
	assertEquals(32, ip.getConstPrefixLength());
    }

    @Test
    public void getPrefixLengthTestConstructorWithPrefixLength() {
	int prefixLength = FirewallTestsUtility.getRandomPrefixLengthIPv4();
	IPv4 ip = new IPv4(FirewallTestsUtility.getRandomAddressIPv4(), prefixLength);
	assertEquals(prefixLength, ip.getConstPrefixLength());
    }

    @Test
    public void getParentTest() {
	IPv4 ip = new IPv4(FirewallTestsUtility.getRandomAddressIPv4());

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
	IPv4 ip = new IPv4(FirewallTestsUtility.getRandomAddressIPv4(), rand.nextInt(11) + 22);
	assertEquals(ip.hasChildren(), ip.getChildren()[0] != null);
	assertEquals(ip.hasChildren(), ip.getChildren()[1] != null);
    }

    @Test
    public void equalsTestItselfBasicConstructor() {
	int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	IPv4 ip1 = new IPv4(address);
	IPv4 ip2 = new IPv4(address);
	assertTrue(ip1.equals(ip2));
	assertTrue(ip2.equals(ip1));
    }

    @Test
    public void equalsTestItselfOneIpWithConstructorWithPrefixLength() {
	int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	IPv4 ip1 = new IPv4(address);
	IPv4 ip2 = new IPv4(address, 32);
	assertTrue(ip1.equals(ip2));
	assertTrue(ip2.equals(ip1));
    }

    @Test
    public void equalsTestItselfTwoIpwithConstructorWithPrefixLength() {
	int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	int prefix = FirewallTestsUtility.getRandomPrefixLengthIPv4();
	IPv4 ip1 = new IPv4(address, prefix);
	IPv4 ip2 = new IPv4(address, prefix);
	assertTrue(ip1.equals(ip2));
	assertTrue(ip2.equals(ip1));
    }

    @Test
    public void equalsTestNotEqualsItselfTwoDifferentPrefixLength() {
	int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	int prefix1 = FirewallTestsUtility.getRandomPrefixLengthIPv4();
	int prefix2;
	do {
	    prefix2 = FirewallTestsUtility.getRandomPrefixLengthIPv4();
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
	int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	int prefixLength = FirewallTestsUtility.getRandomPrefixLengthIPv4();
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
	IPv4 ip2 = new IPv4(FirewallTestsUtility.getRandomAddressIPv4());
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
	IPv4 ip2 = new IPv4(FirewallTestsUtility.getRandomAddressIPv4());
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
	IPv4 ip4 = new IPv4(FirewallTestsUtility.getRandomAddressIPv4(), FirewallTestsUtility.getRandomPrefixLengthIPv4());
	IPv6 ip6 = FirewallTestsUtility.getRandomIPv6();
	assertFalse(ip4.contains(ip6));
    }

}
