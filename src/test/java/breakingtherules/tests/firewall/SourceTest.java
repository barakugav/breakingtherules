package breakingtherules.tests.firewall;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import breakingtherules.firewall.Destination;
import breakingtherules.firewall.IP;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;

public class SourceTest {

    @Test
    public void constructorTest() {
	IP ip = FirewallTestsUtility.getRandomIP();
	new Source(ip);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestNullIP() {
	IP ip = null;
	new Source(ip);
	fail("Allowed Source creation will null address arg");
    }

    @Test
    public void constructorStringTest() {
	String ip = "2.12.45.7/21";
	new Source(ip);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorStringTestNullIP() {
	String ip = null;
	new Source(ip);
	fail("Allowed Source creation will null address arg");
    }

    @Test
    public void getIPTest() {
	IP ip = FirewallTestsUtility.getRandomIP();
	Source source = new Source(ip);
	assertEquals(ip, source.getIp());
    }

    @Test
    public void containsTestContainsItsef() {
	IP ip = FirewallTestsUtility.getRandomIP();
	Source source1 = new Source(ip);
	Source source2 = new Source(ip);
	assertTrue(source1.contains(source2));
	assertTrue(source2.contains(source1));
    }

    @Test
    public void containsTestNotContainsNull() {
	IP ip = FirewallTestsUtility.getRandomIP();
	Source source1 = new Source(ip);
	Source source2 = null;
	assertFalse(source1.contains(source2));
    }

    @Test
    public void containsTestNotContainsOtherAttributes() {
	IP ip = FirewallTestsUtility.getRandomIP();
	Source source = new Source(ip);

	Destination des = new Destination(ip);
	assertFalse(source.contains(des));

	Service service = new Service("TCP", 80);
	assertFalse(source.contains(service));
    }

    @Test
    public void containsTestOtherDestination() {
	IP ip1 = FirewallTestsUtility.getRandomIP();
	IP ip2 = FirewallTestsUtility.getRandomIP();
	Source source1 = new Source(ip1);
	Source source2 = new Source(ip2);
	assertEquals(ip1.contains(ip2), source1.contains(source2));
    }

    @Test
    public void shouldEqualIdenticalOne() {
	Source s1, s2;
	s1 = new Source("2.12.45.7/21");
	s2 = new Source("2.12.45.7/21");
	assertEquals(s1, s2);
    }

    @Test
    public void shouldNotEqualDifferentOne() {
	Source s1, s2;
	s1 = new Source("2.12.45.7/21");
	s2 = new Source("2.13.45.7/21");
	assertFalse(s1.equals(s2));
    }

    @Test
    public void toStringTest() {
	IP ip = FirewallTestsUtility.getRandomIP();
	Source des = new Source(ip);
	assertTrue(ip.toString().equals(des.toString()));
    }

}
