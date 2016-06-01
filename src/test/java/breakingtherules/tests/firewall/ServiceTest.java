package breakingtherules.tests.firewall;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;
import breakingtherules.tests.TestBase;

public class ServiceTest extends TestBase {

    @Test
    public void constructorTestOnePort() {
	System.out.println("# ServiceTest constructorTestOnePort");
	String protocol = "TCP";
	int port = FirewallTestsUtility.getRandomPort();
	Service.create(protocol, port);
    }

    @Test
    public void constructorTestOnePortAnyProtocol() {
	System.out.println("# ServiceTest constructorTestOnePortAnyProtocol");
	int protocol = Service.ANY_PROTOCOL;
	int port = FirewallTestsUtility.getRandomPort();
	Service.create(protocol, port);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestOneNegativePort() {
	System.out.println("# ServiceTest constructorTestOneNegativePort");
	String protocol = "TCP";
	int port = -2;
	Service.create(protocol, port);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestOnePortOver2pow16() {
	System.out.println("# ServiceTest constructorTestOnePortOver2pow16");
	String protocol = "TCP";
	int port = 1 << 16;
	Service.create(protocol, port);
    }

    @Test
    public void contructorTestPortRange() {
	System.out.println("# ServiceTest contructorTestPortRange");
	String protocol = "TCP";
	int range[] = FirewallTestsUtility.getRandomPortRange();
	Service.create(protocol, range[0], range[1]);
    }

    @Test
    public void contructorTestPortRangeAnyProtocol() {
	System.out.println("# ServiceTest contructorTestPortRangeAnyProtocol");
	int protocol = Service.ANY_PROTOCOL;
	int range[] = FirewallTestsUtility.getRandomPortRange();
	Service.create(protocol, range[0], range[1]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestPortRangeUpperRangeLowerThanLowerRange() {
	System.out.println("# ServiceTest contructorTestPortRangeUpperRangeLowerThanLowerRange");
	String protocol = "TCP";
	int range[] = FirewallTestsUtility.getRandomPortRange();
	Service.create(protocol, range[1], range[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestPortRangeNegativePort() {
	System.out.println("# ServiceTest contructorTestPortRangeNegativePort");
	String protocol = "TCP";
	int range[] = FirewallTestsUtility.getRandomPortRange();
	Service.create(protocol, -1, range[1]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestPortRangeUpperRangeOver2pow16() {
	System.out.println("# ServiceTest contructorTestPortRangeUpperRangeOver2pow16");
	String protocol = "TCP";
	int range[] = FirewallTestsUtility.getRandomPortRange();
	Service.create(protocol, range[0], 1 << 16);
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringNullString() {
	System.out.println("# ServiceTest contructorTestFromStringNullString");
	Service.create(null);
    }

    @Test
    public void contructorTestFromStringOnePort() {
	System.out.println("# ServiceTest contructorTestFromStringOnePort");
	Service.create("TCP 80");
    }

    @Test
    public void contructorTestFromStringOnePortAnyProtocol() {
	System.out.println("# ServiceTest contructorTestFromStringOnePortAnyProtocol");
	Service.create("Port 80");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringOnePortNoProtocol() {
	System.out.println("# ServiceTest contructorTestFromStringOnePortNoProtocol");
	Service.create("80");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringOnePortNoProtocolAndSpace() {
	System.out.println("# ServiceTest contructorTestFromStringOnePortNoProtocolAndSpace");
	Service.create(" 80");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringOnePortNegative() {
	System.out.println("# ServiceTest contructorTestFromStringOnePortNegative");
	Service.create("TCP -1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringOnePortOver2pow16() {
	System.out.println("# ServiceTest contructorTestFromStringOnePortOver2pow16");
	Service.create("TCP 65536");
    }

    @Test
    public void contructorTestFromStringPortRange() {
	System.out.println("# ServiceTest contructorTestFromStringPortRange");
	Service.create("TCP 80-100");
    }

    @Test
    public void contructorTestFromStringPortRangeAnyProtocol() {
	System.out.println("# ServiceTest contructorTestFromStringPortRangeAnyProtocol");
	Service.create("Ports 80-100");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringPortRangeNoProtocolWithSpace() {
	System.out.println("# ServiceTest contructorTestFromStringPortRangeNoProtocolWithSpace");
	Service.create(" 80-100");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringPortRangeUpperRangeUnderLowerRange() {
	System.out.println("# ServiceTest contructorTestFromStringPortRangeUpperRangeUnderLowerRange");
	Service.create("TCP 100-80");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringPortRangeNegative() {
	System.out.println("# ServiceTest contructorTestFromStringPortRangeNegative");
	Service.create("TCP -1-100");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringPortRangePortOver2pow16() {
	System.out.println("# ServiceTest contructorTestFromStringPortRangePortOver2pow16");
	Service.create("TCP 80-65536");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringPortRangeNaN() {
	System.out.println("# ServiceTest contructorTestFromStringPortRangeNaN");
	Service.create("TCP sdw-100");
    }

    @Test
    public void contructorTestFromStringAnyPort() {
	System.out.println("# ServiceTest contructorTestFromStringAnyPort");
	Service.create("Any TCP");
    }

    @Test
    public void contructorTestFromStringAnyPortAnyProtocol() {
	System.out.println("# ServiceTest contructorTestFromStringAnyPortAnyProtocol");
	Service s1 = Service.create("Any");
	Service s2 = Service.create("Any Any");

	assertEquals("Should be same object", s1, s2);
    }

    @Test
    public void getProtocolTest() {
	System.out.println("# ServiceTest getProtocolTest");
	String protocol = "TCP";
	Service service;

	int port = FirewallTestsUtility.getRandomPort();
	service = Service.create(protocol, port);
	assertTrue(protocol.equals(service.getProtocol()));

	int[] range = FirewallTestsUtility.getRandomPortRange();
	service = Service.create(protocol, range[0], range[1]);
	assertEquals(protocol, service.getProtocol());
    }

    @Test
    public void getProtocolTestAnyProtocol() {
	System.out.println("# ServiceTest getProtocolTestAnyProtocol");
	int protocol = Service.ANY_PROTOCOL;
	Service service;

	int port = FirewallTestsUtility.getRandomPort();
	service = Service.create(protocol, port);
	assertTrue(protocol == service.getProtocolCode());

	int[] range = FirewallTestsUtility.getRandomPortRange();
	service = Service.create(protocol, range[0], range[1]);
	assertEquals(protocol, service.getProtocolCode());
    }

    @Test
    public void getPortRangeStartTestOnePort() {
	System.out.println("# ServiceTest getPortRangeStartTestOnePort");
	String protocol = "TCP";
	int port = FirewallTestsUtility.getRandomPort();
	Service service = Service.create(protocol, port);
	assertEquals(port, service.getPortRangeStart());
    }

    @Test
    public void getPortRangeStartTestPortRange() {
	System.out.println("# ServiceTest getPortRangeStartTestPortRange");
	String protocol = "TCP";
	int[] range = FirewallTestsUtility.getRandomPortRange();
	Service service = Service.create(protocol, range[0], range[1]);
	assertEquals(range[0], service.getPortRangeStart());
    }

    @Test
    public void getPortRangeStartTestOnePortFromString() {
	System.out.println("# ServiceTest getPortRangeStartTestOnePortFromString");
	Service service = Service.create("TCP 50");
	assertEquals(50, service.getPortRangeStart());
    }

    @Test
    public void getPortRangeStartTestPortRangeFromString() {
	System.out.println("# ServiceTest getPortRangeStartTestPortRangeFromString");
	Service service = Service.create("TCP 50-70");
	assertEquals(50, service.getPortRangeStart());
    }

    @Test
    public void getPortRangeEndTestOnePort() {
	System.out.println("# ServiceTest getPortRangeEndTestOnePort");
	String protocol = "TCP";
	int port = FirewallTestsUtility.getRandomPort();
	Service service = Service.create(protocol, port);
	assertEquals(port, service.getPortRangeEnd());
    }

    @Test
    public void getPortRangeEndTestPortRange() {
	System.out.println("# ServiceTest getPortRangeEndTestPortRange");
	String protocol = "TCP";
	int[] range = FirewallTestsUtility.getRandomPortRange();
	Service service = Service.create(protocol, range[0], range[1]);
	assertEquals(range[1], service.getPortRangeEnd());
    }

    @Test
    public void getPortRangeEndTestOnePortFromString() {
	System.out.println("# ServiceTest getPortRangeEndTestOnePortFromString");
	Service service = Service.create("TCP 70");
	assertEquals(70, service.getPortRangeEnd());
    }

    @Test
    public void getPortRangeEndTestPortRangeFromString() {
	System.out.println("# ServiceTest getPortRangeEndTestPortRangeFromString");
	Service service = Service.create("TCP 50-70");
	assertEquals(70, service.getPortRangeEnd());
    }

    @Test
    public void containsTestPortRangeContainsOnePort() {
	System.out.println("# ServiceTest containsTestPortRangeContainsOnePort");
	String protocol = "TCP";
	Service service1 = Service.create(protocol, 1);
	Service service2 = Service.create(protocol, 0, 10);
	assertFalse(service1.contains(service2));
	assertTrue(service2.contains(service1));
    }

    @Test
    public void containsTestPortRangeContainsPortRange() {
	System.out.println("# ServiceTest containsTestPortRangeContainsPortRange");
	String protocol = "TCP";
	Service service1 = Service.create(protocol, 1, 8);
	Service service2 = Service.create(protocol, 0, 10);
	assertFalse(service1.contains(service2));
	assertTrue(service2.contains(service1));
    }

    @Test
    public void containsTestPortRangeContainsPortRangeDifferentProtocol() {
	System.out.println("# ServiceTest containsTestPortRangeContainsPortRangeDifferentProtocol");
	String protocol1 = "TCP";
	String protocol2 = "UDP";
	Service service1 = Service.create(protocol1, 1, 8);
	Service service2 = Service.create(protocol2, 0, 10);
	assertFalse(service1.contains(service2));
	assertFalse(service2.contains(service1));
    }

    @Test
    public void containsTestPortRangeContainsPortRangeAnyProtocol() {
	System.out.println("# ServiceTest containsTestPortRangeContainsPortRangeAnyProtocol");
	int protocol1 = Service.ANY_PROTOCOL;
	String protocol2 = "UDP";
	Service service1 = Service.create(protocol1, 1, 18);
	Service service2 = Service.create(protocol2, 5, 10);
	assertTrue(service1.contains(service2));
	assertFalse(service2.contains(service1));
    }

    @Test
    public void containsTestContainsItsef() {
	System.out.println("# ServiceTest containsTestContainsItsef");
	String protocol = "TCP";
	int[] range = FirewallTestsUtility.getRandomPortRange();
	Service service1 = Service.create(protocol, range[0], range[1]);
	Service service2 = Service.create(protocol, range[0], range[1]);
	assertTrue(service1.contains(service1));
	assertTrue(service1.contains(service2));
	assertTrue(service2.contains(service1));
    }

    @Test
    public void containsTestNotContainsNull() {
	System.out.println("# ServiceTest containsTestNotContainsNull");
	String protocol = "TCP";
	int[] range = FirewallTestsUtility.getRandomPortRange();
	Service service1 = Service.create(protocol, range[0], range[1]);
	Service service2 = null;
	assertFalse(service1.contains(service2));
    }

    @Test
    public void containsTestNotContainsOtherAttributes() {
	System.out.println("# ServiceTest containsTestNotContainsOtherAttributes");
	String protocol = "TCP";
	int[] range = FirewallTestsUtility.getRandomPortRange();
	Service service = Service.create(protocol, range[0], range[1]);

	Source source = Source.create("1.1.1.1");
	assertFalse(service.contains(source));

	Destination des = Destination.create("1.1.1.1");
	assertFalse(service.contains(des));
    }

    @Test
    public void toStringTestSinglePortSingleProtocol() {
	System.out.println("# ServiceTest toStringTestSinglePortSingleProtocol");
	Service s = Service.create("TCP 80");
	assertEquals("TCP 80", s.toString());
    }

    @Test
    public void toStringTestSinglePortAnyProtocol() {
	System.out.println("# ServiceTest toStringTestSinglePortAnyProtocol");
	Service s = Service.create("Port 80");
	assertEquals("Port 80", s.toString());
    }

    @Test
    public void toStringTestAnyPortSingleProtocol() {
	System.out.println("# ServiceTest toStringTestAnyPortSingleProtocol");
	Service s = Service.create("Any TCP");
	assertEquals("Any TCP", s.toString());
    }

    @Test
    public void toStringTestAnyPortAnyProtocol() {
	System.out.println("# ServiceTest toStringTestAnyPortAnyProtocol");
	Service s = Service.create("Any Any");
	assertEquals("Any", s.toString());
    }

    @Test
    public void toStringTestPortRangeSingleProtocol() {
	System.out.println("# ServiceTest toStringTestPortRangeSingleProtocol");
	Service s = Service.create("TCP 80-90");
	assertEquals("TCP 80-90", s.toString());
    }

    @Test
    public void toStringTestPortRangeAnyProtocol() {
	System.out.println("# ServiceTest toStringTestPortRangeAnyProtocol");
	Service s = Service.create("Ports 80-90");
	assertEquals("Ports 80-90", s.toString());
    }

    @Test
    public void equalsTestTrue() {
	System.out.println("# ServiceTest equalsTestTrue");
	Service s1, s2;
	s1 = Service.create("TCP 80-205");
	s2 = Service.create("TCP 80-205");
	assertEquals(s1, s2);
    }

    @Test
    public void equalsTestFalse() {
	System.out.println("# ServiceTest equalsTestFalse");
	Service s1, s2;
	s1 = Service.create("Any TCP");
	s2 = Service.create("Any UDP");
	assertNotEquals(s1, s2);
    }

}
