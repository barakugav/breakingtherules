package breakingtherules.tests.firewall;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import breakingtherules.firewall.IP;
import breakingtherules.firewall.IPv4;
import breakingtherules.firewall.IPv6;
import breakingtherules.tests.TestBase;

@SuppressWarnings("javadoc")
public class IPv6Test extends TestBase {

    @Test
    public void constructorFromBooleansTest() {
	final boolean T = true, F = false;
	int[] address = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
	List<Boolean> l = toBooleanList(F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
		F, F, F, F, F);
	l.addAll(l); // Multiply to 64 size
	l.addAll(l); // Multiply to 128 size
	IP ip = IPv6.parseIPv6FromBits(l);
	assertEquals(IPv6.valueOf(address), ip);
	assertEquals(IPv6.SIZE, ip.getMaskSize());

	address = new int[] { 47, 123, 200, 87, 47, 123, 200, 87 };
	l = toBooleanList(F, F, F, F, F, F, F, F, F, F, T, F, T, T, T, T, F, F, F, F, F, F, F, F, F, T, T, T, T, F, T,
		T, F, F, F, F, F, F, F, F, T, T, F, F, T, F, F, F, F, F, F, F, F, F, F, F, F, T, F, T, F, T, T, T);
	l.addAll(l); // Multiply to 128 size
	ip = IPv6.parseIPv6FromBits(l);
	assertEquals(IPv6.valueOf(address), ip);
	assertEquals(IPv6.SIZE, ip.getMaskSize());

	address = new int[] { 129, 50, 93, 10, 129, 50, 93, 10 };
	l = toBooleanList(F, F, F, F, F, F, F, F, T, F, F, F, F, F, F, T, F, F, F, F, F, F, F, F, F, F, T, T, F, F, T,
		F, F, F, F, F, F, F, F, F, F, T, F, T, T, T, F, T, F, F, F, F, F, F, F, F, F, F, F, F, T, F, T, F);
	l.addAll(l); // Multiply to 128 size
	ip = IPv6.parseIPv6FromBits(l);
	assertEquals(IPv6.valueOf(address), ip);
	assertEquals(IPv6.SIZE, ip.getMaskSize());

	address = new int[] { 65535, 65535, 65535, 65535, 65535, 65535, 65535, 65535 };
	l = toBooleanList(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T,
		T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T);
	l.addAll(l); // Multiply to 128 size
	ip = IPv6.parseIPv6FromBits(l);
	assertEquals(IPv6.valueOf(address), ip);
	assertEquals(IPv6.SIZE, ip.getMaskSize());
    }

    @Test
    public void constructorFromStringTest() {
	final String ipStr = "215:255:457:4966:0:65535:78:1257";
	final int[] address = new int[] { 215, 255, 457, 4966, 0, 65535, 78, 1257 };
	final IPv6 ip = IPv6.valueOf(ipStr);
	assertEquals(ipStr, ip.toString());
	assertEquals(address, ip.getAddress());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTest7Blocks() {
	final String ipStr = "255:0:46:4784:48:74:89";
	IPv6.valueOf(ipStr);

    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTest9Blocks() {
	final String ipStr = "255:0:2:46:47863:32146:879:11112:30";
	IPv6.valueOf(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestBlockOver65535() {
	final String ipStr = "255:65536:4:46:801:24020:4852:31";
	IPv6.valueOf(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestBlockUnder0() {
	final String ipStr = "255:-55:4:46:44:879:326:15";
	IPv6.valueOf(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestDoubleDot() {
	// TODO
	final String ipStr = "255::2:4:46:1:1:1:1";
	IPv6.valueOf(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestExtraNumbers() {
	final String ipStr = "255:2:4:46:14:48:79:13245/1 5";
	IPv6.valueOf(ipStr);
    }

    @Test
    public void constructorFromStringTestMaskSize() {
	short maskSize = 26;
	String ipStr = "215:255:457:4966:0:65535:78:1257";
	int[] address = new int[] { 215, 192, 0, 0, 0, 0, 0, 0 };
	IP ip = IPv6.valueOf(ipStr + "/" + maskSize);
	assertEquals(maskSize, ip.getMaskSize());
	assertEquals(address, ip.getAddress());

	maskSize = 112;
	ipStr = "1023:81:57:100:999:4:20000:7894";
	address = new int[] { 1023, 81, 57, 100, 999, 4, 20000, 0 };
	ip = IPv6.valueOf(ipStr + "/" + maskSize);
	assertEquals(maskSize, ip.getMaskSize());
	assertEquals(address, ip.getAddress());
	maskSize = 96;
	address = new int[] { 1023, 81, 57, 100, 999, 4, 0, 0 };
	ip = IPv6.valueOf(ipStr + "/" + maskSize);
	assertEquals(maskSize, ip.getMaskSize());
	assertEquals(address, ip.getAddress());
	maskSize = 94;
	address = new int[] { 1023, 81, 57, 100, 999, 4, 0, 0 };
	ip = IPv6.valueOf(ipStr + "/" + maskSize);
	assertEquals(maskSize, ip.getMaskSize());
	assertEquals(address, ip.getAddress());
	maskSize = 93;
	address = new int[] { 1023, 81, 57, 100, 999, 0, 0, 0 };
	ip = IPv6.valueOf(ipStr + "/" + maskSize);
	assertEquals(maskSize, ip.getMaskSize());
	assertEquals(address, ip.getAddress());
	maskSize = 48;
	address = new int[] { 1023, 81, 57, 0, 0, 0, 0, 0 };
	ip = IPv6.valueOf(ipStr + "/" + maskSize);
	assertEquals(maskSize, ip.getMaskSize());
	assertEquals(address, ip.getAddress());
	maskSize = 47;
	address = new int[] { 1023, 81, 56, 0, 0, 0, 0, 0 };
	ip = IPv6.valueOf(ipStr + "/" + maskSize);
	assertEquals(maskSize, ip.getMaskSize());
	assertEquals(address, ip.getAddress());
	maskSize = 16;
	address = new int[] { 1023, 0, 0, 0, 0, 0, 0, 0 };
	ip = IPv6.valueOf(ipStr + "/" + maskSize);
	assertEquals(maskSize, ip.getMaskSize());
	assertEquals(address, ip.getAddress());
	maskSize = 15;
	address = new int[] { 1022, 0, 0, 0, 0, 0, 0, 0 };
	ip = IPv6.valueOf(ipStr + "/" + maskSize);
	assertEquals(maskSize, ip.getMaskSize());
	assertEquals(address, ip.getAddress());
	maskSize = 14;
	address = new int[] { 1020, 0, 0, 0, 0, 0, 0, 0 };
	ip = IPv6.valueOf(ipStr + "/" + maskSize);
	assertEquals(maskSize, ip.getMaskSize());
	assertEquals(address, ip.getAddress());
	maskSize = 13;
	address = new int[] { 1016, 0, 0, 0, 0, 0, 0, 0 };
	ip = IPv6.valueOf(ipStr + "/" + maskSize);
	assertEquals(maskSize, ip.getMaskSize());
	assertEquals(address, ip.getAddress());
	maskSize = 12;
	address = new int[] { 1008, 0, 0, 0, 0, 0, 0, 0 };
	ip = IPv6.valueOf(ipStr + "/" + maskSize);
	assertEquals(maskSize, ip.getMaskSize());
	assertEquals(address, ip.getAddress());
	maskSize = 11;
	address = new int[] { 992, 0, 0, 0, 0, 0, 0, 0 };
	ip = IPv6.valueOf(ipStr + "/" + maskSize);
	assertEquals(maskSize, ip.getMaskSize());
	assertEquals(address, ip.getAddress());
	maskSize = 10;
	address = new int[] { 960, 0, 0, 0, 0, 0, 0, 0 };
	ip = IPv6.valueOf(ipStr + "/" + maskSize);
	assertEquals(maskSize, ip.getMaskSize());
	assertEquals(address, ip.getAddress());
	maskSize = 9;
	address = new int[] { 896, 0, 0, 0, 0, 0, 0, 0 };
	ip = IPv6.valueOf(ipStr + "/" + maskSize);
	assertEquals(maskSize, ip.getMaskSize());
	assertEquals(address, ip.getAddress());
	maskSize = 8;
	address = new int[] { 768, 0, 0, 0, 0, 0, 0, 0 };
	ip = IPv6.valueOf(ipStr + "/" + maskSize);
	assertEquals(maskSize, ip.getMaskSize());
	assertEquals(address, ip.getAddress());
	maskSize = 7;
	address = new int[] { 512, 0, 0, 0, 0, 0, 0, 0 };
	ip = IPv6.valueOf(ipStr + "/" + maskSize);
	assertEquals(maskSize, ip.getMaskSize());
	assertEquals(address, ip.getAddress());
	maskSize = 6;
	address = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
	ip = IPv6.valueOf(ipStr + "/" + maskSize);
	assertEquals(maskSize, ip.getMaskSize());
	assertEquals(address, ip.getAddress());
	maskSize = 5;
	ip = IPv6.valueOf(ipStr + "/" + maskSize);
	assertEquals(maskSize, ip.getMaskSize());
	assertEquals(address, ip.getAddress());
	maskSize = 4;
	ip = IPv6.valueOf(ipStr + "/" + maskSize);
	assertEquals(maskSize, ip.getMaskSize());
	assertEquals(address, ip.getAddress());
	maskSize = 3;
	ip = IPv6.valueOf(ipStr + "/" + maskSize);
	assertEquals(maskSize, ip.getMaskSize());
	assertEquals(address, ip.getAddress());
	maskSize = 2;
	ip = IPv6.valueOf(ipStr + "/" + maskSize);
	assertEquals(maskSize, ip.getMaskSize());
	assertEquals(address, ip.getAddress());
	maskSize = 1;
	ip = IPv6.valueOf(ipStr + "/" + maskSize);
	assertEquals(maskSize, ip.getMaskSize());
	assertEquals(address, ip.getAddress());
	maskSize = 0;
	ip = IPv6.valueOf(ipStr + "/" + maskSize);
	assertEquals(maskSize, ip.getMaskSize());
	assertEquals(address, ip.getAddress());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestMaskSizeOver128() {
	final String ipStr = "255:2:549:785:324:7841:4:46/129";
	IPv6.valueOf(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestNegativeMaskSize() {
	final String ipStr = "255:2:4:46:4:5:6:1/-1";
	IPv6.valueOf(ipStr);
    }

    @Test
    public void constructorTestBasic() {
	final int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	IPv6.valueOf(address);
    }

    @Test(expected = NullPointerException.class)
    public void constructorTestBasicWillNullAdressTest() {
	final int[] address = null;
	IPv6.valueOf(address);
    }

    @Test
    public void constructorTestWithMaskSize() {
	final int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	final short maskSize = FirewallTestsUtility.getRandomMaskSizeIPv6();
	IPv6.valueOf(address, maskSize);
    }

    @Test(expected = NullPointerException.class)
    public void constructorTestWithMaskSizeNullAdress() {
	final int[] address = null;
	final short maskSize = FirewallTestsUtility.getRandomMaskSizeIPv6();
	IPv6.valueOf(address, maskSize);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestWithMaskSizeOverMaxLength() {
	final int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	final short maskSize = IPv6.SIZE + 1;
	IPv6.valueOf(address, maskSize);

    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestWithNegativeMaskSize() {
	final int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	final short maskSize = -1;
	IPv6.valueOf(address, maskSize);
    }

    @Test
    public void containsTestContainsItself() {
	final IPv6 ip = FirewallTestsUtility.getRandomIPv6();
	assertTrue(ip.contains(ip));
    }

    @Test
    public void containsTestContainsItselfNoMaskSize() {
	final IPv6 ip1 = IPv6.valueOf(new int[] { 0, 160, 40, 0, 10, 0, 540, 0 });
	final IPv6 ip2 = IPv6.valueOf(new int[] { 0, 160, 40, 0, 10, 0, 540, 0 });
	assertTrue(ip1.contains(ip2));
	assertTrue(ip2.contains(ip1));
    }

    @Test
    public void containsTestFullIpNotContainsOthers() {
	final int repeat = 25;
	for (int i = repeat; i-- != 0;) {
	    final IPv6 ip1 = IPv6.valueOf(FirewallTestsUtility.getRandomAddressIPv6());
	    final IPv6 ip2 = IPv6.valueOf(FirewallTestsUtility.getRandomAddressIPv6());
	    if (ip1.equals(ip2))
		continue;
	    assertFalse(ip1.contains(ip2));
	    assertFalse(ip2.contains(ip1));
	}
    }

    @Test
    public void containsTestMaskSize16() {
	final IPv6 ip1 = IPv6.valueOf(new int[] { 145, 0, 0, 0, 0, 0, 0, 0 }, (short) 16);
	IPv6 ip2 = IPv6.valueOf(new int[] { 145, 55, 0, 0, 0, 0, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv6.valueOf(new int[] { 145, 0, 48, 0, 0, 0, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv6.valueOf(new int[] { 145, 255, 255, 255, 0, 0, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv6.valueOf(new int[] { 146, 0, 0, 0, 0, 0, 0, 0 });
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    @Test
    public void containsTestMaskSize31() {
	final IPv6 ip1 = IPv6.valueOf(new int[] { 16, 216, 0, 0, 0, 0, 0, 0 }, (short) 31);
	IPv6 ip2 = IPv6.valueOf(new int[] { 16, 217, 11, 7, 0, 0, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv6.valueOf(new int[] { 16, 216, 48, 0, 0, 0, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv6.valueOf(new int[] { 16, 216, 45, 77, 0, 0, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv6.valueOf(new int[] { 17, 216, 14, 42, 0, 0, 0, 0 });
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv6.valueOf(new int[] { 16, 218, 36, 38, 0, 0, 0, 0 });
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    @Test
    public void containsTestMaskSize45() {
	final IPv6 ip1 = IPv6.valueOf(new int[] { 0, 160, 40, 0, 0, 0, 0, 0 }, (short) 45);
	IPv6 ip2 = IPv6.valueOf(new int[] { 0, 160, 47, 7, 0, 0, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv6.valueOf(new int[] { 0, 160, 41, 0, 0, 0, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv6.valueOf(new int[] { 0, 160, 40, 255, 0, 0, 0, 0 });
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv6.valueOf(new int[] { 0, 160, 96, 0, 0, 0, 0, 0 });
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv6.valueOf(new int[] { 0, 160, 7, 44, 0, 0, 0, 0 });
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    @Test
    public void containsTestMaskSize59() {
	final IPv6 ip1 = IPv6.valueOf(new int[] { 41, 99, 243, 160, 0, 0, 0, 0 }, (short) 59);
	IPv6 ip2 = IPv6.valueOf(new int[] { 41, 99, 243, 160, 0, 0, 0, 0 }, (short) 64);
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv6.valueOf(new int[] { 41, 99, 243, 160, 0, 0, 0, 0 }, (short) 59);
	assertTrue(ip1.contains(ip2));
	assertTrue(ip2.contains(ip1));
	ip2 = IPv6.valueOf(new int[] { 41, 99, 243, 168, 0, 0, 0, 0 }, (short) 61);
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv6.valueOf(new int[] { 41, 99, 243, 176, 0, 0, 0, 0 }, (short) 62);
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
	ip2 = IPv6.valueOf(new int[] { 41, 99, 243, 224, 0, 0, 0, 0 }, (short) 59);
	assertFalse(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    @Test
    public void containsTestNotContainsIPv4() {
	final IPv6 ip6 = IPv6.valueOf(FirewallTestsUtility.getRandomAddressIPv6(),
		FirewallTestsUtility.getRandomMaskSizeIPv6());
	final IPv4 ip4 = FirewallTestsUtility.getRandomIPv4();
	assertFalse(ip6.contains(ip4));
    }

    @Test
    public void containsTestZeroMaskSizeContainsAll() {
	final IPv6 ip1 = IPv6.valueOf(new int[] { 0, 0, 0, 0, 0, 0, 0, 0 }, (short) 0);
	final IPv6 ip2 = IPv6.valueOf(FirewallTestsUtility.getRandomAddressIPv6());
	assertTrue(ip1.contains(ip2));
	assertFalse(ip2.contains(ip1));
    }

    @Test
    public void equalsTestItselfBasicConstructor() {
	final int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	final IPv6 ip1 = IPv6.valueOf(address);
	final IPv6 ip2 = IPv6.valueOf(address);
	assertTrue(ip1.equals(ip2));
	assertTrue(ip2.equals(ip1));
    }

    @Test
    public void equalsTestItselfOneIpWithConstructorWithMaskSize() {
	final int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	final IPv6 ip1 = IPv6.valueOf(address);
	final IPv6 ip2 = IPv6.valueOf(address, (short) 128);
	assertTrue(ip1.equals(ip2));
	assertTrue(ip2.equals(ip1));
    }

    @Test
    public void equalsTestItselfTwoIpwithConstructorWithMaskSize() {
	final int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	final short maskSize = FirewallTestsUtility.getRandomMaskSizeIPv6();
	final IPv6 ip1 = IPv6.valueOf(address, maskSize);
	final IPv6 ip2 = IPv6.valueOf(address, maskSize);
	assertTrue(ip1.equals(ip2));
	assertTrue(ip2.equals(ip1));
    }

    @Test
    public void equalsTestNotEqualsItselfTwoDifferentMaskSize() {
	final int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	final short maskSize1 = FirewallTestsUtility.getRandomMaskSizeIPv6();
	short maskSize2;
	do
	    maskSize2 = FirewallTestsUtility.getRandomMaskSizeIPv6();
	while (maskSize1 == maskSize2);

	final IPv6 ip1 = IPv6.valueOf(address, maskSize1);
	final IPv6 ip2 = IPv6.valueOf(address, maskSize2);
	assertNotEquals(ip1, ip2);
    }

    @Test
    public void getAddressTestConstructorWithMaskSize() {
	final int[] address = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011,
		0b1100100001010111, 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111 };
	final int[][] expected = new int[129][];
	expected[128] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111 };
	expected[127] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010110 };
	expected[126] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010100 };
	expected[125] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010000 };
	expected[124] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010000 };
	expected[123] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001000000 };
	expected[122] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001000000 };
	expected[121] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000 };
	expected[120] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000 };
	expected[119] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000 };
	expected[118] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000 };
	expected[117] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000 };
	expected[116] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100000000000000 };
	expected[115] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100000000000000 };
	expected[114] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100000000000000 };
	expected[113] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1000000000000000 };
	expected[112] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0 };
	expected[111] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111010, 0 };
	expected[110] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111000, 0 };
	expected[109] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111000, 0 };
	expected[108] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101110000, 0 };
	expected[107] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101100000, 0 };
	expected[106] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101000000, 0 };
	expected[105] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110100000000, 0 };
	expected[104] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110100000000, 0 };
	expected[103] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110000000000, 0 };
	expected[102] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110000000000, 0 };
	expected[101] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010100000000000, 0 };
	expected[100] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010000000000000, 0 };
	expected[99] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010000000000000, 0 };
	expected[98] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0, 0 };
	expected[97] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0, 0 };
	expected[96] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0, 0 };
	expected[95] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010110, 0, 0 };
	expected[94] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010100, 0, 0 };
	expected[93] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010000, 0, 0 };
	expected[92] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010000, 0, 0 };
	expected[91] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001000000, 0, 0 };
	expected[90] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001000000, 0, 0 };
	expected[89] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100000000000, 0, 0 };
	expected[88] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100000000000, 0, 0 };
	expected[87] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100000000000, 0, 0 };
	expected[86] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100000000000, 0, 0 };
	expected[85] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100000000000, 0, 0 };
	expected[84] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100000000000000, 0, 0 };
	expected[83] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100000000000000, 0, 0 };
	expected[82] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100000000000000, 0, 0 };
	expected[81] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1000000000000000, 0, 0 };
	expected[80] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0, 0, 0 };
	expected[79] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111010, 0, 0, 0 };
	expected[78] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111000, 0, 0, 0 };
	expected[77] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111000, 0, 0, 0 };
	expected[76] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101110000, 0, 0, 0 };
	expected[75] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101100000, 0, 0, 0 };
	expected[74] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101000000, 0, 0, 0 };
	expected[73] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110100000000, 0, 0, 0 };
	expected[72] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110100000000, 0, 0, 0 };
	expected[71] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110000000000, 0, 0, 0 };
	expected[70] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110000000000, 0, 0, 0 };
	expected[69] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010100000000000, 0, 0, 0 };
	expected[68] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010000000000000, 0, 0, 0 };
	expected[67] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010000000000000, 0, 0, 0 };
	expected[66] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111, 0, 0,
		0, 0 };
	expected[65] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111, 0, 0,
		0, 0 };
	expected[64] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111, 0, 0,
		0, 0 };
	expected[63] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010110, 0, 0,
		0, 0 };
	expected[62] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010100, 0, 0,
		0, 0 };
	expected[61] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010000, 0, 0,
		0, 0 };
	expected[60] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010000, 0, 0,
		0, 0 };
	expected[59] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001000000, 0, 0,
		0, 0 };
	expected[58] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001000000, 0, 0,
		0, 0 };
	expected[57] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000, 0, 0,
		0, 0 };
	expected[56] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000, 0, 0,
		0, 0 };
	expected[55] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000, 0, 0,
		0, 0 };
	expected[54] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000, 0, 0,
		0, 0 };
	expected[53] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000, 0, 0,
		0, 0 };
	expected[52] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100000000000000, 0, 0,
		0, 0 };
	expected[51] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100000000000000, 0, 0,
		0, 0 };
	expected[50] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100000000000000, 0, 0,
		0, 0 };
	expected[49] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1000000000000000, 0, 0,
		0, 0 };
	expected[48] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0, 0, 0, 0, 0 };
	expected[47] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111010, 0, 0, 0, 0, 0 };
	expected[46] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111000, 0, 0, 0, 0, 0 };
	expected[45] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111000, 0, 0, 0, 0, 0 };
	expected[44] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101110000, 0, 0, 0, 0, 0 };
	expected[43] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101100000, 0, 0, 0, 0, 0 };
	expected[42] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101000000, 0, 0, 0, 0, 0 };
	expected[41] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110100000000, 0, 0, 0, 0, 0 };
	expected[40] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110100000000, 0, 0, 0, 0, 0 };
	expected[39] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110000000000, 0, 0, 0, 0, 0 };
	expected[38] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110000000000, 0, 0, 0, 0, 0 };
	expected[37] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010100000000000, 0, 0, 0, 0, 0 };
	expected[36] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010000000000000, 0, 0, 0, 0, 0 };
	expected[35] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010000000000000, 0, 0, 0, 0, 0 };
	expected[34] = new int[] { 0b0010110101111011, 0b1100100001010111, 0, 0, 0, 0, 0, 0 };
	expected[33] = new int[] { 0b0010110101111011, 0b1100100001010111, 0, 0, 0, 0, 0, 0 };
	expected[32] = new int[] { 0b0010110101111011, 0b1100100001010111, 0, 0, 0, 0, 0, 0 };
	expected[31] = new int[] { 0b0010110101111011, 0b1100100001010110, 0, 0, 0, 0, 0, 0 };
	expected[30] = new int[] { 0b0010110101111011, 0b1100100001010100, 0, 0, 0, 0, 0, 0 };
	expected[29] = new int[] { 0b0010110101111011, 0b1100100001010000, 0, 0, 0, 0, 0, 0 };
	expected[28] = new int[] { 0b0010110101111011, 0b1100100001010000, 0, 0, 0, 0, 0, 0 };
	expected[27] = new int[] { 0b0010110101111011, 0b1100100001000000, 0, 0, 0, 0, 0, 0 };
	expected[26] = new int[] { 0b0010110101111011, 0b1100100001000000, 0, 0, 0, 0, 0, 0 };
	expected[25] = new int[] { 0b0010110101111011, 0b1100100000000000, 0, 0, 0, 0, 0, 0 };
	expected[24] = new int[] { 0b0010110101111011, 0b1100100000000000, 0, 0, 0, 0, 0, 0 };
	expected[23] = new int[] { 0b0010110101111011, 0b1100100000000000, 0, 0, 0, 0, 0, 0 };
	expected[22] = new int[] { 0b0010110101111011, 0b1100100000000000, 0, 0, 0, 0, 0, 0 };
	expected[21] = new int[] { 0b0010110101111011, 0b1100100000000000, 0, 0, 0, 0, 0, 0 };
	expected[20] = new int[] { 0b0010110101111011, 0b1100000000000000, 0, 0, 0, 0, 0, 0 };
	expected[19] = new int[] { 0b0010110101111011, 0b1100000000000000, 0, 0, 0, 0, 0, 0 };
	expected[18] = new int[] { 0b0010110101111011, 0b1100000000000000, 0, 0, 0, 0, 0, 0 };
	expected[17] = new int[] { 0b0010110101111011, 0b1000000000000000, 0, 0, 0, 0, 0, 0 };
	expected[16] = new int[] { 0b0010110101111011, 0, 0, 0, 0, 0, 0, 0 };
	expected[15] = new int[] { 0b0010110101111010, 0, 0, 0, 0, 0, 0, 0 };
	expected[14] = new int[] { 0b0010110101111000, 0, 0, 0, 0, 0, 0, 0 };
	expected[13] = new int[] { 0b0010110101111000, 0, 0, 0, 0, 0, 0, 0 };
	expected[12] = new int[] { 0b0010110101110000, 0, 0, 0, 0, 0, 0, 0 };
	expected[11] = new int[] { 0b0010110101100000, 0, 0, 0, 0, 0, 0, 0 };
	expected[10] = new int[] { 0b0010110101000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[9] = new int[] { 0b0010110100000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[8] = new int[] { 0b0010110100000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[7] = new int[] { 0b0010110000000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[6] = new int[] { 0b0010110000000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[5] = new int[] { 0b0010100000000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[4] = new int[] { 0b0010000000000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[3] = new int[] { 0b0010000000000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[2] = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
	expected[1] = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
	expected[0] = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };

	for (short maskSize = 128; maskSize >= 0; maskSize--) {
	    final IPv6 ip = IPv6.valueOf(address, maskSize);
	    assertEquals("Mask size " + maskSize, maskSize, ip.getMaskSize());
	    assertEquals("Mask size " + maskSize, expected[maskSize], ip.getAddress());
	}
    }

    @Test
    public void getAddressTestWithBasicConstructor() {
	final int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	final IPv6 ip = IPv6.valueOf(address);
	assertEquals(address, ip.getAddress());
    }

    @Test
    public void getChildrenTest() {
	final int R = 1, L = 0;
	final int[] choices = new int[] { L, L, R, L, R, R, L, R, L, R, R, R, R, L, R, R, R, R, L, L, R, L, L, L, L, R,
		L, R, L, R, R, R, L, L, R, L, R, R, L, R, L, R, R, R, R, L, R, R, R, R, L, L, R, L, L, L, L, R, L, R, L,
		R, R, R, L, L, R, L, R, R, L, R, L, R, R, R, R, L, R, R, R, R, L, L, R, L, L, L, L, R, L, R, L, R, R, R,
		L, L, R, L, R, R, L, R, L, R, R, R, R, L, R, R, R, R, L, L, R, L, L, L, L, R, L, R, L, R, R, R };
	final int[][][] expected = new int[129][2][];
	expected[128][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111 };
	expected[128][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010110 };
	expected[127][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010110 };
	expected[127][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010100 };
	expected[126][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010100 };
	expected[126][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010000 };
	expected[125][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001011000 };
	expected[125][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010000 };
	expected[124][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010000 };
	expected[124][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001000000 };
	expected[123][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001100000 };
	expected[123][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001000000 };
	expected[122][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001000000 };
	expected[122][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000 };
	expected[121][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100010000000 };
	expected[121][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000 };
	expected[120][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100100000000 };
	expected[120][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000 };
	expected[119][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100101000000000 };
	expected[119][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000 };
	expected[118][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100110000000000 };
	expected[118][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000 };
	expected[117][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000 };
	expected[117][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100000000000000 };
	expected[116][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1101000000000000 };
	expected[116][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100000000000000 };
	expected[115][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1110000000000000 };
	expected[115][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100000000000000 };
	expected[114][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100000000000000 };
	expected[114][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1000000000000000 };
	expected[113][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1000000000000000 };
	expected[113][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b0000000000000000 };
	expected[112][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0 };
	expected[112][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111010, 0 };
	expected[111][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111010, 0 };
	expected[111][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111000, 0 };
	expected[110][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111100, 0 };
	expected[110][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111000, 0 };
	expected[109][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111000, 0 };
	expected[109][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101110000, 0 };
	expected[108][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101110000, 0 };
	expected[108][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101100000, 0 };
	expected[107][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101100000, 0 };
	expected[107][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101000000, 0 };
	expected[106][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101000000, 0 };
	expected[106][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110100000000, 0 };
	expected[105][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110110000000, 0 };
	expected[105][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110100000000, 0 };
	expected[104][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110100000000, 0 };
	expected[104][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110000000000, 0 };
	expected[103][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010111000000000, 0 };
	expected[103][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110000000000, 0 };
	expected[102][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110000000000, 0 };
	expected[102][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010100000000000, 0 };
	expected[101][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010100000000000, 0 };
	expected[101][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010000000000000, 0 };
	expected[100][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0011000000000000, 0 };
	expected[100][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010000000000000, 0 };
	expected[99][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010000000000000, 0 };
	expected[99][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0000000000000000, 0 };
	expected[98][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0100000000000000, 0 };
	expected[98][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0000000000000000, 0 };
	expected[97][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b1000000000000000, 0 };
	expected[97][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0000000000000000, 0 };
	expected[96][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0, 0 };
	expected[96][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010110, 0, 0 };
	expected[95][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010110, 0, 0 };
	expected[95][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010100, 0, 0 };
	expected[94][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010100, 0, 0 };
	expected[94][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010000, 0, 0 };
	expected[93][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001011000, 0, 0 };
	expected[93][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010000, 0, 0 };
	expected[92][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010000, 0, 0 };
	expected[92][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001000000, 0, 0 };
	expected[91][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001100000, 0, 0 };
	expected[91][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001000000, 0, 0 };
	expected[90][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001000000, 0, 0 };
	expected[90][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100000000000, 0, 0 };
	expected[89][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100010000000, 0, 0 };
	expected[89][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100000000000, 0, 0 };
	expected[88][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100100000000, 0, 0 };
	expected[88][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100000000000, 0, 0 };
	expected[87][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100101000000000, 0, 0 };
	expected[87][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100000000000, 0, 0 };
	expected[86][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100110000000000, 0, 0 };
	expected[86][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100000000000, 0, 0 };
	expected[85][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100000000000, 0, 0 };
	expected[85][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100000000000000, 0, 0 };
	expected[84][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1101000000000000, 0, 0 };
	expected[84][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100000000000000, 0, 0 };
	expected[83][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1110000000000000, 0, 0 };
	expected[83][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100000000000000, 0, 0 };
	expected[82][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100000000000000, 0, 0 };
	expected[82][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1000000000000000, 0, 0 };
	expected[81][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1000000000000000, 0, 0 };
	expected[81][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b0000000000000000, 0, 0 };
	expected[80][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0, 0, 0 };
	expected[80][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111010, 0, 0, 0 };
	expected[79][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111010, 0, 0, 0 };
	expected[79][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111000, 0, 0, 0 };
	expected[78][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111100, 0, 0, 0 };
	expected[78][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111000, 0, 0, 0 };
	expected[77][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111000, 0, 0, 0 };
	expected[77][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101110000, 0, 0, 0 };
	expected[76][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101110000, 0, 0, 0 };
	expected[76][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101100000, 0, 0, 0 };
	expected[75][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101100000, 0, 0, 0 };
	expected[75][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101000000, 0, 0, 0 };
	expected[74][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101000000, 0, 0, 0 };
	expected[74][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110100000000, 0, 0, 0 };
	expected[73][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110110000000, 0, 0, 0 };
	expected[73][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110100000000, 0, 0, 0 };
	expected[72][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110100000000, 0, 0, 0 };
	expected[72][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110000000000, 0, 0, 0 };
	expected[71][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010111000000000, 0, 0, 0 };
	expected[71][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110000000000, 0, 0, 0 };
	expected[70][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110000000000, 0, 0, 0 };
	expected[70][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010100000000000, 0, 0, 0 };
	expected[69][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010100000000000, 0, 0, 0 };
	expected[69][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010000000000000, 0, 0, 0 };
	expected[68][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0011000000000000, 0, 0, 0 };
	expected[68][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010000000000000, 0, 0, 0 };
	expected[67][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010000000000000, 0, 0, 0 };
	expected[67][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0000000000000000, 0, 0, 0 };
	expected[66][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0100000000000000, 0, 0, 0 };
	expected[66][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0000000000000000, 0, 0, 0 };
	expected[65][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b1000000000000000, 0, 0, 0 };
	expected[65][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0000000000000000, 0, 0, 0 };
	expected[64][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111, 0,
		0, 0, 0 };
	expected[64][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010110, 0,
		0, 0, 0 };
	expected[63][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010110, 0,
		0, 0, 0 };
	expected[63][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010100, 0,
		0, 0, 0 };
	expected[62][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010100, 0,
		0, 0, 0 };
	expected[62][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010000, 0,
		0, 0, 0 };
	expected[61][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001011000, 0,
		0, 0, 0 };
	expected[61][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010000, 0,
		0, 0, 0 };
	expected[60][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010000, 0,
		0, 0, 0 };
	expected[60][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001000000, 0,
		0, 0, 0 };
	expected[59][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001100000, 0,
		0, 0, 0 };
	expected[59][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001000000, 0,
		0, 0, 0 };
	expected[58][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001000000, 0,
		0, 0, 0 };
	expected[58][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000, 0,
		0, 0, 0 };
	expected[57][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100010000000, 0,
		0, 0, 0 };
	expected[57][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000, 0,
		0, 0, 0 };
	expected[56][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100100000000, 0,
		0, 0, 0 };
	expected[56][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000, 0,
		0, 0, 0 };
	expected[55][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100101000000000, 0,
		0, 0, 0 };
	expected[55][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000, 0,
		0, 0, 0 };
	expected[54][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100110000000000, 0,
		0, 0, 0 };
	expected[54][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000, 0,
		0, 0, 0 };
	expected[53][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000, 0,
		0, 0, 0 };
	expected[53][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100000000000000, 0,
		0, 0, 0 };
	expected[52][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1101000000000000, 0,
		0, 0, 0 };
	expected[52][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100000000000000, 0,
		0, 0, 0 };
	expected[51][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1110000000000000, 0,
		0, 0, 0 };
	expected[51][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100000000000000, 0,
		0, 0, 0 };
	expected[50][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100000000000000, 0,
		0, 0, 0 };
	expected[50][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1000000000000000, 0,
		0, 0, 0 };
	expected[49][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1000000000000000, 0,
		0, 0, 0 };
	expected[49][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b0000000000000000, 0,
		0, 0, 0 };
	expected[48][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0, 0, 0, 0, 0 };
	expected[48][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111010, 0, 0, 0, 0, 0 };
	expected[47][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111010, 0, 0, 0, 0, 0 };
	expected[47][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111000, 0, 0, 0, 0, 0 };
	expected[46][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111100, 0, 0, 0, 0, 0 };
	expected[46][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111000, 0, 0, 0, 0, 0 };
	expected[45][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111000, 0, 0, 0, 0, 0 };
	expected[45][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101110000, 0, 0, 0, 0, 0 };
	expected[44][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101110000, 0, 0, 0, 0, 0 };
	expected[44][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101100000, 0, 0, 0, 0, 0 };
	expected[43][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101100000, 0, 0, 0, 0, 0 };
	expected[43][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101000000, 0, 0, 0, 0, 0 };
	expected[42][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101000000, 0, 0, 0, 0, 0 };
	expected[42][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110100000000, 0, 0, 0, 0, 0 };
	expected[41][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110110000000, 0, 0, 0, 0, 0 };
	expected[41][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110100000000, 0, 0, 0, 0, 0 };
	expected[40][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110100000000, 0, 0, 0, 0, 0 };
	expected[40][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110000000000, 0, 0, 0, 0, 0 };
	expected[39][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010111000000000, 0, 0, 0, 0, 0 };
	expected[39][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110000000000, 0, 0, 0, 0, 0 };
	expected[38][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110000000000, 0, 0, 0, 0, 0 };
	expected[38][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010100000000000, 0, 0, 0, 0, 0 };
	expected[37][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010100000000000, 0, 0, 0, 0, 0 };
	expected[37][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010000000000000, 0, 0, 0, 0, 0 };
	expected[36][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0011000000000000, 0, 0, 0, 0, 0 };
	expected[36][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010000000000000, 0, 0, 0, 0, 0 };
	expected[35][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010000000000000, 0, 0, 0, 0, 0 };
	expected[35][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0000000000000000, 0, 0, 0, 0, 0 };
	expected[34][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0100000000000000, 0, 0, 0, 0, 0 };
	expected[34][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0000000000000000, 0, 0, 0, 0, 0 };
	expected[33][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b1000000000000000, 0, 0, 0, 0, 0 };
	expected[33][0] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0000000000000000, 0, 0, 0, 0, 0 };
	expected[32][1] = new int[] { 0b0010110101111011, 0b1100100001010111, 0, 0, 0, 0, 0, 0 };
	expected[32][0] = new int[] { 0b0010110101111011, 0b1100100001010110, 0, 0, 0, 0, 0, 0 };
	expected[31][1] = new int[] { 0b0010110101111011, 0b1100100001010110, 0, 0, 0, 0, 0, 0 };
	expected[31][0] = new int[] { 0b0010110101111011, 0b1100100001010100, 0, 0, 0, 0, 0, 0 };
	expected[30][1] = new int[] { 0b0010110101111011, 0b1100100001010100, 0, 0, 0, 0, 0, 0 };
	expected[30][0] = new int[] { 0b0010110101111011, 0b1100100001010000, 0, 0, 0, 0, 0, 0 };
	expected[29][1] = new int[] { 0b0010110101111011, 0b1100100001011000, 0, 0, 0, 0, 0, 0 };
	expected[29][0] = new int[] { 0b0010110101111011, 0b1100100001010000, 0, 0, 0, 0, 0, 0 };
	expected[28][1] = new int[] { 0b0010110101111011, 0b1100100001010000, 0, 0, 0, 0, 0, 0 };
	expected[28][0] = new int[] { 0b0010110101111011, 0b1100100001000000, 0, 0, 0, 0, 0, 0 };
	expected[27][1] = new int[] { 0b0010110101111011, 0b1100100001100000, 0, 0, 0, 0, 0, 0 };
	expected[27][0] = new int[] { 0b0010110101111011, 0b1100100001000000, 0, 0, 0, 0, 0, 0 };
	expected[26][1] = new int[] { 0b0010110101111011, 0b1100100001000000, 0, 0, 0, 0, 0, 0 };
	expected[26][0] = new int[] { 0b0010110101111011, 0b1100100000000000, 0, 0, 0, 0, 0, 0 };
	expected[25][1] = new int[] { 0b0010110101111011, 0b1100100010000000, 0, 0, 0, 0, 0, 0 };
	expected[25][0] = new int[] { 0b0010110101111011, 0b1100100000000000, 0, 0, 0, 0, 0, 0 };
	expected[24][1] = new int[] { 0b0010110101111011, 0b1100100100000000, 0, 0, 0, 0, 0, 0 };
	expected[24][0] = new int[] { 0b0010110101111011, 0b1100100000000000, 0, 0, 0, 0, 0, 0 };
	expected[23][1] = new int[] { 0b0010110101111011, 0b1100101000000000, 0, 0, 0, 0, 0, 0 };
	expected[23][0] = new int[] { 0b0010110101111011, 0b1100100000000000, 0, 0, 0, 0, 0, 0 };
	expected[22][1] = new int[] { 0b0010110101111011, 0b1100110000000000, 0, 0, 0, 0, 0, 0 };
	expected[22][0] = new int[] { 0b0010110101111011, 0b1100100000000000, 0, 0, 0, 0, 0, 0 };
	expected[21][1] = new int[] { 0b0010110101111011, 0b1100100000000000, 0, 0, 0, 0, 0, 0 };
	expected[21][0] = new int[] { 0b0010110101111011, 0b1100000000000000, 0, 0, 0, 0, 0, 0 };
	expected[20][1] = new int[] { 0b0010110101111011, 0b1101000000000000, 0, 0, 0, 0, 0, 0 };
	expected[20][0] = new int[] { 0b0010110101111011, 0b1100000000000000, 0, 0, 0, 0, 0, 0 };
	expected[19][1] = new int[] { 0b0010110101111011, 0b1110000000000000, 0, 0, 0, 0, 0, 0 };
	expected[19][0] = new int[] { 0b0010110101111011, 0b1100000000000000, 0, 0, 0, 0, 0, 0 };
	expected[18][1] = new int[] { 0b0010110101111011, 0b1100000000000000, 0, 0, 0, 0, 0, 0 };
	expected[18][0] = new int[] { 0b0010110101111011, 0b1000000000000000, 0, 0, 0, 0, 0, 0 };
	expected[17][1] = new int[] { 0b0010110101111011, 0b1000000000000000, 0, 0, 0, 0, 0, 0 };
	expected[17][0] = new int[] { 0b0010110101111011, 0b0000000000000000, 0, 0, 0, 0, 0, 0 };
	expected[16][1] = new int[] { 0b0010110101111011, 0, 0, 0, 0, 0, 0, 0 };
	expected[16][0] = new int[] { 0b0010110101111010, 0, 0, 0, 0, 0, 0, 0 };
	expected[15][1] = new int[] { 0b0010110101111010, 0, 0, 0, 0, 0, 0, 0 };
	expected[15][0] = new int[] { 0b0010110101111000, 0, 0, 0, 0, 0, 0, 0 };
	expected[14][1] = new int[] { 0b0010110101111100, 0, 0, 0, 0, 0, 0, 0 };
	expected[14][0] = new int[] { 0b0010110101111000, 0, 0, 0, 0, 0, 0, 0 };
	expected[13][1] = new int[] { 0b0010110101111000, 0, 0, 0, 0, 0, 0, 0 };
	expected[13][0] = new int[] { 0b0010110101110000, 0, 0, 0, 0, 0, 0, 0 };
	expected[12][1] = new int[] { 0b0010110101110000, 0, 0, 0, 0, 0, 0, 0 };
	expected[12][0] = new int[] { 0b0010110101100000, 0, 0, 0, 0, 0, 0, 0 };
	expected[11][1] = new int[] { 0b0010110101100000, 0, 0, 0, 0, 0, 0, 0 };
	expected[11][0] = new int[] { 0b0010110101000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[10][1] = new int[] { 0b0010110101000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[10][0] = new int[] { 0b0010110100000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[9][1] = new int[] { 0b0010110110000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[9][0] = new int[] { 0b0010110100000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[8][1] = new int[] { 0b0010110100000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[8][0] = new int[] { 0b0010110000000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[7][1] = new int[] { 0b0010111000000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[7][0] = new int[] { 0b0010110000000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[6][1] = new int[] { 0b0010110000000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[6][0] = new int[] { 0b0010100000000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[5][1] = new int[] { 0b0010100000000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[5][0] = new int[] { 0b0010000000000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[4][1] = new int[] { 0b0011000000000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[4][0] = new int[] { 0b0010000000000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[3][1] = new int[] { 0b0010000000000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[3][0] = new int[] { 0b0000000000000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[2][1] = new int[] { 0b0100000000000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[2][0] = new int[] { 0b0000000000000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[1][1] = new int[] { 0b1000000000000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[1][0] = new int[] { 0b0000000000000000, 0, 0, 0, 0, 0, 0, 0 };
	final int[] address = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };

	IPv6 ip = IPv6.valueOf(address, (short) 0);
	for (short maskSize = 0; maskSize < 128; maskSize++) {

	    assertTrue("maskSize=" + maskSize + ", has no children", ip.hasChildren());
	    IPv6[] children = null;
	    try {
		children = ip.getChildren();
	    } catch (final IllegalStateException e) {
		fail("Failed to get children when maskSize = " + maskSize + " (" + e.getMessage() + ")");
	    }
	    assertEquals("maskSize=" + maskSize + ", number of children", 2, children.length);
	    assertEquals("maskSize=" + maskSize + ", child 0", expected[maskSize + 1][0], children[0].getAddress());
	    assertEquals("maskSize=" + maskSize + ", child 1", expected[maskSize + 1][1], children[1].getAddress());
	    assertEquals("maskSize=" + maskSize + ", child 0 mask size", maskSize + 1, children[0].getMaskSize());
	    assertEquals("maskSize=" + maskSize + ", child 1 mask size", maskSize + 1, children[1].getMaskSize());
	    assertTrue("maskSize=" + maskSize + ", child 0 doesn't have parent", children[0].hasParent());
	    assertTrue("maskSize=" + maskSize + ", child 1 doesn't have parent", children[1].hasParent());
	    try {
		assertEquals("maskSize=" + maskSize + ", child 0 parent", ip, children[0].getParent());
		assertEquals("maskSize=" + maskSize + ", child 1 parent", ip, children[1].getParent());
	    } catch (final IllegalStateException e) {
		fail("one of the children failed to get parent have declaring it has one. " + e.getMessage());
	    }
	    ip = children[choices[maskSize]];
	}

	assertFalse("maskSize=128, ip still have children", ip.hasChildren());
    }

    @Test
    public void getMaskSizeTestBasicConstructor() {
	final IPv6 ip = IPv6.valueOf(FirewallTestsUtility.getRandomAddressIPv6());
	assertEquals(IPv6.SIZE, ip.getMaskSize());
    }

    @Test
    public void getMaskSizeTestConstructorWithMaskSize() {
	final short maskSize = FirewallTestsUtility.getRandomMaskSizeIPv6();
	final IPv6 ip = IPv6.valueOf(FirewallTestsUtility.getRandomAddressIPv6(), maskSize);
	assertEquals(maskSize, ip.getMaskSize());
    }

    @Test
    public void getParentTest() {
	final int[] address = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011,
		0b1100100001010111, 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111 };
	final int[][] expected = new int[129][];
	expected[128] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111 };
	expected[127] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010110 };
	expected[126] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010100 };
	expected[125] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010000 };
	expected[124] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010000 };
	expected[123] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001000000 };
	expected[122] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001000000 };
	expected[121] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000 };
	expected[120] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000 };
	expected[119] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000 };
	expected[118] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000 };
	expected[117] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000 };
	expected[116] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100000000000000 };
	expected[115] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100000000000000 };
	expected[114] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100000000000000 };
	expected[113] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1000000000000000 };
	expected[112] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0 };
	expected[111] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111010, 0 };
	expected[110] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111000, 0 };
	expected[109] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101111000, 0 };
	expected[108] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101110000, 0 };
	expected[107] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101100000, 0 };
	expected[106] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110101000000, 0 };
	expected[105] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110100000000, 0 };
	expected[104] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110100000000, 0 };
	expected[103] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110000000000, 0 };
	expected[102] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010110000000000, 0 };
	expected[101] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010100000000000, 0 };
	expected[100] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010000000000000, 0 };
	expected[99] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0b0010000000000000, 0 };
	expected[98] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0, 0 };
	expected[97] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0, 0 };
	expected[96] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010111, 0, 0 };
	expected[95] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010110, 0, 0 };
	expected[94] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010100, 0, 0 };
	expected[93] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010000, 0, 0 };
	expected[92] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001010000, 0, 0 };
	expected[91] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001000000, 0, 0 };
	expected[90] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100001000000, 0, 0 };
	expected[89] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100000000000, 0, 0 };
	expected[88] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100000000000, 0, 0 };
	expected[87] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100000000000, 0, 0 };
	expected[86] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100000000000, 0, 0 };
	expected[85] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100100000000000, 0, 0 };
	expected[84] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100000000000000, 0, 0 };
	expected[83] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100000000000000, 0, 0 };
	expected[82] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1100000000000000, 0, 0 };
	expected[81] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0b1000000000000000, 0, 0 };
	expected[80] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111011, 0, 0, 0 };
	expected[79] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111010, 0, 0, 0 };
	expected[78] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111000, 0, 0, 0 };
	expected[77] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101111000, 0, 0, 0 };
	expected[76] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101110000, 0, 0, 0 };
	expected[75] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101100000, 0, 0, 0 };
	expected[74] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110101000000, 0, 0, 0 };
	expected[73] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110100000000, 0, 0, 0 };
	expected[72] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110100000000, 0, 0, 0 };
	expected[71] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110000000000, 0, 0, 0 };
	expected[70] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010110000000000, 0, 0, 0 };
	expected[69] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010100000000000, 0, 0, 0 };
	expected[68] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010000000000000, 0, 0, 0 };
	expected[67] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111,
		0b0010000000000000, 0, 0, 0 };
	expected[66] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111, 0, 0,
		0, 0 };
	expected[65] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111, 0, 0,
		0, 0 };
	expected[64] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010111, 0, 0,
		0, 0 };
	expected[63] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010110, 0, 0,
		0, 0 };
	expected[62] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010100, 0, 0,
		0, 0 };
	expected[61] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010000, 0, 0,
		0, 0 };
	expected[60] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001010000, 0, 0,
		0, 0 };
	expected[59] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001000000, 0, 0,
		0, 0 };
	expected[58] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100001000000, 0, 0,
		0, 0 };
	expected[57] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000, 0, 0,
		0, 0 };
	expected[56] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000, 0, 0,
		0, 0 };
	expected[55] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000, 0, 0,
		0, 0 };
	expected[54] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000, 0, 0,
		0, 0 };
	expected[53] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100100000000000, 0, 0,
		0, 0 };
	expected[52] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100000000000000, 0, 0,
		0, 0 };
	expected[51] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100000000000000, 0, 0,
		0, 0 };
	expected[50] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1100000000000000, 0, 0,
		0, 0 };
	expected[49] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0b1000000000000000, 0, 0,
		0, 0 };
	expected[48] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111011, 0, 0, 0, 0, 0 };
	expected[47] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111010, 0, 0, 0, 0, 0 };
	expected[46] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111000, 0, 0, 0, 0, 0 };
	expected[45] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101111000, 0, 0, 0, 0, 0 };
	expected[44] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101110000, 0, 0, 0, 0, 0 };
	expected[43] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101100000, 0, 0, 0, 0, 0 };
	expected[42] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110101000000, 0, 0, 0, 0, 0 };
	expected[41] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110100000000, 0, 0, 0, 0, 0 };
	expected[40] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110100000000, 0, 0, 0, 0, 0 };
	expected[39] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110000000000, 0, 0, 0, 0, 0 };
	expected[38] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010110000000000, 0, 0, 0, 0, 0 };
	expected[37] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010100000000000, 0, 0, 0, 0, 0 };
	expected[36] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010000000000000, 0, 0, 0, 0, 0 };
	expected[35] = new int[] { 0b0010110101111011, 0b1100100001010111, 0b0010000000000000, 0, 0, 0, 0, 0 };
	expected[34] = new int[] { 0b0010110101111011, 0b1100100001010111, 0, 0, 0, 0, 0, 0 };
	expected[33] = new int[] { 0b0010110101111011, 0b1100100001010111, 0, 0, 0, 0, 0, 0 };
	expected[32] = new int[] { 0b0010110101111011, 0b1100100001010111, 0, 0, 0, 0, 0, 0 };
	expected[31] = new int[] { 0b0010110101111011, 0b1100100001010110, 0, 0, 0, 0, 0, 0 };
	expected[30] = new int[] { 0b0010110101111011, 0b1100100001010100, 0, 0, 0, 0, 0, 0 };
	expected[29] = new int[] { 0b0010110101111011, 0b1100100001010000, 0, 0, 0, 0, 0, 0 };
	expected[28] = new int[] { 0b0010110101111011, 0b1100100001010000, 0, 0, 0, 0, 0, 0 };
	expected[27] = new int[] { 0b0010110101111011, 0b1100100001000000, 0, 0, 0, 0, 0, 0 };
	expected[26] = new int[] { 0b0010110101111011, 0b1100100001000000, 0, 0, 0, 0, 0, 0 };
	expected[25] = new int[] { 0b0010110101111011, 0b1100100000000000, 0, 0, 0, 0, 0, 0 };
	expected[24] = new int[] { 0b0010110101111011, 0b1100100000000000, 0, 0, 0, 0, 0, 0 };
	expected[23] = new int[] { 0b0010110101111011, 0b1100100000000000, 0, 0, 0, 0, 0, 0 };
	expected[22] = new int[] { 0b0010110101111011, 0b1100100000000000, 0, 0, 0, 0, 0, 0 };
	expected[21] = new int[] { 0b0010110101111011, 0b1100100000000000, 0, 0, 0, 0, 0, 0 };
	expected[20] = new int[] { 0b0010110101111011, 0b1100000000000000, 0, 0, 0, 0, 0, 0 };
	expected[19] = new int[] { 0b0010110101111011, 0b1100000000000000, 0, 0, 0, 0, 0, 0 };
	expected[18] = new int[] { 0b0010110101111011, 0b1100000000000000, 0, 0, 0, 0, 0, 0 };
	expected[17] = new int[] { 0b0010110101111011, 0b1000000000000000, 0, 0, 0, 0, 0, 0 };
	expected[16] = new int[] { 0b0010110101111011, 0, 0, 0, 0, 0, 0, 0 };
	expected[15] = new int[] { 0b0010110101111010, 0, 0, 0, 0, 0, 0, 0 };
	expected[14] = new int[] { 0b0010110101111000, 0, 0, 0, 0, 0, 0, 0 };
	expected[13] = new int[] { 0b0010110101111000, 0, 0, 0, 0, 0, 0, 0 };
	expected[12] = new int[] { 0b0010110101110000, 0, 0, 0, 0, 0, 0, 0 };
	expected[11] = new int[] { 0b0010110101100000, 0, 0, 0, 0, 0, 0, 0 };
	expected[10] = new int[] { 0b0010110101000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[9] = new int[] { 0b0010110100000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[8] = new int[] { 0b0010110100000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[7] = new int[] { 0b0010110000000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[6] = new int[] { 0b0010110000000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[5] = new int[] { 0b0010100000000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[4] = new int[] { 0b0010000000000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[3] = new int[] { 0b0010000000000000, 0, 0, 0, 0, 0, 0, 0 };
	expected[2] = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
	expected[1] = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
	expected[0] = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };

	IPv6 ip = IPv6.valueOf(address);
	for (short maskSize = 128; maskSize-- > 0;) {
	    assertTrue("Mask size " + maskSize, ip.hasParent());
	    final IPv6 parent = ip.getParent();
	    assertNotNull("Mask size " + maskSize, parent);
	    assertEquals("Mask size " + maskSize, maskSize, parent.getMaskSize());
	    assertEquals("Mask size " + maskSize, expected[maskSize], parent.getAddress());
	    ip = parent;
	}
	assertFalse(ip.hasParent());
    }

    @Test(expected = IllegalStateException.class)
    public void getParentTestPrefix0() {
	final IPv6 ip = IPv6.valueOf(FirewallTestsUtility.getRandomAddressIPv6(), (short) 0);
	assertFalse(ip.hasParent());
	ip.getParent();
    }

    @Test
    public void hasChildrenTest() {
	final int repeat = 25;
	for (int i = 0; i < repeat; i++) {
	    final short maskSize = (short) (rand.nextInt(11) + 22);
	    final int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	    final IPv6 ip = IPv6.valueOf(address, maskSize);
	    assertEquals("maskSize=" + ip.getMaskSize(), ip.getMaskSize() != IPv6.SIZE, ip.hasChildren());
	}

	for (int i = 0; i < repeat; i++) {
	    final short maskSize = IPv6.SIZE;
	    final int[] address = FirewallTestsUtility.getRandomAddressIPv6();
	    final IPv6 ip = IPv6.valueOf(address, maskSize);
	    assertFalse(ip.hasChildren());
	}

	for (int i = 0; i < repeat; i++)
	    for (short maskSize = 0; maskSize < IPv6.SIZE; maskSize++) {
		final int[] address = FirewallTestsUtility.getRandomAddressIPv6();
		final IPv6 ip = IPv6.valueOf(address, maskSize);
		assertTrue(ip.hasChildren());
	    }
    }

    private static List<Boolean> toBooleanList(final boolean... arr) {
	final List<Boolean> l = new ArrayList<>(arr.length);
	for (final boolean b : arr)
	    l.add(Boolean.valueOf(b));
	return l;
    }

}
