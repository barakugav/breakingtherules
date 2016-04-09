package breakingtherules.tests.firewall;

import static breakingtherules.tests.utilities.JUnitUtilities.advanceAssertEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import breakingtherules.firewall.IP;
import breakingtherules.firewall.IPv4;
import breakingtherules.firewall.IPv6;
import breakingtherules.utilities.ArraysUtilities;

public class IPTest {

    // Most methods and functionality is tested in IPv4Test and IPv6Test

    @Test
    public void getBitTestFirstBlock() {
	System.out.println("# IPTest getBitTestFirstBlock");
	IP ip = new IPv4(new int[] { 0b00010000, 0, 0, 0 });
	assertEquals(false, ip.getBit(3));
	assertEquals(true, ip.getBit(4));
	assertEquals(false, ip.getBit(5));
    }

    @Test
    public void getBitTest3Block() {
	System.out.println("# IPTest getBitTest3Block");
	IP ip = new IPv4(new int[] { 0, 0, 0b00010000, 0 });
	assertEquals(false, ip.getBit(19));
	assertEquals(true, ip.getBit(20));
	assertEquals(false, ip.getBit(21));
    }

    @Test
    public void fromBooleansTestIPv4() {
	System.out.println("# IPTest fromBooleansTestIPv4");
	final int blockSize = 8;
	int[] address = new int[] { 0, 128, 4, 11 };
	boolean[] addressBol = new boolean[0];
	for (int block : address) {
	    addressBol = ArraysUtilities.merge(addressBol, ArraysUtilities.intToBooleans(block, blockSize));
	}

	IP ip = IP.fromBooleans(addressBol, IPv4.class);
	assertNotNull(ip);
	assertTrue(ip instanceof IPv4);
	advanceAssertEquals(address, ip.getAddress());

    }

    @Test
    public void fromBooleansTestIPv6() {
	System.out.println("# IPTest fromBooleansTestIPv6");
	final int blockSize = 16;
	int[] address = new int[] { 0, 45, 8794, 64, 54, 165, 1, 41 };
	boolean[] addressBol = new boolean[0];
	for (int block : address) {
	    addressBol = ArraysUtilities.merge(addressBol, ArraysUtilities.intToBooleans(block, blockSize));
	}

	IP ip = IP.fromBooleans(addressBol, IPv6.class);
	assertNotNull(ip);
	assertTrue(ip instanceof IPv6);
	advanceAssertEquals(address, ip.getAddress());
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromBooleansTestNullIp() {
	System.out.println("# IPTest fromBooleansTestNullIp");
	IP.fromBooleans(null, IPv4.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromBooleansTestNullClass() {
	System.out.println("# IPTest fromBooleansTestNullClass");
	boolean[] ip = new boolean[] {};
	IP.fromBooleans(ip, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromBooleansTestUnkownClass() {
	System.out.println("# IPTest fromBooleansTestUnkownClass");
	boolean[] ip = new boolean[] {};
	IP.fromBooleans(ip, getClass());
    }

    @Test
    public void fromStringTestIPv4() {
	System.out.println("# IPTest fromStringTestIPv4");
	IP ip = IP.fromString("255.0.28.0/24");
	assertTrue(ip instanceof IPv4);
    }

    @Test
    public void fromStringTestIPv6() {
	System.out.println("# IPTest fromStringTestIPv6");
	IP ip = IP.fromString("255:0:24:51:24567:55555:0:0/32");
	assertTrue(ip instanceof IPv6);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromStringTestNull() {
	IP.fromString(null);
    }

    @Test
    public void hashCodeTestEqualsToItself() {
	System.out.println("# IPTest hashCodeTestEqualsToItself");
	IP ip = FirewallTestsUtility.getRandomIP();
	assertEquals(ip.hashCode(), ip.hashCode());
    }

    @Test
    public void hashCodeTestNoEqualsToOther() {
	System.out.println("# IPTest hashCodeTestNoEqualsToOther");
	IP ip1 = FirewallTestsUtility.getRandomIP();
	IP ip2 = FirewallTestsUtility.getRandomIP();
	assertEquals(ip1.equals(ip2), ip1.hashCode() == ip2.hashCode());
    }

}
