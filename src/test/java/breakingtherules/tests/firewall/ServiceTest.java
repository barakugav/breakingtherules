package breakingtherules.tests.firewall;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;
import breakingtherules.tests.TestBase;

@SuppressWarnings("javadoc")
public class ServiceTest extends TestBase {

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestOneNegativePort() {
	final String protocol = "TCP";
	final int port = -2;
	Service.valueOf(Service.protocolCode(protocol), port);
    }

    @Test
    public void constructorTestOnePort() {
	final String protocol = "TCP";
	final int port = FirewallTestsUtility.getRandomPort();
	Service.valueOf(Service.protocolCode(protocol), port);
    }

    @Test
    public void constructorTestOnePortAnyProtocol() {
	final short protocol = Service.ANY_PROTOCOL;
	final int port = FirewallTestsUtility.getRandomPort();
	Service.valueOf(protocol, port);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestOnePortOver2pow16() {
	final String protocol = "TCP";
	final int port = 1 << 16;
	Service.valueOf(Service.protocolCode(protocol), port);
    }

    @Test
    public void containsTestContainsItsef() {
	final String protocol = "TCP";
	final int[] range = FirewallTestsUtility.getRandomPortRange();
	final Service service1 = Service.valueOf(Service.protocolCode(protocol), range[0], range[1]);
	final Service service2 = Service.valueOf(Service.protocolCode(protocol), range[0], range[1]);
	assertTrue(service1.contains(service1));
	assertTrue(service1.contains(service2));
	assertTrue(service2.contains(service1));
    }

    @Test
    public void containsTestNotContainsNull() {
	final String protocol = "TCP";
	final int[] range = FirewallTestsUtility.getRandomPortRange();
	final Service service1 = Service.valueOf(Service.protocolCode(protocol), range[0], range[1]);
	final Service service2 = null;
	assertFalse(service1.contains(service2));
    }

    @Test
    public void containsTestNotContainsOtherAttributes() {
	final String protocol = "TCP";
	final int[] range = FirewallTestsUtility.getRandomPortRange();
	final Service service = Service.valueOf(Service.protocolCode(protocol), range[0], range[1]);

	final Source source = Source.valueOf("1.1.1.1");
	assertFalse(service.contains(source));

	final Destination des = Destination.valueOf("1.1.1.1");
	assertFalse(service.contains(des));
    }

    @Test
    public void containsTestPortRangeContainsOnePort() {
	final String protocol = "TCP";
	final Service service1 = Service.valueOf(Service.protocolCode(protocol), 1);
	final Service service2 = Service.valueOf(Service.protocolCode(protocol), 0, 10);
	assertFalse(service1.contains(service2));
	assertTrue(service2.contains(service1));
    }

    @Test
    public void containsTestPortRangeContainsPortRange() {
	final String protocol = "TCP";
	final Service service1 = Service.valueOf(Service.protocolCode(protocol), 1, 8);
	final Service service2 = Service.valueOf(Service.protocolCode(protocol), 0, 10);
	assertFalse(service1.contains(service2));
	assertTrue(service2.contains(service1));
    }

    @Test
    public void containsTestPortRangeContainsPortRangeAnyProtocol() {
	final short protocol1 = Service.ANY_PROTOCOL;
	final String protocol2 = "UDP";
	final Service service1 = Service.valueOf(protocol1, 1, 18);
	final Service service2 = Service.valueOf(Service.protocolCode(protocol2), 5, 10);
	assertTrue(service1.contains(service2));
	assertFalse(service2.contains(service1));
    }

    @Test
    public void containsTestPortRangeContainsPortRangeDifferentProtocol() {
	final String protocol1 = "TCP";
	final String protocol2 = "UDP";
	final Service service1 = Service.valueOf(Service.protocolCode(protocol1), 1, 8);
	final Service service2 = Service.valueOf(Service.protocolCode(protocol2), 0, 10);
	assertFalse(service1.contains(service2));
	assertFalse(service2.contains(service1));
    }

    @Test
    public void contructorTestFromStringAnyPort() {
	Service.valueOf("TCP Any");
    }

    @Test
    public void contructorTestFromStringAnyPortAnyProtocol() {
	final Service s1 = Service.valueOf("Any");
	final Service s2 = Service.valueOf("Any Any");

	assertEquals("Should be same object", s1, s2);
    }

    @Test(expected = NullPointerException.class)
    public void contructorTestFromStringNullString() {
	Service.valueOf(null);
    }

    @Test
    public void contructorTestFromStringOnePort() {
	Service.valueOf("TCP 80");
    }

    @Test
    public void contructorTestFromStringOnePortAnyProtocol() {
	Service.valueOf("Any 80");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringOnePortNegative() {
	Service.valueOf("TCP -1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringOnePortNoProtocol() {
	Service.valueOf("80");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringOnePortNoProtocolAndSpace() {
	Service.valueOf(" 80");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringOnePortOver2pow16() {
	Service.valueOf("TCP 65536");
    }

    @Test
    public void contructorTestFromStringPortRange() {
	Service.valueOf("TCP 80-100");
    }

    @Test
    public void contructorTestFromStringPortRangeAnyProtocol() {
	Service.valueOf("Any 80-100");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringPortRangeNaN() {
	Service.valueOf("TCP sdw-100");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringPortRangeNegative() {
	Service.valueOf("TCP -1-100");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringPortRangeNoProtocolWithSpace() {
	Service.valueOf(" 80-100");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringPortRangePortOver2pow16() {
	Service.valueOf("TCP 80-65536");
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestFromStringPortRangeUpperRangeUnderLowerRange() {
	Service.valueOf("TCP 100-80");
    }

    @Test
    public void contructorTestPortRange() {
	final String protocol = "TCP";
	final int range[] = FirewallTestsUtility.getRandomPortRange();
	Service.valueOf(Service.protocolCode(protocol), range[0], range[1]);
    }

    @Test
    public void contructorTestPortRangeAnyProtocol() {
	final short protocol = Service.ANY_PROTOCOL;
	final int range[] = FirewallTestsUtility.getRandomPortRange();
	Service.valueOf(protocol, range[0], range[1]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestPortRangeNegativePort() {
	final String protocol = "TCP";
	final int range[] = FirewallTestsUtility.getRandomPortRange();
	Service.valueOf(Service.protocolCode(protocol), -1, range[1]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestPortRangeUpperRangeLowerThanLowerRange() {
	final String protocol = "TCP";
	final int range[] = FirewallTestsUtility.getRandomPortRange();
	Service.valueOf(Service.protocolCode(protocol), range[1], range[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void contructorTestPortRangeUpperRangeOver2pow16() {
	final String protocol = "TCP";
	final int range[] = FirewallTestsUtility.getRandomPortRange();
	Service.valueOf(Service.protocolCode(protocol), range[0], 1 << 16);
    }

    @Test
    public void equalsTestFalse() {
	Service s1, s2;
	s1 = Service.valueOf("TCP Any");
	s2 = Service.valueOf("UDP Any");
	assertNotEquals(s1, s2);
    }

    @Test
    public void equalsTestTrue() {
	Service s1, s2;
	s1 = Service.valueOf("TCP 80-205");
	s2 = Service.valueOf("TCP 80-205");
	assertEquals(s1, s2);
    }

    @Test
    public void getPortRangeEndTestOnePort() {
	final String protocol = "TCP";
	final int port = FirewallTestsUtility.getRandomPort();
	final Service service = Service.valueOf(Service.protocolCode(protocol), port);
	assertEquals(port, service.getPortRangeEnd());
    }

    @Test
    public void getPortRangeEndTestOnePortFromString() {
	final Service service = Service.valueOf("TCP 70");
	assertEquals(70, service.getPortRangeEnd());
    }

    @Test
    public void getPortRangeEndTestPortRange() {
	final String protocol = "TCP";
	final int[] range = FirewallTestsUtility.getRandomPortRange();
	final Service service = Service.valueOf(Service.protocolCode(protocol), range[0], range[1]);
	assertEquals(range[1], service.getPortRangeEnd());
    }

    @Test
    public void getPortRangeEndTestPortRangeFromString() {
	final Service service = Service.valueOf("TCP 50-70");
	assertEquals(70, service.getPortRangeEnd());
    }

    @Test
    public void getPortRangeStartTestOnePort() {
	final String protocol = "TCP";
	final int port = FirewallTestsUtility.getRandomPort();
	final Service service = Service.valueOf(Service.protocolCode(protocol), port);
	assertEquals(port, service.getPortRangeStart());
    }

    @Test
    public void getPortRangeStartTestOnePortFromString() {
	final Service service = Service.valueOf("TCP 50");
	assertEquals(50, service.getPortRangeStart());
    }

    @Test
    public void getPortRangeStartTestPortRange() {
	final String protocol = "TCP";
	final int[] range = FirewallTestsUtility.getRandomPortRange();
	final Service service = Service.valueOf(Service.protocolCode(protocol), range[0], range[1]);
	assertEquals(range[0], service.getPortRangeStart());
    }

    @Test
    public void getPortRangeStartTestPortRangeFromString() {
	final Service service = Service.valueOf("TCP 50-70");
	assertEquals(50, service.getPortRangeStart());
    }

    @Test
    public void getProtocolTest() {
	final String protocol = "TCP";
	Service service;

	final int port = FirewallTestsUtility.getRandomPort();
	service = Service.valueOf(Service.protocolCode(protocol), port);
	assertTrue(protocol.equals(service.getProtocol()));

	final int[] range = FirewallTestsUtility.getRandomPortRange();
	service = Service.valueOf(Service.protocolCode(protocol), range[0], range[1]);
	assertEquals(protocol, service.getProtocol());
    }

    @Test
    public void getProtocolTestAnyProtocol() {
	final short protocol = Service.ANY_PROTOCOL;
	Service service;

	final int port = FirewallTestsUtility.getRandomPort();
	service = Service.valueOf(protocol, port);
	assertTrue(protocol == service.getProtocolCode());

	final int[] range = FirewallTestsUtility.getRandomPortRange();
	service = Service.valueOf(protocol, range[0], range[1]);
	assertEquals(protocol, service.getProtocolCode());
    }

    @Test
    public void toStringTestAnyPortAnyProtocol() {
	final Service s = Service.valueOf("Any Any");
	assertEquals("Any", s.toString());
    }

    @Test
    public void toStringTestAnyPortSingleProtocol() {
	final Service s = Service.valueOf("TCP Any");
	assertEquals("TCP Any", s.toString());
    }

    @Test
    public void toStringTestPortRangeAnyProtocol() {
	final Service s = Service.valueOf("Any 80-90");
	assertEquals("Any 80-90", s.toString());
    }

    @Test
    public void toStringTestPortRangeSingleProtocol() {
	final Service s = Service.valueOf("TCP 80-90");
	assertEquals("TCP 80-90", s.toString());
    }

    @Test
    public void toStringTestSinglePortAnyProtocol() {
	final Service s = Service.valueOf("Any 80");
	assertEquals("Any 80", s.toString());
    }

    @Test
    public void toStringTestSinglePortSingleProtocol() {
	final Service s = Service.valueOf("TCP 80");
	assertEquals("TCP 80", s.toString());
    }

}
