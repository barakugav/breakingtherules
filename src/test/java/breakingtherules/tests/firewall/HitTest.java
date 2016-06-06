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
	List<Attribute> attributes = FirewallTestsUtility.getRandomAttributes();
	new Hit(attributes);
    }

    @Test(expected = NullPointerException.class)
    public void constructorTestNullAttributes() {
	System.out.println("# HitTest constructorTestNullAttributes");
	List<Attribute> attributes = null;
	new Hit(attributes);
    }


    @Test
    public void getAttributeTest() {
	System.out.println("# HitTest getAttributeTest");
	List<Attribute> attributes = FirewallTestsUtility.getRandomAttributes();
	Hit hit = new Hit(attributes);

	for (Attribute expected : attributes) {
	    String type = expected.getType();
	    Attribute actual = hit.getAttribute(type);
	    assertEquals(expected, actual);
	}
    }

    @Test
    public void getAttributesTest() {
	System.out.println("# HitTest getAttributesTest");
	List<Attribute> expected = FirewallTestsUtility.getRandomAttributes();
	Hit hit = new Hit(expected);
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
