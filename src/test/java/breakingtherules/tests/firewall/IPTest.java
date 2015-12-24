package breakingtherules.tests.firewall;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Random;

import org.junit.Test;

import breakingtherules.firewall.IP;
import breakingtherules.firewall.IPv4;
import breakingtherules.firewall.IPv6;

public class IPTest {

    private static final Random rand = new Random();

    /*--------------------Test Methods--------------------*/

    // Most methods and functionality is tested in IPv4Test and IPv6Test

    @Test
    public void fromStringTestIPv4() {
	try {
	    IP ip = IP.fromString("255.0.28.0/24");
	    assertTrue(ip instanceof IPv4);

	} catch (IllegalArgumentException e) {
	    fail("Failed to create IPv4 from string: " + e.getMessage());
	}
    }

    @Test
    public void fromStringTestIPv6() {
	try {
	    IP ip = IP.fromString("255:0:24:51:24567:55555:0:0/32");
	    assertTrue(ip instanceof IPv6);

	} catch (IllegalArgumentException e) {
	    fail("Failed to create IPv6 from string: " + e.getMessage());
	}
    }

    @Test
    public void hashCodeTestEqualsToItself() {
	IP ip = getRandomIP();
	assertEquals(ip.hashCode(), ip.hashCode());
    }

    @Test
    public void hashCodeTestNoEqualsToOther() {
	IP ip1 = getRandomIP();
	IP ip2 = getRandomIP();
	assertEquals(ip1.equals(ip2), ip1.hashCode() == ip2.hashCode());
    }

    /*--------------------Help Methods--------------------*/

    private IP getRandomIP() {
	int ipID = rand.nextInt(2) * 2 + 4; // 4 or 6
	int[] address = new int[ipID * 2 - 4]; // 4 or 8

	for (int i = 0; i < address.length; i++)
	    address[i] = rand.nextInt(1 << (ipID * 4 - 8)); // rand(256) or
							    // rand(65536)

	int prefixLength = rand.nextInt(ipID * 48 - 160); // 32 or 128

	if (ipID == 4)
	    return new IPv4(address, prefixLength);
	if (ipID == 6)
	    return new IPv6(address, prefixLength);

	return null;
    }

}
