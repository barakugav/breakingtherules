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
public class SourceTest extends TestBase {

    @Test
    public void constructorTest() {
	IP ip = FirewallTestsUtility.getRandomIP();
	Source.valueOf(ip);
    }

    @Test(expected = NullPointerException.class)
    public void constructorTestNullIP() {
	IP ip = null;
	Source.valueOf(ip);
    }

    @Test
    public void constructorStringTest() {
	String ip = "2.12.45.7/21";
	Source.valueOf(ip);
    }

    @Test(expected = NullPointerException.class)
    public void constructorStringTestNullIP() {
	String ip = null;
	Source.valueOf(ip);
    }

    @Test
    public void getIPTest() {
	IP ip = FirewallTestsUtility.getRandomIP();
	Source source = Source.valueOf(ip);
	assertEquals(ip, source.getIp());
    }

    @Test
    public void containsTestContainsItsef() {
	IP ip = FirewallTestsUtility.getRandomIP();
	Source source1 = Source.valueOf(ip);
	Source source2 = Source.valueOf(ip);
	assertTrue(source1.contains(source2));
	assertTrue(source2.contains(source1));
    }

    @Test
    public void containsTestNotContainsNull() {
	IP ip = FirewallTestsUtility.getRandomIP();
	Source source1 = Source.valueOf(ip);
	Source source2 = null;
	assertFalse(source1.contains(source2));
    }

    @Test
    public void containsTestNotContainsOtherAttributes() {
	IP ip = FirewallTestsUtility.getRandomIP();
	Source source = Source.valueOf(ip);

	Destination des = Destination.valueOf(ip);
	assertFalse(source.contains(des));

	Service service = Service.valueOf("TCP", 80);
	assertFalse(source.contains(service));
    }

    @Test
    public void containsTestOtherDestination() {
	IP ip1 = FirewallTestsUtility.getRandomIP();
	IP ip2 = FirewallTestsUtility.getRandomIP();
	Source source1 = Source.valueOf(ip1);
	Source source2 = Source.valueOf(ip2);
	assertEquals(ip1.contains(ip2), source1.contains(source2));
    }

    @Test
    public void equalsTestTrue() {
	Source s1, s2;
	s1 = Source.valueOf("2.12.45.7/21");
	s2 = Source.valueOf("2.12.45.7/21");
	assertEquals(s1, s2);
    }

    @Test
    public void equalsTestFalse() {
	Source s1, s2;
	s1 = Source.valueOf("2.12.45.7/21");
	s2 = Source.valueOf("2.13.45.7/21");
	assertNotEquals(s1, s2);
    }

    @Test
    public void toStringTest() {
	IP ip = FirewallTestsUtility.getRandomIP();
	Source des = Source.valueOf(ip);
	assertTrue(ip.toString().equals(des.toString()));
    }

}
