package breakingtherules.tests.firewall;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

import breakingtherules.firewall.IPv4;
import breakingtherules.firewall.IPv6;
import breakingtherules.tests.TestBase;

public class IPv6Test extends TestBase {

    private static final Random rand = new Random();

    @Test
    public void constructorTestBasic() {
	System.out.println("# IPv6Test constructorTestBasic");
	int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	IPv6.create(address);
    }

    @Test(expected = NullPointerException.class)
    public void constructorTestBasicWillNullAdressTest() {
	System.out.println("# IPv6Test constructorTestBasicWillNullAdressTest");
	int[] address = null;
	IPv6.create(address);
    }

    @Test
    public void constructorTestWithPrefixLength() {
	System.out.println("# IPv6Test constructorTestWithPrefixLength");
	int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	int prefixLength = FirewallTestsUtility.getRandomPrefixLengthIPv6();
	IPv6.create(address, prefixLength);
    }

    @Test(expected = NullPointerException.class)
    public void constructorTestWithPrefixLengthNullAdress() {
	System.out.println("# IPv6Test constructorTestWithPrefixLengthNullAdress");
	int[] address = null;
	int prefixLength = FirewallTestsUtility.getRandomPrefixLengthIPv6();
	IPv6.create(address, prefixLength);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestWithNegativePrefixLength() {
	System.out.println("# IPv6Test constructorTestWithNegativePrefixLength");
	int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	int prefixLength = -1;
	IPv6.create(address, prefixLength);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestWithPrefixLengthOverMaxLength() {
	System.out.println("# IPv6Test constructorTestWithPrefixLengthOverMaxLength");
	int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	int prefixLength = 129;
	IPv6.create(address, prefixLength);

    }

    @Test
    public void constructorFromStringTest() {
	System.out.println("# IPv6Test constructorFromStringTest");
	String ipStr = "215:255:457:4966:0:65535:78:1257";
	IPv6 ip = IPv6.create(ipStr);
	assertEquals(ipStr, ip.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTest7Blocks() {
	System.out.println("# IPv6Test constructorFromStringTest7Blocks");
	String ipStr = "255:0:46:4784:48:74:89";
	IPv6.create(ipStr);

    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTest9Blocks() {
	System.out.println("# IPv6Test constructorFromStringTest9Blocks");
	String ipStr = "255:0:2:46:47863:32146:879:11112:30";
	IPv6.create(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestBlockOver65535() {
	System.out.println("# IPv6Test constructorFromStringTestBlockOver65535");
	String ipStr = "255:65536:4:46:801:24020:4852:31";
	IPv6.create(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestBlockUnder0() {
	System.out.println("# IPv6Test constructorFromStringTestBlockUnder0");
	String ipStr = "255:-55:4:46:44:879:326:15";
	IPv6.create(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestDoubleDot() {
	System.out.println("# IPv6Test constructorFromStringTestDoubleDot");
	String ipStr = "255::2:4:46:1:1:1:1";
	IPv6.create(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestNegativePrefixLength() {
	System.out.println("# IPv6Test constructorFromStringTestNegativePrefixLength");
	String ipStr = "255:2:4:46:4:5:6:1/-1";
	IPv6.create(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestPrefixLengthOver32() {
	System.out.println("# IPv6Test constructorFromStringTestPrefixLengthOver32");
	String ipStr = "255:2:549:785:324:7841:4:46/129";
	IPv6.create(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestExtraNumbers() {
	System.out.println("# IPv6Test constructorFromStringTestExtraNumbers");
	String ipStr = "255:2:4:46:14:48:79:13245/1 5";
	IPv6.create(ipStr);
    }

    @Test
    public void getAddressTestWithBasicConstructor() {
	System.out.println("# IPv6Test getAddressTestWithBasicConstructor");
	int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	IPv6 ip = IPv6.create(address);
	assertEquals(address, ip.getAddress());
    }

    @Test
    public void getAddressTestConstructorWithPrefixLength() {
	System.out.println("# IPv6Test getAddressTestConstructorWithPrefixLength");

	int[] address = new int[] { 0, 0, 0, 0b0010110101111011, 0b1100100001010111, 0, 0, 0 };
	int[][] expected = new int[33][];
	expected[32] = new int[] { 0, 0, 0, 0b0010110101111011, 0b1100100001010111, 0, 0, 0 };
	expected[31] = new int[] { 0, 0, 0, 0b0010110101111011, 0b1100100001010110, 0, 0, 0 };
	expected[30] = new int[] { 0, 0, 0, 0b0010110101111011, 0b1100100001010100, 0, 0, 0 };
	expected[29] = new int[] { 0, 0, 0, 0b0010110101111011, 0b1100100001010000, 0, 0, 0 };
	expected[28] = new int[] { 0, 0, 0, 0b0010110101111011, 0b1100100001010000, 0, 0, 0 };
	expected[27] = new int[] { 0, 0, 0, 0b0010110101111011, 0b1100100001000000, 0, 0, 0 };
	expected[26] = new int[] { 0, 0, 0, 0b0010110101111011, 0b1100100001000000, 0, 0, 0 };
	expected[25] = new int[] { 0, 0, 0, 0b0010110101111011, 0b1100100000000000, 0, 0, 0 };
	expected[24] = new int[] { 0, 0, 0, 0b0010110101111011, 0b1100100000000000, 0, 0, 0 };
	expected[23] = new int[] { 0, 0, 0, 0b0010110101111011, 0b1100100000000000, 0, 0, 0 };
	expected[22] = new int[] { 0, 0, 0, 0b0010110101111011, 0b1100100000000000, 0, 0, 0 };
	expected[21] = new int[] { 0, 0, 0, 0b0010110101111011, 0b1100100000000000, 0, 0, 0 };
	expected[20] = new int[] { 0, 0, 0, 0b0010110101111011, 0b1100000000000000, 0, 0, 0 };
	expected[19] = new int[] { 0, 0, 0, 0b0010110101111011, 0b1100000000000000, 0, 0, 0 };
	expected[18] = new int[] { 0, 0, 0, 0b0010110101111011, 0b1100000000000000, 0, 0, 0 };
	expected[17] = new int[] { 0, 0, 0, 0b0010110101111011, 0b1000000000000000, 0, 0, 0 };
	expected[16] = new int[] { 0, 0, 0, 0b0010110101111011, 0b0000000000000000, 0, 0, 0 };
	expected[15] = new int[] { 0, 0, 0, 0b0010110101111010, 0b0000000000000000, 0, 0, 0 };
	expected[14] = new int[] { 0, 0, 0, 0b0010110101111000, 0b0000000000000000, 0, 0, 0 };
	expected[13] = new int[] { 0, 0, 0, 0b0010110101111000, 0b0000000000000000, 0, 0, 0 };
	expected[12] = new int[] { 0, 0, 0, 0b0010110101110000, 0b0000000000000000, 0, 0, 0 };
	expected[11] = new int[] { 0, 0, 0, 0b0010110101100000, 0b0000000000000000, 0, 0, 0 };
	expected[10] = new int[] { 0, 0, 0, 0b0010110101000000, 0b0000000000000000, 0, 0, 0 };
	expected[9] = new int[] { 0, 0, 0, 0b0010110100000000, 0b0000000000000000, 0, 0, 0 };
	expected[8] = new int[] { 0, 0, 0, 0b0010110100000000, 0b0000000000000000, 0, 0, 0 };
	expected[7] = new int[] { 0, 0, 0, 0b0010110000000000, 0b0000000000000000, 0, 0, 0 };
	expected[6] = new int[] { 0, 0, 0, 0b0010110000000000, 0b0000000000000000, 0, 0, 0 };
	expected[5] = new int[] { 0, 0, 0, 0b0010100000000000, 0b0000000000000000, 0, 0, 0 };
	expected[4] = new int[] { 0, 0, 0, 0b0010000000000000, 0b0000000000000000, 0, 0, 0 };
	expected[3] = new int[] { 0, 0, 0, 0b0010000000000000, 0b0000000000000000, 0, 0, 0 };
	expected[2] = new int[] { 0, 0, 0, 0b0000000000000000, 0b0000000000000000, 0, 0, 0 };
	expected[1] = new int[] { 0, 0, 0, 0b0000000000000000, 0b0000000000000000, 0, 0, 0 };
	expected[0] = new int[] { 0, 0, 0, 0b0000000000000000, 0b0000000000000000, 0, 0, 0 };

	IPv6 ip = IPv6.create(address);
	for (int prefixLength = 128; prefixLength-- > 80;) {
	    assertTrue("Prefix length " + prefixLength, ip.hasParent());
	    IPv6 parent = ip.getParent();
	    assertNotNull("Prefix length " + prefixLength, parent);
	    // expected address is always
	    assertEquals("Prefix length " + prefixLength, prefixLength, parent.getPrefixLength());
	    ip = parent;
	}
	for (int prefixLength = 80; prefixLength-- > 48;) {
	    assertTrue("Prefix length " + prefixLength, ip.hasParent());
	    IPv6 parent = ip.getParent();
	    assertNotNull("Prefix length " + prefixLength, parent);
	    assertEquals("Prefix length " + prefixLength, prefixLength, parent.getPrefixLength());
	    assertEquals("Prefix length " + prefixLength, expected[prefixLength - 48], parent.getAddress());
	    ip = parent;
	}
	for (int prefixLength = 48; prefixLength-- > 0;) {
	    assertTrue("Prefix length " + prefixLength, ip.hasParent());
	    IPv6 parent = ip.getParent();
	    assertNotNull("Prefix length " + prefixLength, parent);
	    assertEquals("Prefix length " + prefixLength, prefixLength, parent.getPrefixLength());
	    // expected address is always expected[0] because all 48 highest
	    // bits are 0
	    assertEquals("Prefix length " + prefixLength, expected[0], parent.getAddress());
	    ip = parent;
	}
	assertFalse("Prefix length 0", ip.hasParent());
    }

    @Test
    public void getPrefixLengthTestBasicConstructor() {
	System.out.println("# IPv6Test getPrefixLengthTestBasicConstructor");
	IPv6 ip = IPv6.create(FirewallTestsUtility.getRandomAddressIPv6());
	assertEquals(128, ip.getPrefixLength());
    }

    @Test
    public void getPrefixLengthTestConstructorWithPrefixLength() {
	System.out.println("# IPv6Test getPrefixLengthTestConstructorWithPrefixLength");
	int prefixLength = FirewallTestsUtility.getRandomPrefixLengthIPv6();
	IPv6 ip = IPv6.create(FirewallTestsUtility.getRandomAddressIPv6(), prefixLength);
	assertEquals(prefixLength, ip.getPrefixLength());
    }

    @Test(expected = IllegalStateException.class)
    public void getParentTest() {
	System.out.println("# IPv6Test getParentTest");
	IPv6 ip = IPv6.create(FirewallTestsUtility.getRandomAddressIPv6());

	for (int expectedLength = 128; expectedLength > 0; expectedLength--) {
	    assertTrue(ip.hasParent());
	    assertNotNull(ip.getParent());
	    ip = ip.getParent();
	}

	assertFalse(ip.hasParent());
	ip.getParent();
    }

    @Test
    public void getChildrenTest() {
	System.out.println("# IPv6Test getChildrenTest");
	IPv6 ip = IPv6.create(FirewallTestsUtility.getRandomAddressIPv6(), rand.nextInt(28) + 100);
	assertEquals(ip.hasChildren(), ip.getPrefixLength() != IPv6.MAX_LENGTH);
    }

    @Test
    public void equalsTestItselfBasicConstructor() {
	System.out.println("# IPv6Test equalsTestItselfBasicConstructor");
	int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	IPv6 ip1 = IPv6.create(address);
	IPv6 ip2 = IPv6.create(address);
	assertTrue(ip1.equals(ip2));
	assertTrue(ip2.equals(ip1));
    }

    @Test
    public void equalsTestItselfOneIpWithConstructorWithPrefixLength() {
	System.out.println("# IPv6Test equalsTestItselfOneIpWithConstructorWithPrefixLength");
	int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	IPv6 ip1 = IPv6.create(address);
	IPv6 ip2 = IPv6.create(address, 128);
	assertTrue(ip1.equals(ip2));
	assertTrue(ip2.equals(ip1));
    }

    @Test
    public void equalsTestItselfTwoIpwithConstructorWithPrefixLength() {
	System.out.println("# IPv6Test equalsTestItselfTwoIpwithConstructorWithPrefixLength");
	int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	int prefix = FirewallTestsUtility.getRandomPrefixLengthIPv6();
	IPv6 ip1 = IPv6.create(address, prefix);
	IPv6 ip2 = IPv6.create(address, prefix);
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

	IPv6 ip1 = IPv6.create(address, prefix1);
	IPv6 ip2 = IPv6.create(address, prefix2);
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
	IPv6 ip1 = IPv6.create(new int[] { 0, 160, 40, 0, 10, 0, 540, 0 });
	IPv6 ip2 = IPv6.create(new int[] { 0, 160, 40, 0, 10, 0, 540, 0 });
	assertTrue(ip1.contains(ip2));
	assertTrue(ip2.contains(ip1));
    }

    @Test
    public void containsTestZeroPrefixLengthContainsAll() {
	System.out.println("# IPv6Test containsTestZeroPrefixLengthContainsAll");
	IPv6 ip1 = IPv6.create(new int[] { 0, 0, 0, 0, 0, 0, 0, 0 }, 0);
	IPv6 ip2 = IPv6.create(FirewallTestsUtility.getRandomAddressIPv6());
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    @Test
    public void containsTestPrefixLength16() {
	System.out.println("# IPv6Test containsTestPrefixLength16");
	IPv6 ip1 = IPv6.create(new int[] { 145, 0, 0, 0, 0, 0, 0, 0 }, 16);
	IPv6 ip2 = IPv6.create(new int[] { 145, 55, 0, 0, 0, 0, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv6.create(new int[] { 145, 0, 48, 0, 0, 0, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv6.create(new int[] { 145, 255, 255, 255, 0, 0, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv6.create(new int[] { 146, 0, 0, 0, 0, 0, 0, 0 });
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    @Test
    public void containsTestPrefixLength31() {
	System.out.println("# IPv6Test containsTestPrefixLength31");
	IPv6 ip1 = IPv6.create(new int[] { 16, 216, 0, 0, 0, 0, 0, 0 }, 31);
	IPv6 ip2 = IPv6.create(new int[] { 16, 217, 11, 7, 0, 0, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv6.create(new int[] { 16, 216, 48, 0, 0, 0, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv6.create(new int[] { 16, 216, 45, 77, 0, 0, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv6.create(new int[] { 17, 216, 14, 42, 0, 0, 0, 0 });
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv6.create(new int[] { 16, 218, 36, 38, 0, 0, 0, 0 });
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    @Test
    public void containsTestPrefixLength45() {
	System.out.println("# IPv6Test containsTestPrefixLength45");
	IPv6 ip1 = IPv6.create(new int[] { 0, 160, 40, 0, 0, 0, 0, 0 }, 45);
	IPv6 ip2 = IPv6.create(new int[] { 0, 160, 47, 7, 0, 0, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv6.create(new int[] { 0, 160, 41, 0, 0, 0, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv6.create(new int[] { 0, 160, 40, 255, 0, 0, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv6.create(new int[] { 0, 160, 96, 0, 0, 0, 0, 0 });
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv6.create(new int[] { 0, 160, 7, 44, 0, 0, 0, 0 });
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    @Test
    public void containsTestPrefixLength59() {
	System.out.println("# IPv6Test containsTestPrefixLength59");
	IPv6 ip1 = IPv6.create(new int[] { 41, 99, 243, 160, 0, 0, 0, 0 }, 59);
	IPv6 ip2 = IPv6.create(new int[] { 41, 99, 243, 160, 0, 0, 0, 0 }, 64);
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv6.create(new int[] { 41, 99, 243, 160, 0, 0, 0, 0 }, 59);
	assertTrue(ip1.contains(ip2));
	assertTrue(ip2.contains(ip1));
	ip2 = IPv6.create(new int[] { 41, 99, 243, 168, 0, 0, 0, 0 }, 61);
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv6.create(new int[] { 41, 99, 243, 176, 0, 0, 0, 0 }, 62);
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv6.create(new int[] { 41, 99, 243, 224, 0, 0, 0, 0 }, 59);
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    @Test
    public void containsTestFullIpNotContainsOthers() {
	System.out.println("# IPv6Test containsTestFullIpNotContainsOthers");
	IPv6 ip1 = IPv6.create(new int[] { 0, 160, 40, 0, 47, 8888, 78, 0 });
	IPv6 ip2 = IPv6.create(FirewallTestsUtility.getRandomAddressIPv6());
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
	IPv6 ip6 = IPv6.create(FirewallTestsUtility.getRandomAddressIPv6(),
		FirewallTestsUtility.getRandomPrefixLengthIPv6());
	IPv4 ip4 = FirewallTestsUtility.getRandomIPv4();
	assertFalse(ip6.contains(ip4));
    }

}
