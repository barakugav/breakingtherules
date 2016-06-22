package breakingtherules.tests.firewall;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import breakingtherules.firewall.IP;
import breakingtherules.firewall.IPv4;
import breakingtherules.firewall.IPv6;
import breakingtherules.tests.TestBase;

@SuppressWarnings("javadoc")
public class IPTest extends TestBase {

    // Most methods and functionality is tested in IPv4Test and IPv6Test

    @Test
    public void getBitTestFirstBlock() {
	IP ip = IPv4.valueOf(new int[] { 0b00010000, 0, 0, 0 });
	assertEquals(false, ip.getBit(3));
	assertEquals(true, ip.getBit(4));
	assertEquals(false, ip.getBit(5));
    }

    @Test
    public void getBitTest3Block() {
	IP ip = IPv4.valueOf(new int[] { 0, 0, 0b00010000, 0 });
	assertEquals(false, ip.getBit(19));
	assertEquals(true, ip.getBit(20));
	assertEquals(false, ip.getBit(21));
    }

    @Test
    public void fromBooleansTestIPv4() {
	final int blockSize = 8;
	int[] address = new int[] { 0, 128, 4, 11 };
	boolean[] addressBol = new boolean[0];
	for (int block : address) {
	    addressBol = FirewallTestsUtility.merge(addressBol, FirewallTestsUtility.intToBooleans(block, blockSize));
	}

	IP ip = IP.parseIPFromBits(toBooleanList(addressBol), IPv4.class);
	assertNotNull(ip);
	assertTrue(ip instanceof IPv4);
	assertEquals(address, ip.getAddress());

    }

    @Test
    public void fromBooleansTestIPv6() {
	final int blockSize = 16;
	int[] address = new int[] { 0, 45, 8794, 64, 54, 165, 1, 41 };
	boolean[] addressBol = new boolean[0];
	for (int block : address) {
	    addressBol = FirewallTestsUtility.merge(addressBol, FirewallTestsUtility.intToBooleans(block, blockSize));
	}

	IP ip = IP.parseIPFromBits(toBooleanList(addressBol), IPv6.class);
	assertNotNull(ip);
	assertTrue(ip instanceof IPv6);
	assertEquals(address, ip.getAddress());
    }

    @Test(expected = NullPointerException.class)
    public void fromBooleansTestNullIp() {
	IP.parseIPFromBits(null, IPv4.class);
    }

    @Test(expected = NullPointerException.class)
    public void fromBooleansTestNullClass() {
	boolean[] ip = new boolean[] {};
	IP.parseIPFromBits(toBooleanList(ip), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromBooleansTestUnkownClass() {
	boolean[] ip = new boolean[] {};
	IP.parseIPFromBits(toBooleanList(ip), getClass());
    }

    @Test
    public void fromStringTestIPv4() {
	IP ip = IP.valueOf("255.0.28.0/24");
	assertTrue(ip instanceof IPv4);
    }

    @Test
    public void fromStringTestIPv6() {
	IP ip = IP.valueOf("255:0:24:51:24567:55555:0:0/32");
	assertTrue(ip instanceof IPv6);
    }

    @Test(expected = NullPointerException.class)
    public void fromStringTestNull() {
	IP.valueOf(null);
    }

    @Test
    public void hashCodeTestEqualsToItself() {
	IP ip = FirewallTestsUtility.getRandomIP();
	assertEquals(ip.hashCode(), ip.hashCode());
    }

    @Test
    public void hashCodeTestNoEqualsToOther() {
	IP ip1 = FirewallTestsUtility.getRandomIP();
	IP ip2 = FirewallTestsUtility.getRandomIP();
	assertEquals(ip1.equals(ip2), ip1.hashCode() == ip2.hashCode());
    }

    private static List<Boolean> toBooleanList(boolean[] a) {
	List<Boolean> l = new ArrayList<>(a.length);
	for (boolean b : a) {
	    l.add(Boolean.valueOf(b));
	}
	return l;
    }

}
