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

public class DestinationTest extends TestBase {

    @Test
    public void constructorTest() {
	IP ip = FirewallTestsUtility.getRandomIP();
	Destination.create(ip);
    }

    @Test(expected = NullPointerException.class)
    public void constructorTestNullIP() {
	IP ip = null;
	Destination.create(ip);
    }

    @Test
    public void constructorStringTest() {
	String ip = "2.12.45.7/21";
	Destination.create(ip);
    }

    @Test(expected = NullPointerException.class)
    public void constructorStringTestNullIP() {
	String ip = null;
	Destination.create(ip);
    }

    @Test
    public void getIPTest() {
	IP ip = FirewallTestsUtility.getRandomIP();
	Destination des = Destination.create(ip);
	assertEquals(ip, des.getIp());
    }

    @Test
    public void containsTestContainsItsef() {
	IP ip = FirewallTestsUtility.getRandomIP();
	Destination des1 = Destination.create(ip);
	Destination des2 = Destination.create(ip);
	assertTrue(des1.contains(des1));
	assertTrue(des1.contains(des2));
	assertTrue(des2.contains(des1));
    }

    @Test
    public void containsTestNotContainsNull() {
	IP ip = FirewallTestsUtility.getRandomIP();
	Destination des1 = Destination.create(ip);
	Destination des2 = null;
	assertFalse(des1.contains(des2));
    }

    @Test
    public void containsTestNotContainsOtherAttributes() {
	IP ip = FirewallTestsUtility.getRandomIP();
	Destination des = Destination.create(ip);

	Source source = Source.create(ip);
	assertFalse(des.contains(source));

	Service service = Service.create("TCP", 80);
	assertFalse(des.contains(service));
    }

    @Test
    public void containsTestOtherDestination() {
	IP ip1 = FirewallTestsUtility.getRandomIP();
	IP ip2 = FirewallTestsUtility.getRandomIP();
	Destination des1 = Destination.create(ip1);
	Destination des2 = Destination.create(ip2);
	assertEquals(ip1.contains(ip2), des1.contains(des2));
    }

    @Test
    public void toStringTest() {
	IP ip = FirewallTestsUtility.getRandomIP();
	Destination des = Destination.create(ip);
	assertTrue(ip.toString().equals(des.toString()));
    }

    @Test
    public void equalsTestTrue() {
	Destination s1, s2;
	s1 = Destination.create("2.12.45.7/21");
	s2 = Destination.create("2.12.45.7/21");
	assertEquals(s1, s2);
    }

    @Test
    public void equalsTestFalse() {
	Destination s1, s2;
	s1 = Destination.create("2.12.45.7/21");
	s2 = Destination.create("2.13.45.7/21");
	assertNotEquals(s1, s2);
    }

}
