package breakingtherules.tests.firewall;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import breakingtherules.firewall.Destination;
import breakingtherules.firewall.IP;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;
import breakingtherules.tests.TestBase;

@SuppressWarnings("javadoc")
public class DestinationTest extends TestBase {

    @Test
    public void constructorStringTest() {
	final String ip = "2.12.45.7/21";
	Destination.valueOf(ip);
    }

    @Test(expected = NullPointerException.class)
    public void constructorStringTestNullIP() {
	final String ip = null;
	Destination.valueOf(ip);
    }

    @Test
    public void constructorTest() {
	final IP ip = FirewallTestsUtility.getRandomIP();
	Destination.valueOf(ip);
    }

    @Test(expected = NullPointerException.class)
    public void constructorTestNullIP() {
	final IP ip = null;
	Destination.valueOf(ip);
    }

    @Test
    public void containsTestContainsItsef() {
	final IP ip = FirewallTestsUtility.getRandomIP();
	final Destination des1 = Destination.valueOf(ip);
	final Destination des2 = Destination.valueOf(ip);
	assertTrue(des1.contains(des1));
	assertTrue(des1.contains(des2));
	assertTrue(des2.contains(des1));
    }

    @Test
    public void containsTestNotContainsNull() {
	final IP ip = FirewallTestsUtility.getRandomIP();
	final Destination des1 = Destination.valueOf(ip);
	final Destination des2 = null;
	assertFalse(des1.contains(des2));
    }

    @Test
    public void containsTestNotContainsOtherAttributes() {
	final IP ip = FirewallTestsUtility.getRandomIP();
	final Destination des = Destination.valueOf(ip);

	final Source source = Source.valueOf(ip);
	assertFalse(des.contains(source));

	final Service service = Service.valueOf(Service.protocolCode("TCP"), 80);
	assertFalse(des.contains(service));
    }

    @Test
    public void containsTestOtherDestination() {
	final IP ip1 = FirewallTestsUtility.getRandomIP();
	final IP ip2 = FirewallTestsUtility.getRandomIP();
	final Destination des1 = Destination.valueOf(ip1);
	final Destination des2 = Destination.valueOf(ip2);
	assertEquals(ip1.contains(ip2), des1.contains(des2));
    }

    @Test
    public void equalsTestFalse() {
	Destination s1, s2;
	s1 = Destination.valueOf("2.12.45.7/21");
	s2 = Destination.valueOf("2.13.45.7/21");
	assertNotEquals(s1, s2);
    }

    @Test
    public void equalsTestTrue() {
	Destination s1, s2;
	s1 = Destination.valueOf("2.12.45.7/21");
	s2 = Destination.valueOf("2.12.45.7/21");
	assertEquals(s1, s2);
    }

    @Test
    public void getIPTest() {
	final IP ip = FirewallTestsUtility.getRandomIP();
	final Destination des = Destination.valueOf(ip);
	assertEquals(ip, des.getIp());
    }

    @Test
    public void toStringTest() {
	final IP ip = FirewallTestsUtility.getRandomIP();
	final Destination des = Destination.valueOf(ip);
	assertTrue(ip.toString().equals(des.toString()));
    }

}
