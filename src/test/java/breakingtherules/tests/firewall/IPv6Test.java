package breakingtherules.tests.firewall;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

import breakingtherules.firewall.IPv4;
import breakingtherules.firewall.IPv6;

public class IPv6Test {

    private static final Random rand = new Random();

    @Test
    public void constructorTestBasic() {
	System.out.println("# IPv6Test constructorTestBasic");
	int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	new IPv6(address);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestBasicWillNullAdressTest() {
	System.out.println("# IPv6Test constructorTestBasicWillNullAdressTest");
	int[] address = null;
	new IPv6(address);
    }

    @Test
    public void constructorTestWithPrefixLength() {
	System.out.println("# IPv6Test constructorTestWithPrefixLength");
	int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	int prefixLength = FirewallTestsUtility.getRandomPrefixLengthIPv6();
	new IPv6(address, prefixLength);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestWithPrefixLengthNullAdress() {
	System.out.println("# IPv6Test constructorTestWithPrefixLengthNullAdress");
	int[] address = null;
	int prefixLength = FirewallTestsUtility.getRandomPrefixLengthIPv6();
	new IPv6(address, prefixLength);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestWithNegativePrefixLength() {
	System.out.println("# IPv6Test constructorTestWithNegativePrefixLength");
	int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	int prefixLength = -1;
	new IPv6(address, prefixLength);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestWithPrefixLengthOverMaxLength() {
	System.out.println("# IPv6Test constructorTestWithPrefixLengthOverMaxLength");
	int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	int prefixLength = 129;
	new IPv6(address, prefixLength);

    }

    @Test
    public void constructorFromStringTest() {
	System.out.println("# IPv6Test constructorFromStringTest");
	String ipStr = "215:255:457:4966:0:65535:78:1257";
	new IPv6(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTest7Blocks() {
	System.out.println("# IPv6Test constructorFromStringTest7Blocks");
	String ipStr = "255:0:46:4784:48:74:89";
	new IPv6(ipStr);

    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTest9Blocks() {
	System.out.println("# IPv6Test constructorFromStringTest9Blocks");
	String ipStr = "255:0:2:46:47863:32146:879:11112:30";
	new IPv6(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestBlockOver65535() {
	System.out.println("# IPv6Test constructorFromStringTestBlockOver65535");
	String ipStr = "255:65536:4:46:801:24020:4852:31";
	new IPv6(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestBlockUnder0() {
	System.out.println("# IPv6Test constructorFromStringTestBlockUnder0");
	String ipStr = "255:-55:4:46:44:879:326:15";
	new IPv6(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestDoubleDot() {
	System.out.println("# IPv6Test constructorFromStringTestDoubleDot");
	String ipStr = "255::2:4:46:1:1:1:1";
	new IPv6(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestNegativePrefixLength() {
	System.out.println("# IPv6Test constructorFromStringTestNegativePrefixLength");
	String ipStr = "255:2:4:46:4:5:6:1/-1";
	new IPv6(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestPrefixLengthOver32() {
	System.out.println("# IPv6Test constructorFromStringTestPrefixLengthOver32");
	String ipStr = "255:2:549:785:324:7841:4:46/129";
	new IPv6(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestExtraNumbers() {
	System.out.println("# IPv6Test constructorFromStringTestExtraNumbers");
	String ipStr = "255:2:4:46:14:48:79:13245/1 5";
	new IPv6(ipStr);
    }

    @Test
    public void getAddressTestWithBasicConstructor() {
	System.out.println("# IPv6Test getAddressTestWithBasicConstructor");
	int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	IPv6 ip = new IPv6(address);
	assertEquals(address, ip.getAddress());
    }

    @Test
    public void getAddressTestConstructorWithPrefixLength() {
	System.out.println("# IPv6Test getAddressTestConstructorWithPrefixLength");
	int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	IPv6 ip = new IPv6(address, FirewallTestsUtility.getRandomPrefixLengthIPv6());
	assertEquals(address, ip.getAddress());
    }

    @Test
    public void getPrefixLengthTestBasicConstructor() {
	System.out.println("# IPv6Test getPrefixLengthTestBasicConstructor");
	IPv6 ip = new IPv6(FirewallTestsUtility.getRandomAddressIPv6());
	assertEquals(128, ip.getConstPrefixLength());
    }

    @Test
    public void getPrefixLengthTestConstructorWithPrefixLength() {
	System.out.println("# IPv6Test getPrefixLengthTestConstructorWithPrefixLength");
	int prefixLength = FirewallTestsUtility.getRandomPrefixLengthIPv6();
	IPv6 ip = new IPv6(FirewallTestsUtility.getRandomAddressIPv6(), prefixLength);
	assertEquals(prefixLength, ip.getConstPrefixLength());
    }

    @Test
    public void getParentTest() {
	System.out.println("# IPv6Test getParentTest");
	IPv6 ip = new IPv6(FirewallTestsUtility.getRandomAddressIPv6());

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
	System.out.println("# IPv6Test getChildrenTest");
	IPv6 ip = new IPv6(FirewallTestsUtility.getRandomAddressIPv6(), rand.nextInt(28) + 101);
	assertEquals(ip.hasChildren(), ip.getChildren()[0] != null);
	assertEquals(ip.hasChildren(), ip.getChildren()[1] != null);
    }

    @Test
    public void equalsTestItselfBasicConstructor() {
	System.out.println("# IPv6Test equalsTestItselfBasicConstructor");
	int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	IPv6 ip1 = new IPv6(address);
	IPv6 ip2 = new IPv6(address);
	assertTrue(ip1.equals(ip2));
	assertTrue(ip2.equals(ip1));
    }

    @Test
    public void equalsTestItselfOneIpWithConstructorWithPrefixLength() {
	System.out.println("# IPv6Test equalsTestItselfOneIpWithConstructorWithPrefixLength");
	int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	IPv6 ip1 = new IPv6(address);
	IPv6 ip2 = new IPv6(address, 128);
	assertTrue(ip1.equals(ip2));
	assertTrue(ip2.equals(ip1));
    }

    @Test
    public void equalsTestItselfTwoIpwithConstructorWithPrefixLength() {
	System.out.println("# IPv6Test equalsTestItselfTwoIpwithConstructorWithPrefixLength");
	int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	int prefix = FirewallTestsUtility.getRandomPrefixLengthIPv6();
	IPv6 ip1 = new IPv6(address, prefix);
	IPv6 ip2 = new IPv6(address, prefix);
	assertTrue(ip1.equals(ip2));
	assertTrue(ip2.equals(ip1));
    }

    @Test
    public void equalsTestNotEqualsItselfTwoDifferentPrefixLength() {
	System.out.println("# IPv6Test equalsTestNotEqualsItselfTwoDifferentPrefixLength");
	int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	int prefix1 = FirewallTestsUtility.getRandomPrefixLengthIPv6();
	int prefix2;
	do {
	    prefix2 = FirewallTestsUtility.getRandomPrefixLengthIPv6();
	} while (prefix1 == prefix2);

	IPv6 ip1 = new IPv6(address, prefix1);
	IPv6 ip2 = new IPv6(address, prefix2);
	assertNotEquals(ip1, ip2);
    }

    @Test
    public void containsTestContainsItself() {
	System.out.println("# IPv6Test containsTestContainsItself");
	IPv6 ip = FirewallTestsUtility.getRandomIPv6();
	assertTrue(ip.contains(ip));
    }

    @Test
    public void containsTestContainsItselfNoPrefixLength() {
	System.out.println("# IPv6Test containsTestContainsItselfNoPrefixLength");
	IPv6 ip1 = new IPv6(new int[] { 0, 160, 40, 0, 10, 0, 540, 0 });
	IPv6 ip2 = new IPv6(new int[] { 0, 160, 40, 0, 10, 0, 540, 0 });
	assertTrue(ip1.contains(ip2));
	assertTrue(ip2.contains(ip1));
    }

    @Test
    public void containsTestZeroPrefixLengthContainsAll() {
	System.out.println("# IPv6Test containsTestZeroPrefixLengthContainsAll");
	IPv6 ip1 = new IPv6(new int[] { 0, 0, 0, 0, 0, 0, 0, 0 }, 0);
	IPv6 ip2 = new IPv6(FirewallTestsUtility.getRandomAddressIPv6());
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    @Test
    public void containsTestPrefixLength16() {
	System.out.println("# IPv6Test containsTestPrefixLength16");
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
	System.out.println("# IPv6Test containsTestPrefixLength31");
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
	System.out.println("# IPv6Test containsTestPrefixLength45");
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
	System.out.println("# IPv6Test containsTestPrefixLength59");
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
	System.out.println("# IPv6Test containsTestFullIpNotContainsOthers");
	IPv6 ip1 = new IPv6(new int[] { 0, 160, 40, 0, 47, 8888, 78, 0 });
	IPv6 ip2 = new IPv6(FirewallTestsUtility.getRandomAddressIPv6());
	if (ip1.equals(ip2)) {
	    System.out.print(
		    "*********************************\nThere is no f***ing way!\n*********************************\n");
	    return;
	}

	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    @Test
    public void containsTestNotContainsIPv4() {
	System.out.println("# IPv6Test containsTestNotContainsIPv4");
	IPv6 ip6 = new IPv6(FirewallTestsUtility.getRandomAddressIPv6(),
		FirewallTestsUtility.getRandomPrefixLengthIPv6());
	IPv4 ip4 = FirewallTestsUtility.getRandomIPv4();
	assertFalse(ip6.contains(ip4));
    }

    @Test
    public void cloneTest() {
	System.out.println("# IPv6Test cloneTest");
	int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	IPv6 ip = new IPv6(address);
	IPv6 ipClone = (IPv6) ip.clone();
	assertFalse(ip == ipClone);
	assertEquals(ip, ipClone);
    }

}
