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

public class SourceTest extends TestBase {

    @Test
    public void constructorTest() {
	System.out.println("# SourceTest constructorTest");
	IP ip = FirewallTestsUtility.getRandomIP();
	Source.create(ip);
    }

    @Test(expected = NullPointerException.class)
    public void constructorTestNullIP() {
	System.out.println("# SourceTest constructorTestNullIP");
	IP ip = null;
	Source.create(ip);
    }

    @Test
    public void constructorStringTest() {
	System.out.println("# SourceTest constructorStringTest");
	String ip = "2.12.45.7/21";
	Source.create(ip);
    }

    @Test(expected = NullPointerException.class)
    public void constructorStringTestNullIP() {
	System.out.println("# SourceTest constructorStringTestNullIP");
	String ip = null;
	Source.create(ip);
    }

    @Test
    public void getIPTest() {
	System.out.println("# SourceTest getIPTest");
	IP ip = FirewallTestsUtility.getRandomIP();
	Source source = Source.create(ip);
	assertEquals(ip, source.getIp());
    }

    @Test
    public void containsTestContainsItsef() {
	System.out.println("# SourceTest containsTestContainsItsef");
	IP ip = FirewallTestsUtility.getRandomIP();
	Source source1 = Source.create(ip);
	Source source2 = Source.create(ip);
	assertTrue(source1.contains(source2));
	assertTrue(source2.contains(source1));
    }

    @Test
    public void containsTestNotContainsNull() {
	System.out.println("# SourceTest containsTestNotContainsNull");
	IP ip = FirewallTestsUtility.getRandomIP();
	Source source1 = Source.create(ip);
	Source source2 = null;
	assertFalse(source1.contains(source2));
    }

    @Test
    public void containsTestNotContainsOtherAttributes() {
	System.out.println("# SourceTest containsTestNotContainsOtherAttributes");
	IP ip = FirewallTestsUtility.getRandomIP();
	Source source = Source.create(ip);

	Destination des = Destination.create(ip);
	assertFalse(source.contains(des));

	Service service = Service.create("TCP", 80);
	assertFalse(source.contains(service));
    }

    @Test
    public void containsTestOtherDestination() {
	System.out.println("# SourceTest containsTestOtherDestination");
	IP ip1 = FirewallTestsUtility.getRandomIP();
	IP ip2 = FirewallTestsUtility.getRandomIP();
	Source source1 = Source.create(ip1);
	Source source2 = Source.create(ip2);
	assertEquals(ip1.contains(ip2), source1.contains(source2));
    }

    @Test
    public void equalsTestTrue() {
	System.out.println("# SourceTest equalsTestTrue");
	Source s1, s2;
	s1 = Source.create("2.12.45.7/21");
	s2 = Source.create("2.12.45.7/21");
	assertEquals(s1, s2);
    }

    @Test
    public void equalsTestFalse() {
	System.out.println("# SourceTest equalsTestFalse");
	Source s1, s2;
	s1 = Source.create("2.12.45.7/21");
	s2 = Source.create("2.13.45.7/21");
	assertNotEquals(s1, s2);
    }

    @Test
    public void toStringTest() {
	System.out.println("# SourceTest toStringTest");
	IP ip = FirewallTestsUtility.getRandomIP();
	Source des = Source.create(ip);
	assertTrue(ip.toString().equals(des.toString()));
    }

}
