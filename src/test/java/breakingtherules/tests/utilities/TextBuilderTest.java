package breakingtherules.tests.utilities;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import breakingtherules.utilities.TextBuilder;

@SuppressWarnings("javadoc")
public class TextBuilderTest {

    private static final String LoremIpsum = "Lorem ipsum dolor sit amet, "
	    + "consectetur adipiscing elit, sed do eiusmod tempor incididunt "
	    + "ut labore et dolore magna aliqua. Ut enim ad minim veniam, "
	    + "quis nostrud exercitation ullamco laboris nisi ut aliquip ex "
	    + "ea commodo consequat. Duis aute irure dolor in reprehenderit "
	    + "in voluptate velit esse cillum dolore eu fugiat nulla pariatur. "
	    + "Excepteur sint occaecat cupidatat non proident, sunt in culpa "
	    + "qui officia deserunt mollit anim id est laborum.";

    @Test
    public void appendTestNoWrap() {
	TextBuilder builder = new TextBuilder(20);
	String expected = "Hello world!";

	builder.append(expected);
	String actual = builder.getText();
	assertEquals(expected, actual);
    }

    @Test
    public void appendTestWithWrap() {
	TextBuilder builder = new TextBuilder(20);
	String expectedFirstRow = "Hello big world,";
	String expectedSecondRow = "what is up?";
	String expected = expectedFirstRow + System.lineSeparator() + expectedSecondRow;
	String text = expectedFirstRow + " " + expectedSecondRow;

	builder.append(text);
	String actual = builder.getText();
	assertEquals(expected, actual);
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void setMaxLineTestNegative() {
	new TextBuilder(-1);
    }

    @Test
    public void toStringTest() {
	TextBuilder builder = new TextBuilder(20);

	builder.appendln(LoremIpsum);
	String expected = builder.getText();
	String actual = builder.toString();
	assertEquals(expected, actual);
    }

}
