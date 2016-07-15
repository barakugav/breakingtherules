package breakingtherules.tests.firewall;

import java.util.List;

import org.junit.Test;

import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Attribute.AttributeType;
import breakingtherules.firewall.Hit;
import breakingtherules.tests.TestBase;

@SuppressWarnings("javadoc")
public class HitTest extends TestBase {

    @SuppressWarnings("unused")
    @Test
    public void constructorTest() {
	final List<Attribute> attributes = FirewallTestsUtility.getRandomAttributes();
	new Hit(attributes);
    }

    @SuppressWarnings("unused")
    @Test(expected = NullPointerException.class)
    public void constructorTestNullAttributes() {
	final List<Attribute> attributes = null;
	new Hit(attributes);
    }

    @Test
    public void getAttributesTest() {
	final List<Attribute> expected = FirewallTestsUtility.getRandomAttributes();
	final Hit hit = new Hit(expected);
	final List<Attribute> actual = hit.getAttributes();

	// Sort lists for comparison
	expected.sort(Attribute.ATTRIBUTES_TYPE_COMPARATOR);
	actual.sort(Attribute.ATTRIBUTES_TYPE_COMPARATOR);

	assertEquals(expected, actual);
    }

    @Test
    public void getAttributeTest() {
	final List<Attribute> attributes = FirewallTestsUtility.getRandomAttributes();
	final Hit hit = new Hit(attributes);

	for (final Attribute expected : attributes) {
	    final AttributeType type = expected.getType();
	    final Attribute actual = hit.getAttribute(type);
	    assertEquals(expected, actual);
	}
    }

}
