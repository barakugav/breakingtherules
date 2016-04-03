package breakingtherules.tests.firewall;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import breakingtherules.firewall.IP;
import breakingtherules.firewall.IPv4;
import breakingtherules.firewall.IPv6;

public class IPTest {

    // Most methods and functionality is tested in IPv4Test and IPv6Test

    @Test
    public void getBitTestFirstBlock() {
	IP ip = new IPv4(new int[] { 0b00010000, 0, 0, 0 });
	assertEquals(0, ip.getBit(3));
	assertEquals(1, ip.getBit(4));
	assertEquals(0, ip.getBit(5));
    }

    @Test
    public void getBitTest3Block() {
	IP ip = new IPv4(new int[] { 0, 0, 0b00010000, 0 });
	assertEquals(0, ip.getBit(19));
	assertEquals(1, ip.getBit(20));
	assertEquals(0, ip.getBit(21));
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
