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

public class DestinationTest {

    private Random rand = new Random();

    /*--------------------Help Methods--------------------*/

    @Test
    public void constructorTest() {
	try {
	    IP ip = getRandomIP();
	    new Destination(ip);

	} catch (IllegalArgumentException e) {
	    fail("Excetion should not created: " + e.getMessage());
	}
    }

    @Test
    public void constructorTestNullIP() {
	try {
	    IP ip = null;
	    new Destination(ip);
	    fail("Allowed Destination creation will null address arg");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void constructorStringTest() {
	try {
	    String ip = "IPv4 2.12.45.7/21";
	    new Destination(ip);

	} catch (IllegalArgumentException e) {
	    fail("Failed to create destination from string ip: " + e.getMessage());
	}
    }

    @Test
    public void constructorStringTestNullIP() {
	try {
	    String ip = null;
	    new Destination(ip);
	    fail("Allowed Destination creation will null address arg");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void getIPTest() {
	try {
	    IP ip = getRandomIP();
	    Destination des = new Destination(ip);
	    assertEquals(ip, des.getIP());

	} catch (IllegalArgumentException e) {
	    fail("Excetion should not created: " + e.getMessage());
	}
    }

    @Test
    public void containsTestContainsItsef() {
	IP ip = getRandomIP();
	Destination des1 = new Destination(ip);
	Destination des2 = new Destination(ip);
	assertTrue(des1.contains(des1));
	assertTrue(des1.contains(des2));
	assertTrue(des2.contains(des1));
    }

    @Test
    public void containsTestNotContainsNull() {
	IP ip = getRandomIP();
	Destination des1 = new Destination(ip);
	Destination des2 = null;
	assertFalse(des1.contains(des2));
    }

    @Test
    public void containsTestNotContainsOtherAttributes() {
	IP ip = getRandomIP();
	Destination des = new Destination(ip);

	Source source = new Source(ip);
	assertFalse(des.contains(source));

	Service service = new Service("TCP", 80);
	assertFalse(des.contains(service));
    }

    @Test
    public void containsTestOtherDestination() {
	IP ip1 = getRandomIP();
	IP ip2 = getRandomIP();
	Destination des1 = new Destination(ip1);
	Destination des2 = new Destination(ip2);
	assertEquals(ip1.contains(ip2), des1.contains(des2));
    }

    @Test
    public void toStringTest() {
	IP ip = getRandomIP();
	Destination des = new Destination(ip);
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
