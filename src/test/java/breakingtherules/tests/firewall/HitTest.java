package breakingtherules.tests.firewall;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.Test;

import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Hit;

public class HitTest {

    @Test
    public void constructorTest() {
	System.out.println("# HitTest constructorTest");
	int id = FirewallTestsUtility.getRandomID();
	List<Attribute> attributes = FirewallTestsUtility.getRandomAttributes();
	new Hit(id, attributes);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestNullAttributes() {
	System.out.println("# HitTest constructorTestNullAttributes");
	int id = FirewallTestsUtility.getRandomID();
	List<Attribute> attributes = null;
	new Hit(id, attributes);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestNegativeId() {
	System.out.println("# HitTest constructorTestNegativeId");
	int id = -FirewallTestsUtility.getRandomID();
	List<Attribute> attributes = FirewallTestsUtility.getRandomAttributes();
	new Hit(id, attributes);
    }

    @Test
    public void getIdTest() {
	System.out.println("# HitTest getIdTest");
	int id = FirewallTestsUtility.getRandomID();
	List<Attribute> attributes = FirewallTestsUtility.getRandomAttributes();
	Hit hit = new Hit(id, attributes);
	assertEquals(id, hit.getId());
    }

    @Test
    public void getAttributeTest() {
	System.out.println("# HitTest getAttributeTest");
	int id = FirewallTestsUtility.getRandomID();
	List<Attribute> attributes = FirewallTestsUtility.getRandomAttributes();
	Hit hit = new Hit(id, attributes);

	for (Attribute expected : attributes) {
	    String type = expected.getType();
	    Attribute actual = hit.getAttribute(type);
	    assertEquals(expected, actual);
	}
    }

    @Test
    public void getAttributesTest() {
	System.out.println("# HitTest getAttributesTest");
	int id = FirewallTestsUtility.getRandomID();
	List<Attribute> expected = FirewallTestsUtility.getRandomAttributes();
	Hit hit = new Hit(id, expected);
	List<Attribute> actual = hit.getAttributes();

	// Sort lists for comparison
	Attribute.sort(expected);
	Attribute.sort(actual);
	assertEquals(expected, actual);
    }

    @Test
    public void cloneTest() {
	System.out.println("# HitTest cloneTest");
	int id = FirewallTestsUtility.getRandomID();
	List<Attribute> attributes = FirewallTestsUtility.getRandomAttributes();
	Hit hit = new Hit(id, attributes);
	Hit hitClone = hit.clone();
	assertFalse(hit == hitClone);
	assertEquals(hit, hitClone);
    }

}
