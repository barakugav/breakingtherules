package breakingtherules.tests.firewall;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Hit;
import breakingtherules.tests.TestBase;

public class HitTest extends TestBase {

    @Test
    public void constructorTest() {
	System.out.println("# HitTest constructorTest");
	int id = FirewallTestsUtility.getRandomID();
	List<Attribute> attributes = FirewallTestsUtility.getRandomAttributes();
	new Hit(id, attributes);
    }

    @Test(expected = NullPointerException.class)
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
	expected.sort(Attribute.ATTRIBUTES_COMPARATOR);
	// Clone actual list by creating new list with the elements. This is
	// needed to be done because the hit.getAttributes() return unmodifiable
	// list(so sort operation is not supported).
	actual = new ArrayList<>(actual);
	actual.sort(Attribute.ATTRIBUTES_COMPARATOR);

	assertEquals(expected, actual);
    }

}
