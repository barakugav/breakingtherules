package breakingtherules.tests.firewall;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.IP;
import breakingtherules.firewall.IPv4;
import breakingtherules.firewall.IPv6;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;

public class HitTest {

    private static final Random rand = new Random();

    /*--------------------Test Methods--------------------*/

    @Test
    public void constructorTest() {
	try {
	    int id = getRandomID();
	    List<Attribute> attributes = getRandomAttributes();
	    new Hit(id, attributes);

	} catch (IllegalArgumentException e) {
	    fail("Failed to create Hit: " + e.getMessage());
	}
    }

    @Test
    public void constructorTestNullAttributes() {
	try {
	    int id = getRandomID();
	    List<Attribute> attributes = null;
	    new Hit(id, attributes);
	    fail("Allowed creating Hit with null attributes");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void constructorTestNegativeID() {
	try {
	    int id = -getRandomID();
	    List<Attribute> attributes = getRandomAttributes();
	    new Hit(id, attributes);
	    fail("Allowed creating Hit with negative id");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void getIdTest() {
	int id = getRandomID();
	List<Attribute> attributes = getRandomAttributes();
	Hit hit = new Hit(id, attributes);
	assertEquals(id, hit.getId());
    }

    @Test
    public void getAttributesTest() {
	int id = getRandomID();
	List<Attribute> attributes = getRandomAttributes();
	Hit hit = new Hit(id, attributes);
	assertEquals(attributes, hit.getAttributes());
    }

    @Test
    public void getAttributeTest() {
	int id = getRandomID();
	List<Attribute> attributes = getRandomAttributes();
	Hit hit = new Hit(id, attributes);

	for (Attribute expected : attributes) {
	    String type = expected.getType();
	    Attribute actual = hit.getAttribute(type);
	    assertEquals(expected, actual);
	}
    }

    /*--------------------Help Methods--------------------*/

    private int getRandomID() {
	return rand.nextInt((1 << 31) - 1) + 1; // only positive numbers
    }

    private List<Attribute> getRandomAttributes() {
	List<Attribute> attributes = new ArrayList<Attribute>();
	attributes.add(getRandomSource());
	attributes.add(getRandomDestination());
	attributes.add(getRandomService());
	return attributes;
    }

    private Source getRandomSource() {
	return new Source(getRandomIP());
    }

    private Destination getRandomDestination() {
	return new Destination(getRandomIP());
    }

    private Service getRandomService() {
	String protocol;
	if (rand.nextBoolean())
	    protocol = "TCP";
	else
	    protocol = "UDP";

	int portRangeStart, portRangeEnd;
	do {
	    portRangeStart = rand.nextInt(1 << 16);
	    portRangeEnd = rand.nextInt(1 << 16);
	} while (portRangeStart > portRangeEnd);

	return new Service(protocol, portRangeStart, portRangeEnd);
    }

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
