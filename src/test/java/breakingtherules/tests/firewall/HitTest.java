package breakingtherules.tests.firewall;

import java.util.List;

import org.junit.Test;

import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Hit;
import breakingtherules.tests.TestBase;

@SuppressWarnings("javadoc")
public class HitTest extends TestBase {

    @SuppressWarnings("unused")
    @Test
    public void constructorTest() {
	List<Attribute> attributes = FirewallTestsUtility.getRandomAttributes();
	new Hit(attributes);
    }

    @SuppressWarnings("unused")
    @Test(expected = NullPointerException.class)
    public void constructorTestNullAttributes() {
	List<Attribute> attributes = null;
	new Hit(attributes);
    }

    @Test
    public void getAttributeTest() {
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
	List<Attribute> expected = FirewallTestsUtility.getRandomAttributes();
	Hit hit = new Hit(expected);
	List<Attribute> actual = hit.getAttributes();

	// Sort lists for comparison
	expected.sort(Attribute.ATTRIBUTES_TYPE_COMPARATOR);
	actual.sort(Attribute.ATTRIBUTES_TYPE_COMPARATOR);

	assertEquals(expected, actual);
    }

}
