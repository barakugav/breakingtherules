package breakingtherules.tests.firewall;

import static breakingtherules.tests.JUnitUtilities.deepAssertEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

import breakingtherules.firewall.IP;
import breakingtherules.firewall.IPv4;
import breakingtherules.firewall.IPv6;

public class IPv4Test {

    private static final Random rand = new Random();

    @Test
    public void constructorTestBasic() {
	System.out.println("# IPv4Test constructorTestBasic");
	int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	IPv4.create(address);
    }

    @Test(expected = NullPointerException.class)
    public void constructorTestBasicWillNullAdressTest() {
	System.out.println("# IPv4Test constructorTestBasicWillNullAdressTest");
	int[] address = null;
	IPv4.create(address);
    }

    @Test
    public void constructorTestWithPrefixLength() {
	System.out.println("# IPv4Test constructorTestWithPrefixLength");
	int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	int prefixLength = FirewallTestsUtility.getRandomPrefixLengthIPv4();
	IPv4.create(address, prefixLength);
    }

    @Test(expected = NullPointerException.class)
    public void constructorTestWithPrefixLengthNullAdress() {
	System.out.println("# IPv4Test constructorTestWithPrefixLengthNullAdress");
	int[] address = null;
	int prefixLength = FirewallTestsUtility.getRandomPrefixLengthIPv4();
	IPv4.create(address, prefixLength);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestWithNegativePrefixLength() {
	System.out.println("# IPv4Test constructorTestWithNegativePrefixLength");
	int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	int prefixLength = -1;
	IPv4.create(address, prefixLength);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestWithPrefixLengthOverMaxLength() {
	System.out.println("# IPv4Test constructorTestWithPrefixLengthOverMaxLength");
	int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	int prefixLength = 33;
	IPv4.create(address, prefixLength);
    }

    @Test
    public void constructorFromStringTest() {
	System.out.println("# IPv4Test constructorFromStringTest");
	String ipStr = "215.255.0.46";
	IPv4.create(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTest3Blocks() {
	System.out.println("# IPv4Test constructorFromStringTest3Blocks");
	String ipStr = "255.0.46";
	IPv4.create(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTest5Blocks() {
	System.out.println("# IPv4Test constructorFromStringTest5Blocks");
	String ipStr = "255.0.2.2.46";
	IPv4.create(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestBlockOver255() {
	System.out.println("# IPv4Test constructorFromStringTestBlockOver255");
	String ipStr = "255.300.4.46";
	IPv4.create(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestBlockUnder0() {
	System.out.println("# IPv4Test constructorFromStringTestBlockUnder0");
	String ipStr = "255.-55.4.46";
	IPv4.create(ipStr);
    }

    @Test
    public void constructorFromStringTestPrefixLength10() {
	System.out.println("# IPv4Test constructorFromStringTestPrefixLength10");
	int prefix = 10;
	String ipStr = "255.2.4.46/" + prefix;
	IP ip = IPv4.create(ipStr);
	assertEquals(prefix, ip.getConstPrefixLength());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestNegativePrefixLength() {
	System.out.println("# IPv4Test constructorFromStringTestNegativePrefixLength");
	String ipStr = "255.2.4.46/-1";
	IPv4.create(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestPrefixLengthOver32() {
	System.out.println("# IPv4Test constructorFromStringTestPrefixLengthOver32");
	String ipStr = "255.2.4.46/33";
	IPv4.create(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestExtraNumbers() {
	System.out.println("# IPv4Test constructorFromStringTestExtraNumbers");
	String ipStr = "255.2.4.46/1 5";
	IPv4.create(ipStr);
    }

    @Test
    public void getAddressTestWithBasicConstructor() {
	System.out.println("# IPv4Test getAddressTestWithBasicConstructor");
	int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	IPv4 ip = IPv4.create(address);
	deepAssertEquals(address, ip.getAddress());
    }

    @Test
    public void getAddressTestWithConstructorWithPrefixLength() {
	System.out.println("# IPv4Test getAddressTestWithConstructorWithPrefixLength");
	// [47, 123, 200, 87]
	int[] address = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01010111 };
	int[][] expected = new int[33][];
	expected[32] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01010111 };
	expected[31] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01010110 };
	expected[30] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01010100 };
	expected[29] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01010000 };
	expected[28] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01010000 };
	expected[27] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01000000 };
	expected[26] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01000000 };
	expected[25] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b00000000 };
	expected[24] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b00000000 };
	expected[23] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b00000000 };
	expected[22] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b00000000 };
	expected[21] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b00000000 };
	expected[20] = new int[] { 0b00101101, 0b01111011, 0b11000000, 0b00000000 };
	expected[19] = new int[] { 0b00101101, 0b01111011, 0b11000000, 0b00000000 };
	expected[18] = new int[] { 0b00101101, 0b01111011, 0b11000000, 0b00000000 };
	expected[17] = new int[] { 0b00101101, 0b01111011, 0b10000000, 0b00000000 };
	expected[16] = new int[] { 0b00101101, 0b01111011, 0b00000000, 0b00000000 };
	expected[15] = new int[] { 0b00101101, 0b01111010, 0b00000000, 0b00000000 };
	expected[14] = new int[] { 0b00101101, 0b01111000, 0b00000000, 0b00000000 };
	expected[13] = new int[] { 0b00101101, 0b01111000, 0b00000000, 0b00000000 };
	expected[12] = new int[] { 0b00101101, 0b01110000, 0b00000000, 0b00000000 };
	expected[11] = new int[] { 0b00101101, 0b01100000, 0b00000000, 0b00000000 };
	expected[10] = new int[] { 0b00101101, 0b01000000, 0b00000000, 0b00000000 };
	expected[9] = new int[] { 0b00101101, 0b00000000, 0b00000000, 0b00000000 };
	expected[8] = new int[] { 0b00101101, 0b00000000, 0b00000000, 0b00000000 };
	expected[7] = new int[] { 0b00101100, 0b00000000, 0b00000000, 0b00000000 };
	expected[6] = new int[] { 0b00101100, 0b00000000, 0b00000000, 0b00000000 };
	expected[5] = new int[] { 0b00101000, 0b00000000, 0b00000000, 0b00000000 };
	expected[4] = new int[] { 0b00100000, 0b00000000, 0b00000000, 0b00000000 };
	expected[3] = new int[] { 0b00100000, 0b00000000, 0b00000000, 0b00000000 };
	expected[2] = new int[] { 0b00000000, 0b00000000, 0b00000000, 0b00000000 };
	expected[1] = new int[] { 0b00000000, 0b00000000, 0b00000000, 0b00000000 };
	expected[0] = new int[] { 0b00000000, 0b00000000, 0b00000000, 0b00000000 };

	for (int prefixLength = 32; prefixLength >= 0; prefixLength--) {
	    IPv4 ip = IPv4.create(address, prefixLength);
	    deepAssertEquals("Prefix length " + prefixLength, expected[prefixLength], ip.getAddress());
	}
    }

    @Test
    public void getPrefixLengthTestBasicConstructor() {
	System.out.println("# IPv4Test getPrefixLengthTestBasicConstructor");
	IPv4 ip = IPv4.create(FirewallTestsUtility.getRandomAddressIPv4());
	assertEquals(32, ip.getConstPrefixLength());
    }

    @Test
    public void getPrefixLengthTestConstructorWithPrefixLength() {
	System.out.println("# IPv4Test getPrefixLengthTestConstructorWithPrefixLength");
	int prefixLength = FirewallTestsUtility.getRandomPrefixLengthIPv4();
	IPv4 ip = IPv4.create(FirewallTestsUtility.getRandomAddressIPv4(), prefixLength);
	assertEquals(prefixLength, ip.getConstPrefixLength());
    }

    @Test
    public void getParentTest() {
	System.out.println("# IPv4Test getParentTest");

	// [47, 123, 200, 87]
	int[] address = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01010111 };
	int[][] expected = new int[33][];
	expected[32] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01010111 };
	expected[31] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01010110 };
	expected[30] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01010100 };
	expected[29] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01010000 };
	expected[28] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01010000 };
	expected[27] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01000000 };
	expected[26] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01000000 };
	expected[25] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b00000000 };
	expected[24] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b00000000 };
	expected[23] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b00000000 };
	expected[22] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b00000000 };
	expected[21] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b00000000 };
	expected[20] = new int[] { 0b00101101, 0b01111011, 0b11000000, 0b00000000 };
	expected[19] = new int[] { 0b00101101, 0b01111011, 0b11000000, 0b00000000 };
	expected[18] = new int[] { 0b00101101, 0b01111011, 0b11000000, 0b00000000 };
	expected[17] = new int[] { 0b00101101, 0b01111011, 0b10000000, 0b00000000 };
	expected[16] = new int[] { 0b00101101, 0b01111011, 0b00000000, 0b00000000 };
	expected[15] = new int[] { 0b00101101, 0b01111010, 0b00000000, 0b00000000 };
	expected[14] = new int[] { 0b00101101, 0b01111000, 0b00000000, 0b00000000 };
	expected[13] = new int[] { 0b00101101, 0b01111000, 0b00000000, 0b00000000 };
	expected[12] = new int[] { 0b00101101, 0b01110000, 0b00000000, 0b00000000 };
	expected[11] = new int[] { 0b00101101, 0b01100000, 0b00000000, 0b00000000 };
	expected[10] = new int[] { 0b00101101, 0b01000000, 0b00000000, 0b00000000 };
	expected[9] = new int[] { 0b00101101, 0b00000000, 0b00000000, 0b00000000 };
	expected[8] = new int[] { 0b00101101, 0b00000000, 0b00000000, 0b00000000 };
	expected[7] = new int[] { 0b00101100, 0b00000000, 0b00000000, 0b00000000 };
	expected[6] = new int[] { 0b00101100, 0b00000000, 0b00000000, 0b00000000 };
	expected[5] = new int[] { 0b00101000, 0b00000000, 0b00000000, 0b00000000 };
	expected[4] = new int[] { 0b00100000, 0b00000000, 0b00000000, 0b00000000 };
	expected[3] = new int[] { 0b00100000, 0b00000000, 0b00000000, 0b00000000 };
	expected[2] = new int[] { 0b00000000, 0b00000000, 0b00000000, 0b00000000 };
	expected[1] = new int[] { 0b00000000, 0b00000000, 0b00000000, 0b00000000 };
	expected[0] = new int[] { 0b00000000, 0b00000000, 0b00000000, 0b00000000 };

	IPv4 ip = IPv4.create(address);
	for (int prefixLength = 32; prefixLength-- > 0;) {
	    assertTrue("Prefix length " + prefixLength, ip.hasParent());
	    IPv4 parent = ip.getParent();
	    assertNotNull("Prefix length " + prefixLength, parent);
	    assertEquals("Prefix length " + prefixLength, prefixLength, parent.getConstPrefixLength());
	    deepAssertEquals("Prefix length " + prefixLength, expected[prefixLength], parent.getAddress());
	    ip = parent;
	}
	assertFalse(ip.hasParent());
    }

    @Test(expected = IllegalStateException.class)
    public void getParentTest0Prefix() {
	IPv4 ip = IPv4.create(FirewallTestsUtility.getRandomAddressIPv4(), 0);
	assertFalse(ip.hasParent());
	ip.getParent();
    }

    @Test
    public void getChildrenTest() {
	System.out.println("# IPv4Test getChildrenTest");
	IPv4 ip = IPv4.create(FirewallTestsUtility.getRandomAddressIPv4(), rand.nextInt(11) + 22);
	assertEquals(ip.hasChildren(), ip.getConstPrefixLength() != IPv4.MAX_LENGTH);
    }

    @Test
    public void equalsTestItselfBasicConstructor() {
	System.out.println("# IPv4Test equalsTestItselfBasicConstructor");
	int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	IPv4 ip1 = IPv4.create(address);
	IPv4 ip2 = IPv4.create(address);
	assertTrue(ip1.equals(ip2));
	assertTrue(ip2.equals(ip1));
    }

    @Test
    public void equalsTestItselfOneIpWithConstructorWithPrefixLength() {
	System.out.println("# IPv4Test equalsTestItselfOneIpWithConstructorWithPrefixLength");
	int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	IPv4 ip1 = IPv4.create(address);
	IPv4 ip2 = IPv4.create(address, 32);
	assertTrue(ip1.equals(ip2));
	assertTrue(ip2.equals(ip1));
    }

    @Test
    public void equalsTestItselfTwoIpwithConstructorWithPrefixLength() {
	System.out.println("# IPv4Test equalsTestItselfTwoIpwithConstructorWithPrefixLength");
	int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	int prefix = FirewallTestsUtility.getRandomPrefixLengthIPv4();
	IPv4 ip1 = IPv4.create(address, prefix);
	IPv4 ip2 = IPv4.create(address, prefix);
	assertTrue(ip1.equals(ip2));
	assertTrue(ip2.equals(ip1));
    }

    @Test
    public void equalsTestNotEqualsItselfTwoDifferentPrefixLength() {
	System.out.println("# IPv4Test equalsTestNotEqualsItselfTwoDifferentPrefixLength");
	int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	int prefix1 = FirewallTestsUtility.getRandomPrefixLengthIPv4();
	int prefix2;
	do {
	    prefix2 = FirewallTestsUtility.getRandomPrefixLengthIPv4();
	} while (prefix1 == prefix2);

	IPv4 ip1 = IPv4.create(address, prefix1);
	IPv4 ip2 = IPv4.create(address, prefix2);
	assertNotEquals(ip1, ip2);
    }

    @Test
    public void containsTestContainsItself() {
	System.out.println("# IPv4Test containsTestContainsItself");
	IPv4 ip = FirewallTestsUtility.getRandomIPv4();
	assertTrue(ip.contains(ip));
    }

    @Test
    public void containsTestNotContainsNull() {
	System.out.println("# IPv4Test containsTestNotContainsNull");
	int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	int prefixLength = FirewallTestsUtility.getRandomPrefixLengthIPv4();
	IPv4 ip1 = IPv4.create(address, prefixLength);
	IPv4 ip2 = null;
	assertFalse(ip1.contains(ip2));
    }

    @Test
    public void containsTestContainsItselfNoPrefixLength() {
	System.out.println("# IPv4Test containsTestContainsItselfNoPrefixLength");
	IPv4 ip1 = IPv4.create(new int[] { 0, 160, 40, 0 });
	IPv4 ip2 = IPv4.create(new int[] { 0, 160, 40, 0 });
	assertTrue(ip1.contains(ip2));
	assertTrue(ip2.contains(ip1));
    }

    @Test
    public void containsTestZeroPrefixLengthContainsAll() {
	System.out.println("# IPv4Test containsTestZeroPrefixLengthContainsAll");
	IPv4 ip1 = IPv4.create(new int[] { 0, 0, 0, 0 }, 0);
	IPv4 ip2 = IPv4.create(FirewallTestsUtility.getRandomAddressIPv4());
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    @Test
    public void containsTestPrefixLength8() {
	System.out.println("# IPv4Test containsTestPrefixLength8");
	IPv4 ip1 = IPv4.create(new int[] { 145, 0, 0, 0 }, 8);
	IPv4 ip2 = IPv4.create(new int[] { 145, 55, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv4.create(new int[] { 145, 0, 48, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv4.create(new int[] { 145, 255, 255, 255 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv4.create(new int[] { 146, 0, 0, 0 });
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    @Test
    public void containsTestPrefixLength15() {
	System.out.println("# IPv4Test containsTestPrefixLength15");
	IPv4 ip1 = IPv4.create(new int[] { 16, 216, 0, 0 }, 15);
	IPv4 ip2 = IPv4.create(new int[] { 16, 217, 11, 7 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv4.create(new int[] { 16, 216, 48, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv4.create(new int[] { 16, 216, 45, 77 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv4.create(new int[] { 17, 216, 14, 42 });
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv4.create(new int[] { 16, 218, 36, 38 });
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    @Test
    public void containsTestPrefixLength21() {
	System.out.println("# IPv4Test containsTestPrefixLength21");
	IPv4 ip1 = IPv4.create(new int[] { 0, 160, 40, 0 }, 21);
	IPv4 ip2 = IPv4.create(new int[] { 0, 160, 47, 7 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv4.create(new int[] { 0, 160, 41, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv4.create(new int[] { 0, 160, 40, 255 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv4.create(new int[] { 0, 160, 96, 0 });
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv4.create(new int[] { 0, 160, 7, 44 });
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    @Test
    public void containsTestPrefixLength27() {
	System.out.println("# IPv4Test containsTestPrefixLength27");
	IPv4 ip1 = IPv4.create(new int[] { 41, 99, 243, 160 }, 27);
	IPv4 ip2 = IPv4.create(new int[] { 41, 99, 243, 160 }, 32);
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv4.create(new int[] { 41, 99, 243, 160 }, 27);
	assertTrue(ip1.contains(ip2));
	assertTrue(ip2.contains(ip1));
	ip2 = IPv4.create(new int[] { 41, 99, 243, 168 }, 29);
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv4.create(new int[] { 41, 99, 243, 176 }, 30);
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv4.create(new int[] { 41, 99, 243, 224 }, 27);
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    @Test
    public void containsTestFullIpNotContainsOthers() {
	System.out.println("# IPv4Test containsTestFullIpNotContainsOthers");
	IPv4 ip1 = IPv4.create(new int[] { 0, 160, 40, 0 });
	IPv4 ip2 = IPv4.create(FirewallTestsUtility.getRandomAddressIPv4());
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
	System.out.println("# IPv4Test containsTestNotContainsIPv6");
	IPv4 ip4 = IPv4.create(FirewallTestsUtility.getRandomAddressIPv4(),
		FirewallTestsUtility.getRandomPrefixLengthIPv4());
	IPv6 ip6 = FirewallTestsUtility.getRandomIPv6();
	assertFalse(ip4.contains(ip6));
    }

    @Test
    public void isBrothersTestBit31() {
	IPv4 ip1 = IPv4.create("167.0.0.1");
	IPv4 ip2 = IPv4.create("167.0.0.0");
	assertTrue(ip1.isBrother(ip2));
	assertTrue(ip2.isBrother(ip1));
    }

    @Test
    public void isBrothersTestBit31NotBrothers() {
	IPv4 ip1 = IPv4.create("167.0.0.1");
	IPv4 ip2 = IPv4.create("167.0.0.2");
	assertFalse(ip1.isBrother(ip2));
	assertFalse(ip2.isBrother(ip1));
    }

    @Test
    public void isBrothersTestBit24() {
	IPv4 ip1 = IPv4.create("167.0.51.128/25");
	IPv4 ip2 = IPv4.create("167.0.51.0/25");
	assertTrue(ip1.isBrother(ip2));
	assertTrue(ip2.isBrother(ip1));
    }

    @Test
    public void isBrothersTestBit24NotBrothers() {
	IPv4 ip1 = IPv4.create("10.0.1.0/25");
	IPv4 ip2 = IPv4.create("10.0.0.128/25");
	assertFalse(ip1.isBrother(ip2));
	assertFalse(ip2.isBrother(ip1));
    }

    @Test
    public void isBrothersTestBit20() {
	IPv4 ip1 = IPv4.create("3.0.0.0/21");
	IPv4 ip2 = IPv4.create("3.0.0.0/21");
	assertTrue(ip1.isBrother(ip2));
	assertTrue(ip2.isBrother(ip1));
    }

    @Test
    public void isBrothersTestBit20NotBrothers() {
	IPv4 ip1 = IPv4.create("167.7.8.0");
	IPv4 ip2 = IPv4.create("167.7.0.0");
	assertFalse(ip1.isBrother(ip2));
	assertFalse(ip2.isBrother(ip1));
    }

    @Test
    public void isBrothersTestBit5() {
	IPv4 ip1 = IPv4.create("12.0.0.0/6");
	IPv4 ip2 = IPv4.create("8.0.0.0/6");
	assertTrue(ip1.isBrother(ip2));
	assertTrue(ip2.isBrother(ip1));
    }

    @Test
    public void isBrothersTestBit5NotBrothers() {
	IPv4 ip1 = IPv4.create("4.0.0.1");
	IPv4 ip2 = IPv4.create("8.0.0.2");
	assertFalse(ip1.isBrother(ip2));
	assertFalse(ip2.isBrother(ip1));
    }

    @Test
    public void isBrothersTestAnyIP() {
	IPv4 ip1 = IPv4.create("0.0.0.0/0");
	IPv4 ip2 = IPv4.create("0.0.0.0/0");
	assertTrue(ip1.isBrother(ip2));
	assertTrue(ip2.isBrother(ip1));
    }

    @Test
    public void isBrothersTestItself() {
	IPv4 ip = FirewallTestsUtility.getRandomIPv4();
	assertTrue(ip.isBrother(ip));
    }

}
