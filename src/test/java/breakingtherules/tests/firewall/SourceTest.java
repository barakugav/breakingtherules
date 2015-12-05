package breakingtherules.tests.firewall;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Random;

import org.junit.Test;

import breakingtherules.firewall.Destination;
import breakingtherules.firewall.IP;
import breakingtherules.firewall.IPv4;
import breakingtherules.firewall.IPv6;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;

public class SourceTest {

    private Random rand = new Random();

    /*--------------------Help Methods--------------------*/

    @Test
    public void constructorTest() {
	try {
	    IP ip = getRandomIP();
	    new Source(ip);

	} catch (IllegalArgumentException e) {
	    fail("Excetion should not created: " + e.getMessage());
	}
    }

    @Test
    public void constructorTestNullIP() {
	try {
	    IP ip = null;
	    new Source(ip);
	    fail("Allowed Source creation will null address arg");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void constructorStringTest() {
	try {
	    String ip = "IPv4 2.12.45.7/21";
	    new Source(ip);

	} catch (IllegalArgumentException e) {
	    fail("Failed to create destination from string ip: " + e.getMessage());
	}
    }

    @Test
    public void constructorStringTestNullIP() {
	try {
	    String ip = null;
	    new Source(ip);
	    fail("Allowed Source creation will null address arg");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void getIPTest() {
	try {
	    IP ip = getRandomIP();
	    Source source = new Source(ip);
	    assertEquals(ip, source.getIP());

	} catch (IllegalArgumentException e) {
	    fail("Excetion should not created: " + e.getMessage());
	}
    }

    @Test
    public void containsTestContainsItsef() {
	IP ip = getRandomIP();
	Source source1 = new Source(ip);
	Source source2 = new Source(ip);
	assertTrue(source1.contains(source2));
	assertTrue(source2.contains(source1));
    }

    @Test
    public void containsTestNotContainsNull() {
	IP ip = getRandomIP();
	Source source1 = new Source(ip);
	Source source2 = null;
	assertFalse(source1.contains(source2));
    }

    @Test
    public void containsTestNotContainsOtherAttributes() {
	IP ip = getRandomIP();
	Source source = new Source(ip);

	Destination des = new Destination(ip);
	assertFalse(source.contains(des));

	Service service = new Service("TCP", 80);
	assertFalse(source.contains(service));
    }

    @Test
    public void containsTestOtherDestination() {
	IP ip1 = getRandomIP();
	IP ip2 = getRandomIP();
	Source source1 = new Source(ip1);
	Source source2 = new Source(ip2);
	assertEquals(ip1.contains(ip2), source1.contains(source2));
    }

    @Test
    public void toStringTest() {
	IP ip = getRandomIP();
	Source des = new Source(ip);
	assertTrue(ip.toString().equals(des.toString()));
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
