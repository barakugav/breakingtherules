package breakingtherules.tests.firewall;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Random;

import org.junit.Test;

import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;

public class ServiceTest {

    private static final Random rand = new Random();

    /*--------------------Test Methods--------------------*/

    @Test
    public void constructorTestOnePort() {
	try {
	    String protocol = "TCP";
	    int port = getRandomPort();
	    new Service(protocol, port);

	} catch (IllegalArgumentException e) {
	    fail("Failed to create service with one port: " + e.getMessage());
	}
    }

    @Test
    public void constructorTestOnePortAnyProtocol() {
	try {
	    String protocol = Service.ANY_PROTOCOL;
	    int port = getRandomPort();
	    new Service(protocol, port);

	} catch (IllegalArgumentException e) {
	    fail("Failed to create service with one port and any protocol: " + e.getMessage());
	}
    }

    @Test
    public void constructorTestOneNegativePort() {
	try {
	    String protocol = "TCP";
	    int port = -2;
	    new Service(protocol, port);
	    fail("Allowed to create service with negative port");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void constructorTestOnePortOver2pow16() {
	try {
	    String protocol = "TCP";
	    int port = 1 << 16;
	    new Service(protocol, port);
	    fail("Allowed to create service with port over max port");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void contructorTestPortRange() {
	try {
	    String protocol = "TCP";
	    int range[] = getRandomRange();
	    new Service(protocol, range[0], range[1]);

	} catch (IllegalArgumentException e) {
	    fail("Failed to create service with port range: " + e.getMessage());
	}
    }

    @Test
    public void contructorTestPortRangeAnyProtocol() {
	try {
	    String protocol = Service.ANY_PROTOCOL;
	    int range[] = getRandomRange();
	    new Service(protocol, range[0], range[1]);

	} catch (IllegalArgumentException e) {
	    fail("Failed to create service with port range and any protocol: " + e.getMessage());
	}
    }

    @Test
    public void contructorTestPortRangeUpperRangeLowerThanLowerRange() {
	try {
	    String protocol = "TCP";
	    int range[] = getRandomRange();
	    new Service(protocol, range[1], range[0]);
	    fail("Allowed creation of service with port range and upperRange < lowerRange");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void contructorTestPortRangeNegativePort() {
	try {
	    String protocol = "TCP";
	    int range[] = getRandomRange();
	    new Service(protocol, -1, range[1]);
	    fail("Allowed creation of service with port range and lowerRange < 0");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void contructorTestPortRangeUpperRangeOver2pow16() {
	try {
	    String protocol = "TCP";
	    int range[] = getRandomRange();
	    new Service(protocol, range[0], 1 << 16);
	    fail("Allowed creation of service with port range and upperRange >= 1 << 16");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void contructorTestFromStringNullString() {
	try {
	    new Service(null);
	    fail("Allowed creation of service from string with null String");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void contructorTestFromStringOnePort() {
	try {
	    new Service("TCP 80");

	} catch (IllegalArgumentException e) {
	    fail("Failed to create service from String: " + e.getMessage());
	}
    }

    @Test
    public void contructorTestFromStringOnePortAnyProtocol() {
	try {
	    new Service("Port 80");

	} catch (IllegalArgumentException e) {
	    fail("Failed to create service from String and any protocol: " + e.getMessage());
	}
    }

    @Test
    public void contructorTestFromStringOnePortNoProtocol() {
	try {
	    new Service("80");
	    fail("Allowed creation of service from String without protocol");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void contructorTestFromStringOnePortNoProtocolAndSpace() {
	try {
	    new Service(" 80");
	    fail("Allowed creation of service from String without protocol and space");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void contructorTestFromStringOnePortNegative() {
	try {
	    new Service("TCP -1");
	    fail("Allowed creation of service from String with negative port");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void contructorTestFromStringOnePortOver2pow16() {
	try {
	    new Service("TCP 65536");
	    fail("Allowed creation of service from String with port over 1 << 16");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void contructorTestFromStringPortRange() {
	try {
	    new Service("TCP 80-100");

	} catch (IllegalArgumentException e) {
	    fail("Failed to create service from String and port range: " + e.getMessage());
	}
    }

    @Test
    public void contructorTestFromStringPortRangeAnyProtocol() {
	try {
	    new Service("Ports 80-100");

	} catch (IllegalArgumentException e) {
	    fail("Failed to create service from String and port range: " + e.getMessage());
	}
    }

    @Test
    public void contructorTestFromStringPortRangeNoProtocolWithSpace() {
	try {
	    new Service(" 80-100");
	    fail("Allowed creation of service from string with port range with no protocol with space");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void contructorTestFromStringPortRangeUpperRangeUnderLowerRange() {
	try {
	    new Service("TCP 100-80");
	    fail("Allowed creation of service from string with port range upperRange < lowerRange");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void contructorTestFromStringPortRangeNegative() {
	try {
	    new Service("TCP -1-100");
	    fail("Allowed creation of service from string negative port");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void contructorTestFromStringPortRangePortOver2pow16() {
	try {
	    new Service("TCP 80-65536");
	    fail("Allowed creation of service from string with port over 1 << 16");

	} catch (IllegalArgumentException e) {
	    // success
	}
    }

    @Test
    public void contructorTestFromStringPortRangeNaN() {
	try {
	    new Service("TCP sdw-100");
	    fail("Allowed creation of service from string with characters instead of numebr");

	} catch (IllegalArgumentException e) {
	    // success
	} catch (Exception e) {
	    fail("Wrong exception throwen, should throw IllegalArgumentException");
	}
    }

    @Test
    public void contructorTestFromStringAnyPort() {
	try {
	    new Service("Any TCP");

	} catch (IllegalArgumentException e) {
	    fail("Failed to create service from string with any port: " + e.getMessage());
	}
    }

    @Test
    public void contructorTestFromStringAnyPortAnyProtocol() {
	Service s1 = null,
		s2 = null;

	try {
	    s1 = new Service("Any");

	} catch (IllegalArgumentException e) {
	    fail("Failed to create service from string with any port and any protocol: " + e.getMessage());
	}

	try {
	    s2 = new Service("Any Any");

	} catch (IllegalArgumentException e) {
	    fail("Failed to create service from string with any port and any protocol: " + e.getMessage());
	}

	assertEquals("Should be same object", s1, s2);
    }

    @Test
    public void getProtocol() {
	String protocol = "TCP";
	Service service;

	int port = getRandomPort();
	service = new Service(protocol, port);
	assertTrue(protocol.equals(service.getProtocol()));

	int[] range = getRandomRange();
	service = new Service(protocol, range[0], range[1]);
	assertTrue(protocol.equals(service.getProtocol()));
    }

    @Test
    public void getProtocolAnyProtocol() {
	String protocol = Service.ANY_PROTOCOL;
	Service service;

	int port = getRandomPort();
	service = new Service(protocol, port);
	assertTrue(protocol.equals(service.getProtocol()));

	int[] range = getRandomRange();
	service = new Service(protocol, range[0], range[1]);
	assertTrue(protocol.equals(service.getProtocol()));
    }

    @Test
    public void getPortRangeStartOnePort() {
	String protocol = "TCP";
	int port = getRandomPort();
	Service service = new Service(protocol, port);
	assertEquals(port, service.getPortRangeStart());
    }

    @Test
    public void getPortRangeStartPortRange() {
	String protocol = "TCP";
	int[] range = getRandomRange();
	Service service = new Service(protocol, range[0], range[1]);
	assertEquals(range[0], service.getPortRangeStart());
    }

    @Test
    public void getPortRangeStartOnePortFromString() {
	Service service = new Service("TCP 50");
	assertEquals(50, service.getPortRangeStart());
    }

    @Test
    public void getPortRangeStartPortRangeFromString() {
	Service service = new Service("TCP 50-70");
	assertEquals(50, service.getPortRangeStart());
    }

    @Test
    public void getPortRangeEndOnePort() {
	String protocol = "TCP";
	int port = getRandomPort();
	Service service = new Service(protocol, port);
	assertEquals(port, service.getPortRangeEnd());
    }

    @Test
    public void getPortRangeEndPortRange() {
	String protocol = "TCP";
	int[] range = getRandomRange();
	Service service = new Service(protocol, range[0], range[1]);
	assertEquals(range[1], service.getPortRangeEnd());
    }

    @Test
    public void getPortRangeEndOnePortFromString() {
	Service service = new Service("TCP 70");
	assertEquals(70, service.getPortRangeEnd());
    }

    @Test
    public void getPortRangeEndPortRangeFromString() {
	Service service = new Service("TCP 50-70");
	assertEquals(70, service.getPortRangeEnd());
    }

    @Test
    public void containsTestPortRangeContainsOnePort() {
	String protocol = "TCP";
	Service service1 = new Service(protocol, 1);
	Service service2 = new Service(protocol, 0, 10);
	assertFalse(service1.contains(service2));
	assertTrue(service2.contains(service1));
    }

    @Test
    public void containsTestPortRangeContainsPortRange() {
	String protocol = "TCP";
	Service service1 = new Service(protocol, 1, 8);
	Service service2 = new Service(protocol, 0, 10);
	assertFalse(service1.contains(service2));
	assertTrue(service2.contains(service1));
    }

    @Test
    public void containsTestPortRangeContainsPortRangeDifferentProtocol() {
	String protocol1 = "TCP";
	String protocol2 = "UDP";
	Service service1 = new Service(protocol1, 1, 8);
	Service service2 = new Service(protocol2, 0, 10);
	assertFalse(service1.contains(service2));
	assertFalse(service2.contains(service1));
    }

    @Test
    public void containsTestPortRangeContainsPortRangeAnyProtocol() {
	String protocol1 = Service.ANY_PROTOCOL;
	String protocol2 = "UDP";
	Service service1 = new Service(protocol1, 1, 18);
	Service service2 = new Service(protocol2, 5, 10);
	assertTrue(service1.contains(service2));
	assertFalse(service2.contains(service1));
    }

    @Test
    public void containsTestContainsItsef() {
	String protocol = "TCP";
	int[] range = getRandomRange();
	Service service1 = new Service(protocol, range[0], range[1]);
	Service service2 = new Service(protocol, range[0], range[1]);
	assertTrue(service1.contains(service1));
	assertTrue(service1.contains(service2));
	assertTrue(service2.contains(service1));
    }

    @Test
    public void containsTestNotContainsNull() {
	String protocol = "TCP";
	int[] range = getRandomRange();
	Service service1 = new Service(protocol, range[0], range[1]);
	Service service2 = null;
	assertFalse(service1.contains(service2));
    }

    @Test
    public void containsTestNotContainsOtherAttributes() {
	String protocol = "TCP";
	int[] range = getRandomRange();
	Service service = new Service(protocol, range[0], range[1]);

	Source source = new Source("1.1.1.1");
	assertFalse(service.contains(source));

	Destination des = new Destination("1.1.1.1");
	assertFalse(service.contains(des));
    }

    @Test
    public void toString_SinglePortSingleProtocol() {
	Service s = new Service("TCP 80");
	assertEquals("TCP 80", s.toString());
    }

    @Test
    public void toString_SinglePortAnyProtocol() {
	Service s = new Service("Port 80");
	assertEquals("Port 80", s.toString());
    }

    @Test
    public void toString_AnyPortSingleProtocol() {
	Service s = new Service("Any TCP");
	assertEquals("Any TCP", s.toString());
    }

    @Test
    public void toString_AnyPortAnyProtocol() {
	Service s = new Service("Any Any");
	assertEquals("Any", s.toString());
    }

    @Test
    public void toString_PortRangeSingleProtocol() {
	Service s = new Service("TCP 80-90");
	assertEquals("TCP 80-90", s.toString());
    }

    @Test
    public void toString_PortRangeAnyProtocol() {
	Service s = new Service("Ports 80-90");
	assertEquals("Ports 80-90", s.toString());
    }

    @Test
    public void shouldEqualIdenticalOne() {
	Service s1, s2;
	s1 = new Service("TCP 80-205");
	s2 = new Service("TCP 80-205");
	assertEquals(s1, s2);
    }
    @Test
    public void shouldNotEqualDifferentOne() {
	Service s1, s2;
	s1 = new Service("Any TCP");
	s2 = new Service("Any UDP");
	assertFalse(s1.equals(s2));
    }
    
    /*--------------------Help Methods--------------------*/

    private int getRandomPort() {
	return rand.nextInt(1 << 16);
    }

    private int[] getRandomRange() {
	int a, b;

	do {
	    a = rand.nextInt(1 << 16);
	    b = rand.nextInt(1 << 16);
	} while (!(a < b));

	return new int[] { a, b };
    }

}
