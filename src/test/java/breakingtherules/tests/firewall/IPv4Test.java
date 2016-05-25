package breakingtherules.tests.firewall;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import breakingtherules.firewall.IP;
import breakingtherules.firewall.IPv4;
import breakingtherules.firewall.IPv6;
import breakingtherules.tests.TestBase;

public class IPv4Test extends TestBase {

    private static final Random rand = new Random();

    @Test
    public void constructorTestBasic() {
	int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	IPv4.create(address);
    }

    @Test(expected = NullPointerException.class)
    public void constructorTestBasicWillNullAdressTest() {
	int[] address = null;
	IPv4.create(address);
    }

    @Test
    public void constructorTestWithPrefixLength() {
	int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	int prefixLength = FirewallTestsUtility.getRandomPrefixLengthIPv4();
	IPv4.create(address, prefixLength);
    }

    @Test(expected = NullPointerException.class)
    public void constructorTestWithPrefixLengthNullAdress() {
	int[] address = null;
	int prefixLength = FirewallTestsUtility.getRandomPrefixLengthIPv4();
	IPv4.create(address, prefixLength);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestWithNegativePrefixLength() {
	int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	int prefixLength = -1;
	IPv4.create(address, prefixLength);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestWithPrefixLengthOverMaxLength() {
	int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	int prefixLength = IPv4.MAX_LENGTH + 1;
	IPv4.create(address, prefixLength);
    }

    @Test
    public void constructorFromStringTest() {
	String ipStr = "215.255.0.46";
	int[] address = new int[] { 215, 255, 0, 46 };
	IP ip = IPv4.create(ipStr);
	assertEquals(address, ip.getAddress());
	assertEquals(IPv4.MAX_LENGTH, ip.prefixLength);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTest3Blocks() {
	String ipStr = "255.0.46";
	IPv4.create(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTest5Blocks() {
	String ipStr = "255.0.2.2.46";
	IPv4.create(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestBlockOver255() {
	String ipStr = "255.300.4.46";
	IPv4.create(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestBlockUnder0() {
	String ipStr = "255.-55.4.46";
	IPv4.create(ipStr);
    }

    @Test
    public void constructorFromStringTestPrefixLength10() {
	int prefix = 10;
	String ipStr = "255.2.4.46";
	int[] address = new int[] { 255, 0, 0, 0 };
	IP ip = IPv4.create(ipStr + "/" + prefix);
	assertEquals(prefix, ip.prefixLength);
	assertEquals(address, ip.getAddress());

	prefix = 24;
	ipStr = "84.67.129.5";
	address = new int[] { 84, 67, 129, 0 };
	ip = IPv4.create(ipStr + "/" + prefix);
	assertEquals(prefix, ip.prefixLength);
	assertEquals(address, ip.getAddress());
	prefix = 32;
	address = new int[] { 84, 67, 129, 5 };
	ip = IPv4.create(ipStr + "/" + prefix);
	assertEquals(prefix, ip.prefixLength);
	assertEquals(address, ip.getAddress());
	prefix = 31;
	address = new int[] { 84, 67, 129, 4 };
	ip = IPv4.create(ipStr + "/" + prefix);
	assertEquals(prefix, ip.prefixLength);
	assertEquals(address, ip.getAddress());
	prefix = 0;
	address = new int[] { 0, 0, 0, 0 };
	ip = IPv4.create(ipStr + "/" + prefix);
	assertEquals(prefix, ip.prefixLength);
	assertEquals(address, ip.getAddress());
	prefix = 2;
	address = new int[] { 64, 0, 0, 0 };
	ip = IPv4.create(ipStr + "/" + prefix);
	assertEquals(prefix, ip.prefixLength);
	assertEquals(address, ip.getAddress());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestNegativePrefixLength() {
	String ipStr = "255.2.4.46/-1";
	IPv4.create(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestPrefixLengthOver32() {
	String ipStr = "255.2.4.46/33";
	IPv4.create(ipStr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorFromStringTestExtraNumbers() {
	String ipStr = "255.2.4.46/1 5";
	IPv4.create(ipStr);
    }

    @Test
    public void constructorFromBooleansTest() {
	final boolean T = true, F = false;
	int[] address = new int[] { 0, 0, 0, 0 };
	List<Boolean> l = toBooleanList(F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
		F, F, F, F, F);
	IPv4 ip = IPv4.create(l);
	assertEquals(IPv4.create(address), ip);
	assertEquals(IPv4.MAX_LENGTH, ip.prefixLength);

	address = new int[] { 47, 123, 200, 87 };
	l = toBooleanList(F, F, T, F, T, T, T, T, F, T, T, T, T, F, T, T, T, T, F, F, T, F, F, F, F, T, F, T, F, T, T,
		T);
	ip = IPv4.create(l);
	assertEquals(IPv4.create(address), ip);
	assertEquals(IPv4.MAX_LENGTH, ip.prefixLength);

	address = new int[] { 129, 50, 93, 10 };
	l = toBooleanList(T, F, F, F, F, F, F, T, F, F, T, T, F, F, T, F, F, T, F, T, T, T, F, T, F, F, F, F, T, F, T,
		F);
	ip = IPv4.create(l);
	assertEquals(IPv4.create(address), ip);
	assertEquals(IPv4.MAX_LENGTH, ip.prefixLength);

	address = new int[] { 255, 255, 255, 255 };
	l = toBooleanList(T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T, T,
		T);
	ip = IPv4.create(l);
	assertEquals(IPv4.create(address), ip);
	assertEquals(IPv4.MAX_LENGTH, ip.prefixLength);
    }

    @Test
    public void getAddressTestWithBasicConstructor() {
	int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	IPv4 ip = IPv4.create(address);
	assertEquals(address, ip.getAddress());
    }

    @Test
    public void getAddressTestWithConstructorWithPrefixLength() {
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
	    assertEquals(prefixLength, ip.prefixLength);
	    assertEquals("Prefix length " + prefixLength, expected[prefixLength], ip.getAddress());
	}
    }

    @Test
    public void getPrefixLengthTestBasicConstructor() {
	IPv4 ip = IPv4.create(FirewallTestsUtility.getRandomAddressIPv4());
	assertEquals(IPv4.MAX_LENGTH, ip.prefixLength);
    }

    @Test
    public void getPrefixLengthTestConstructorWithPrefixLength() {
	int prefixLength = FirewallTestsUtility.getRandomPrefixLengthIPv4();
	IPv4 ip = IPv4.create(FirewallTestsUtility.getRandomAddressIPv4(), prefixLength);
	assertEquals(prefixLength, ip.prefixLength);
    }

    @Test
    public void getParentTest() {
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
	    assertEquals("Prefix length " + prefixLength, prefixLength, parent.prefixLength);
	    assertEquals("Prefix length " + prefixLength, expected[prefixLength], parent.getAddress());
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
    public void hasChildrenTest() {
	final int repeat = 25;
	for (int i = 0; i < repeat; i++) {
	    int prefix = rand.nextInt(11) + 22;
	    int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	    IPv4 ip = IPv4.create(address, prefix);
	    assertEquals(ip.hasChildren(), ip.prefixLength != IPv4.MAX_LENGTH);
	}

	for (int i = 0; i < repeat; i++) {
	    int prefix = 32;
	    int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	    IPv4 ip = IPv4.create(address, prefix);
	    assertFalse(ip.hasChildren());
	}

	for (int i = 0; i < repeat; i++) {
	    for (int prefix = 0; prefix < 32; prefix++) {
		int[] address = FirewallTestsUtility.getRandomAddressIPv4();
		IPv4 ip = IPv4.create(address, prefix);
		assertTrue(ip.hasChildren());
	    }
	}
    }

    @Test
    public void getChildrenTest() {
	final int R = 1, L = 0;
	int[] choices = new int[] { L, L, R, L, R, R, L, R, L, R, R, R, R, L, R, R, R, R, L, L, R, L, L, L, L, R, L, R,
		L, R, R, R };
	int[][][] expected = new int[33][2][];
	// [47, 123, 200, 87]
	expected[32][1] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01010111 };
	expected[32][0] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01010110 };
	expected[31][1] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01010110 };
	expected[31][0] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01010100 };
	expected[30][1] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01010100 };
	expected[30][0] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01010000 };
	expected[29][1] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01011000 };
	expected[29][0] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01010000 };
	expected[28][1] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01010000 };
	expected[28][0] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01000000 };
	expected[27][1] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01100000 };
	expected[27][0] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01000000 };
	expected[26][1] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b01000000 };
	expected[26][0] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b00000000 };
	expected[25][1] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b10000000 };
	expected[25][0] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b00000000 };
	expected[24][1] = new int[] { 0b00101101, 0b01111011, 0b11001001, 0b00000000 };
	expected[24][0] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b00000000 };
	expected[23][1] = new int[] { 0b00101101, 0b01111011, 0b11001010, 0b00000000 };
	expected[23][0] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b00000000 };
	expected[22][1] = new int[] { 0b00101101, 0b01111011, 0b11001100, 0b00000000 };
	expected[22][0] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b00000000 };
	expected[21][1] = new int[] { 0b00101101, 0b01111011, 0b11001000, 0b00000000 };
	expected[21][0] = new int[] { 0b00101101, 0b01111011, 0b11000000, 0b00000000 };
	expected[20][1] = new int[] { 0b00101101, 0b01111011, 0b11010000, 0b00000000 };
	expected[20][0] = new int[] { 0b00101101, 0b01111011, 0b11000000, 0b00000000 };
	expected[19][1] = new int[] { 0b00101101, 0b01111011, 0b11100000, 0b00000000 };
	expected[19][0] = new int[] { 0b00101101, 0b01111011, 0b11000000, 0b00000000 };
	expected[18][1] = new int[] { 0b00101101, 0b01111011, 0b11000000, 0b00000000 };
	expected[18][0] = new int[] { 0b00101101, 0b01111011, 0b10000000, 0b00000000 };
	expected[17][1] = new int[] { 0b00101101, 0b01111011, 0b10000000, 0b00000000 };
	expected[17][0] = new int[] { 0b00101101, 0b01111011, 0b00000000, 0b00000000 };
	expected[16][1] = new int[] { 0b00101101, 0b01111011, 0b00000000, 0b00000000 };
	expected[16][0] = new int[] { 0b00101101, 0b01111010, 0b00000000, 0b00000000 };
	expected[15][1] = new int[] { 0b00101101, 0b01111010, 0b00000000, 0b00000000 };
	expected[15][0] = new int[] { 0b00101101, 0b01111000, 0b00000000, 0b00000000 };
	expected[14][1] = new int[] { 0b00101101, 0b01111100, 0b00000000, 0b00000000 };
	expected[14][0] = new int[] { 0b00101101, 0b01111000, 0b00000000, 0b00000000 };
	expected[13][1] = new int[] { 0b00101101, 0b01111000, 0b00000000, 0b00000000 };
	expected[13][0] = new int[] { 0b00101101, 0b01110000, 0b00000000, 0b00000000 };
	expected[12][1] = new int[] { 0b00101101, 0b01110000, 0b00000000, 0b00000000 };
	expected[12][0] = new int[] { 0b00101101, 0b01100000, 0b00000000, 0b00000000 };
	expected[11][1] = new int[] { 0b00101101, 0b01100000, 0b00000000, 0b00000000 };
	expected[11][0] = new int[] { 0b00101101, 0b01000000, 0b00000000, 0b00000000 };
	expected[10][1] = new int[] { 0b00101101, 0b01000000, 0b00000000, 0b00000000 };
	expected[10][0] = new int[] { 0b00101101, 0b00000000, 0b00000000, 0b00000000 };
	expected[9][0] = new int[] { 0b00101101, 0b00000000, 0b00000000, 0b00000000 };
	expected[9][1] = new int[] { 0b00101101, 0b10000000, 0b00000000, 0b00000000 };
	expected[8][1] = new int[] { 0b00101101, 0b00000000, 0b00000000, 0b00000000 };
	expected[8][0] = new int[] { 0b00101100, 0b00000000, 0b00000000, 0b00000000 };
	expected[7][1] = new int[] { 0b00101110, 0b00000000, 0b00000000, 0b00000000 };
	expected[7][0] = new int[] { 0b00101100, 0b00000000, 0b00000000, 0b00000000 };
	expected[6][1] = new int[] { 0b00101100, 0b00000000, 0b00000000, 0b00000000 };
	expected[6][0] = new int[] { 0b00101000, 0b00000000, 0b00000000, 0b00000000 };
	expected[5][1] = new int[] { 0b00101000, 0b00000000, 0b00000000, 0b00000000 };
	expected[5][0] = new int[] { 0b00100000, 0b00000000, 0b00000000, 0b00000000 };
	expected[4][1] = new int[] { 0b00110000, 0b00000000, 0b00000000, 0b00000000 };
	expected[4][0] = new int[] { 0b00100000, 0b00000000, 0b00000000, 0b00000000 };
	expected[3][1] = new int[] { 0b00100000, 0b00000000, 0b00000000, 0b00000000 };
	expected[3][0] = new int[] { 0b00000000, 0b00000000, 0b00000000, 0b00000000 };
	expected[2][1] = new int[] { 0b01000000, 0b00000000, 0b00000000, 0b00000000 };
	expected[2][0] = new int[] { 0b00000000, 0b00000000, 0b00000000, 0b00000000 };
	expected[1][1] = new int[] { 0b10000000, 0b00000000, 0b00000000, 0b00000000 };
	expected[1][0] = new int[] { 0b00000000, 0b00000000, 0b00000000, 0b00000000 };
	int[] address = new int[] { 0b00000000, 0b00000000, 0b00000000, 0b00000000 };

	IPv4 ip = IPv4.create(address, 0);
	for (int prefix = 0; prefix < 32; prefix++) {

	    assertTrue("prefix=" + prefix + ", has no children", ip.hasChildren());
	    IPv4[] children = null;
	    try {
		children = ip.getChildren();
	    } catch (IllegalStateException e) {
		fail("Failed to get children when prefix = " + prefix + " (" + e.getMessage() + ")");
	    }
	    assertEquals("prefix=" + prefix + ", number of children", 2, children.length);
	    assertEquals("prefix=" + prefix + ", child 0", children[0].getAddress(), expected[prefix + 1][0]);
	    assertEquals("prefix=" + prefix + ", child 1", children[1].getAddress(), expected[prefix + 1][1]);
	    assertEquals("prefix=" + prefix + ", child 0 prefix length", prefix + 1, children[0].prefixLength);
	    assertEquals("prefix=" + prefix + ", child 1 prefix length", prefix + 1, children[1].prefixLength);
	    assertTrue("prefix=" + prefix + ", child 0 doesn't have parent", children[0].hasParent());
	    assertTrue("prefix=" + prefix + ", child 1 doesn't have parent", children[1].hasParent());
	    try {
		assertEquals("prefix=" + prefix + ", child 0 parent", ip, children[0].getParent());
		assertEquals("prefix=" + prefix + ", child 1 parent", ip, children[1].getParent());
	    } catch (IllegalStateException e) {
		fail("one of the children failed to get parent have declaring it has one. " + e.getMessage());
	    }
	    ip = children[choices[prefix]];
	}

	assertFalse("prefix=32, ip still have children", ip.hasChildren());
    }

    @Test
    public void containsTestContainsItself() {
	final int repeat = 25;
	for (int i = 0; i < repeat; i++) {
	    IPv4 ip = FirewallTestsUtility.getRandomIPv4();
	    assertTrue(ip.contains(ip));
	}
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
	    // Not really going to happen. one in 2^32
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
    public void equalsTestItself() {
	final int repeat = 25;
	for (int i = 0; i > repeat; i++) {
	    int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	    IPv4 ip1 = IPv4.create(address);
	    IPv4 ip2 = IPv4.create(address);
	    assertTrue(ip1.equals(ip1));
	    assertTrue(ip1.equals(ip2));
	    assertTrue(ip2.equals(ip1));
	}
    }

    @Test
    public void equalsTestItselfOneIpWithConstructorWithPrefixLength() {
	final int repeat = 25;
	for (int i = 0; i < repeat; i++) {
	    int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	    IPv4 ip1 = IPv4.create(address);
	    IPv4 ip2 = IPv4.create(address, 32);
	    assertTrue(ip1.equals(ip2));
	    assertTrue(ip2.equals(ip1));
	}
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

    @Test
    public void getMaxLengthTest() {
	IP ip = FirewallTestsUtility.getRandomIPv4();
	assertEquals(IPv4.MAX_LENGTH, ip.getMaxLength());
    }

    @Test
    public void compareToTest() {
	IPv4 ip1, ip2;

	ip1 = IPv4.create(0, 154, 78, 254);
	ip2 = IPv4.create(0, 154, 78, 255);
	assertTrue(ip1.compareTo(ip2) < 0);
	assertTrue(ip2.compareTo(ip1) > 0);

	ip1 = IPv4.create(0, 154, 78, 0);
	ip2 = IPv4.create(0, 154, 78, 255);
	assertTrue(ip1.compareTo(ip2) < 0);
	assertTrue(ip2.compareTo(ip1) > 0);

	ip1 = IPv4.create(0, 154, 77, 84);
	ip2 = IPv4.create(0, 154, 78, 4);
	assertTrue(ip1.compareTo(ip2) < 0);
	assertTrue(ip2.compareTo(ip1) > 0);

	ip1 = IPv4.create(0, 154, 78, 254);
	ip2 = IPv4.create(1, 0, 0, 0);
	assertTrue(ip1.compareTo(ip2) < 0);
	assertTrue(ip2.compareTo(ip1) > 0);

	ip1 = IPv4.create(0, 154, 78, 254);
	ip2 = IPv4.create(128, 0, 0, 0);
	assertTrue(ip1.compareTo(ip2) < 0);
	assertTrue(ip2.compareTo(ip1) > 0);

	ip1 = IPv4.create(127, 154, 78, 254);
	ip2 = IPv4.create(128, 0, 0, 0);
	assertTrue(ip1.compareTo(ip2) < 0);
	assertTrue(ip2.compareTo(ip1) > 0);

	ip1 = IPv4.create(254, 154, 78, 254);
	ip2 = IPv4.create(255, 0, 0, 0);
	assertTrue(ip1.compareTo(ip2) < 0);
	assertTrue(ip2.compareTo(ip1) > 0);

	ip1 = IPv4.create(new int[] { 4, 100, 255, 4 }, 31);
	ip2 = IPv4.create(new int[] { 4, 100, 255, 4 });
	assertTrue(ip1.compareTo(ip2) < 0);
	assertTrue(ip2.compareTo(ip1) > 0);
    }

    @Test
    public void compareToTestAny() {
	IPv4 anyIp = IPv4.create(new int[] { 0, 0, 0, 0 }, 0);
	final int repeat = 25;
	for (int i = 0; i < repeat; i++) {
	    IPv4 ip = FirewallTestsUtility.getRandomIPv4();
	    if (anyIp.equals(ip)) {
		// One in 2^32
		assertEquals(0, anyIp.compareTo(ip));
		assertEquals(0, ip.compareTo(anyIp));
	    }
	}
    }

    @Test
    public void compareToTestItself() {
	final int repeat = 25;
	for (int i = 0; i < repeat; i++) {
	    IPv4 ip = FirewallTestsUtility.getRandomIPv4();
	    assertEquals(0, ip.compareTo(ip));

	    int[] address = FirewallTestsUtility.getRandomAddressIPv4();
	    IPv4 ip1 = IPv4.create(address);
	    IPv4 ip2 = IPv4.create(address);
	    assertEquals(0, ip1.compareTo(ip2));
	    assertEquals(0, ip2.compareTo(ip1));
	}
    }

    private static List<Boolean> toBooleanList(boolean... arr) {
	List<Boolean> l = new ArrayList<>(arr.length);
	for (boolean b : arr) {
	    l.add(Boolean.valueOf(b));
	}
	return l;
    }

}
