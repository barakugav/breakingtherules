package breakingtherules.tests.firewall;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Hit;

public class HitTest {

    @Test
    public void constructorTest() {
	int id = FirewallTestsUtility.getRandomID();
	List<Attribute> attributes = FirewallTestsUtility.getRandomAttributes();
	new Hit(id, attributes);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestNullAttributes() {
	int id = FirewallTestsUtility.getRandomID();
	List<Attribute> attributes = null;
	new Hit(id, attributes);
	fail("Allowed creating Hit with null attributes");
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTestNegativeID() {
	int id = -FirewallTestsUtility.getRandomID();
	List<Attribute> attributes = FirewallTestsUtility.getRandomAttributes();
	new Hit(id, attributes);
	fail("Allowed creating Hit with negative id");
    }

    @Test
    public void getIdTest() {
	int id = FirewallTestsUtility.getRandomID();
	List<Attribute> attributes = FirewallTestsUtility.getRandomAttributes();
	Hit hit = new Hit(id, attributes);
	assertEquals(id, hit.getId());
    }

    @Test
    public void getAttributesTest() {
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
    public void getAttributeTest() {
	int id = FirewallTestsUtility.getRandomID();
	List<Attribute> attributes = FirewallTestsUtility.getRandomAttributes();
	Hit hit = new Hit(id, attributes);

	for (Attribute expected : attributes) {
	    String type = expected.getType();
	    Attribute actual = hit.getAttribute(type);
	    assertEquals(expected, actual);
	}
    }

}
