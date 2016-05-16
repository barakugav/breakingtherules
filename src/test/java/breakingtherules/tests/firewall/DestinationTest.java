package breakingtherules.tests.firewall;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import breakingtherules.firewall.Destination;
import breakingtherules.firewall.IP;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;

public class DestinationTest {

    @Test
    public void constructorTest() {
	System.out.println("# DestinationTest constructorTest");
	IP ip = FirewallTestsUtility.getRandomIP();
	new Destination(ip);
    }

    @Test(expected = NullPointerException.class)
    public void constructorTestNullIP() {
	System.out.println("# DestinationTest constructorTestNullIP");
	IP ip = null;
	new Destination(ip);
    }

    @Test
    public void constructorStringTest() {
	System.out.println("# DestinationTest constructorStringTest");
	String ip = "2.12.45.7/21";
	new Destination(ip);
    }

    @Test(expected = NullPointerException.class)
    public void constructorStringTestNullIP() {
	System.out.println("# DestinationTest constructorStringTestNullIP");
	String ip = null;
	new Destination(ip);
    }

    @Test
    public void getIPTest() {
	System.out.println("# DestinationTest getIPTest");
	IP ip = FirewallTestsUtility.getRandomIP();
	Destination des = new Destination(ip);
	assertEquals(ip, des.getIp());
    }

    @Test
    public void containsTestContainsItsef() {
	System.out.println("# DestinationTest containsTestContainsItsef");
	IP ip = FirewallTestsUtility.getRandomIP();
	Destination des1 = new Destination(ip);
	Destination des2 = new Destination(ip);
	assertTrue(des1.contains(des1));
	assertTrue(des1.contains(des2));
	assertTrue(des2.contains(des1));
    }

    @Test
    public void containsTestNotContainsNull() {
	System.out.println("# DestinationTest containsTestNotContainsNull");
	IP ip = FirewallTestsUtility.getRandomIP();
	Destination des1 = new Destination(ip);
	Destination des2 = null;
	assertFalse(des1.contains(des2));
    }

    @Test
    public void containsTestNotContainsOtherAttributes() {
	System.out.println("# DestinationTest containsTestNotContainsOtherAttributes");
	IP ip = FirewallTestsUtility.getRandomIP();
	Destination des = new Destination(ip);

	Source source = new Source(ip);
	assertFalse(des.contains(source));

	Service service = new Service("TCP", 80);
	assertFalse(des.contains(service));
    }

    @Test
    public void containsTestOtherDestination() {
	System.out.println("# DestinationTest containsTestOtherDestination");
	IP ip1 = FirewallTestsUtility.getRandomIP();
	IP ip2 = FirewallTestsUtility.getRandomIP();
	Destination des1 = new Destination(ip1);
	Destination des2 = new Destination(ip2);
	assertEquals(ip1.contains(ip2), des1.contains(des2));
    }

    @Test
    public void toStringTest() {
	System.out.println("# DestinationTest toStringTest");
	IP ip = FirewallTestsUtility.getRandomIP();
	Destination des = new Destination(ip);
	assertTrue(ip.toString().equals(des.toString()));
    }

    @Test
    public void equalsTestTrue() {
	System.out.println("# DestinationTest equalsTestTrue");
	Destination s1, s2;
	s1 = new Destination("2.12.45.7/21");
	s2 = new Destination("2.12.45.7/21");
	assertEquals(s1, s2);
    }

    @Test
    public void equalsTestFalse() {
	System.out.println("# DestinationTest equalsTestFalse");
	Destination s1, s2;
	s1 = new Destination("2.12.45.7/21");
	s2 = new Destination("2.13.45.7/21");
	assertNotEquals(s1, s2);
    }

}
