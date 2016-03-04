package breakingtherules.tests.firewall;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;

public class ServiceTest {

    @Test
    public void constructorTestOnePort() {
	String protocol = "TCP";
	int port = FirewallTestsUtility.getRandomPort();
	new Service(protocol, port);
    }

    @Test
    public void constructorTestOnePortAnyProtocol() {
	String protocol = Service.ANY_PROTOCOL;
	int port = FirewallTestsUtility.getRandomPort();
	new Service(protocol, port);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestOneNegativePort() {
	String protocol = "TCP";
	int port = -2;
	new Service(protocol, port);
	fail("Allowed to create service with negative port");
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestOnePortOver2pow16() {
	String protocol = "TCP";
	int port = 1 << 16;
	new Service(protocol, port);
	fail("Allowed to create service with port over max port");
    }

    @Test
    public void contructorTestPortRange() {
	String protocol = "TCP";
	int range[] = FirewallTestsUtility.getRandomPortRange();
	new Service(protocol, range[0], range[1]);
    }

    @Test
    public void contructorTestPortRangeAnyProtocol() {
	String protocol = Service.ANY_PROTOCOL;
	int range[] = FirewallTestsUtility.getRandomPortRange();
	new Service(protocol, range[0], range[1]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestPortRangeUpperRangeLowerThanLowerRange() {
	String protocol = "TCP";
	int range[] = FirewallTestsUtility.getRandomPortRange();
	new Service(protocol, range[1], range[0]);
	fail("Allowed creation of service with port range and upperRange < lowerRange");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestPortRangeNegativePort() {
	String protocol = "TCP";
	int range[] = FirewallTestsUtility.getRandomPortRange();
	new Service(protocol, -1, range[1]);
	fail("Allowed creation of service with port range and lowerRange < 0");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestPortRangeUpperRangeOver2pow16() {
	String protocol = "TCP";
	int range[] = FirewallTestsUtility.getRandomPortRange();
	new Service(protocol, range[0], 1 << 16);
	fail("Allowed creation of service with port range and upperRange >= 1 << 16");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringNullString() {
	new Service(null);
	fail("Allowed creation of service from string with null String");
    }

    @Test
    public void contructorTestFromStringOnePort() {
	new Service("TCP 80");
    }

    @Test
    public void contructorTestFromStringOnePortAnyProtocol() {
	new Service("Port 80");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringOnePortNoProtocol() {
	new Service("80");
	fail("Allowed creation of service from String without protocol");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringOnePortNoProtocolAndSpace() {
	new Service(" 80");
	fail("Allowed creation of service from String without protocol and space");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringOnePortNegative() {
	new Service("TCP -1");
	fail("Allowed creation of service from String with negative port");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringOnePortOver2pow16() {
	new Service("TCP 65536");
	fail("Allowed creation of service from String with port over 1 << 16");
    }

    @Test
    public void contructorTestFromStringPortRange() {
	new Service("TCP 80-100");
    }

    @Test
    public void contructorTestFromStringPortRangeAnyProtocol() {
	new Service("Ports 80-100");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringPortRangeNoProtocolWithSpace() {
	new Service(" 80-100");
	fail("Allowed creation of service from string with port range with no protocol with space");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringPortRangeUpperRangeUnderLowerRange() {
	new Service("TCP 100-80");
	fail("Allowed creation of service from string with port range upperRange < lowerRange");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringPortRangeNegative() {
	new Service("TCP -1-100");
	fail("Allowed creation of service from string negative port");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringPortRangePortOver2pow16() {
	new Service("TCP 80-65536");
	fail("Allowed creation of service from string with port over 1 << 16");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringPortRangeNaN() {
	new Service("TCP sdw-100");
	fail("Allowed creation of service from string with characters instead of numebr");
    }

    @Test
    public void contructorTestFromStringAnyPort() {
	new Service("Any TCP");
    }

    @Test
    public void contructorTestFromStringAnyPortAnyProtocol() {
	Service s1 = new Service("Any");
	Service s2 = new Service("Any Any");

	assertEquals("Should be same object", s1, s2);
    }

    @Test
    public void getProtocol() {
	String protocol = "TCP";
	Service service;

	int port = FirewallTestsUtility.getRandomPort();
	service = new Service(protocol, port);
	assertTrue(protocol.equals(service.getProtocol()));

	int[] range = FirewallTestsUtility.getRandomPortRange();
	service = new Service(protocol, range[0], range[1]);
	assertTrue(protocol.equals(service.getProtocol()));
    }

    @Test
    public void getProtocolAnyProtocol() {
	String protocol = Service.ANY_PROTOCOL;
	Service service;

	int port = FirewallTestsUtility.getRandomPort();
	service = new Service(protocol, port);
	assertTrue(protocol.equals(service.getProtocol()));

	int[] range = FirewallTestsUtility.getRandomPortRange();
	service = new Service(protocol, range[0], range[1]);
	assertTrue(protocol.equals(service.getProtocol()));
    }

    @Test
    public void getPortRangeStartOnePort() {
	String protocol = "TCP";
	int port = FirewallTestsUtility.getRandomPort();
	Service service = new Service(protocol, port);
	assertEquals(port, service.getPortRangeStart());
    }

    @Test
    public void getPortRangeStartPortRange() {
	String protocol = "TCP";
	int[] range = FirewallTestsUtility.getRandomPortRange();
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
	int port = FirewallTestsUtility.getRandomPort();
	Service service = new Service(protocol, port);
	assertEquals(port, service.getPortRangeEnd());
    }

    @Test
    public void getPortRangeEndPortRange() {
	String protocol = "TCP";
	int[] range = FirewallTestsUtility.getRandomPortRange();
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
	int[] range = FirewallTestsUtility.getRandomPortRange();
	Service service1 = new Service(protocol, range[0], range[1]);
	Service service2 = new Service(protocol, range[0], range[1]);
	assertTrue(service1.contains(service1));
	assertTrue(service1.contains(service2));
	assertTrue(service2.contains(service1));
    }

    @Test
    public void containsTestNotContainsNull() {
	String protocol = "TCP";
	int[] range = FirewallTestsUtility.getRandomPortRange();
	Service service1 = new Service(protocol, range[0], range[1]);
	Service service2 = null;
	assertFalse(service1.contains(service2));
    }

    @Test
    public void containsTestNotContainsOtherAttributes() {
	String protocol = "TCP";
	int[] range = FirewallTestsUtility.getRandomPortRange();
	Service service = new Service(protocol, range[0], range[1]);

	Source source = new Source("1.1.1.1");
	assertFalse(service.contains(source));

	Destination des = new Destination("1.1.1.1");
	assertFalse(service.contains(des));
    }

    @Test
    public void toStringSinglePortSingleProtocol() {
	Service s = new Service("TCP 80");
	assertEquals("TCP 80", s.toString());
    }

    @Test
    public void toStringSinglePortAnyProtocol() {
	Service s = new Service("Port 80");
	assertEquals("Port 80", s.toString());
    }

    @Test
    public void toStringAnyPortSingleProtocol() {
	Service s = new Service("Any TCP");
	assertEquals("Any TCP", s.toString());
    }

    @Test
    public void toStringAnyPortAnyProtocol() {
	Service s = new Service("Any Any");
	assertEquals("Any", s.toString());
    }

    @Test
    public void toStringPortRangeSingleProtocol() {
	Service s = new Service("TCP 80-90");
	assertEquals("TCP 80-90", s.toString());
    }

    @Test
    public void toStringPortRangeAnyProtocol() {
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

}
