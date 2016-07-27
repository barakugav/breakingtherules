package breakingtherules.util;

import java.io.PrintStream;

/**
 * The TextPrinter is a tool used print text by used the TextBuilder. This tool
 * provide an easy interface to print texts.
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @see TextBuilder
 * @see PrintStream
 */
public class TextPrinter {

    /**
     * Builder used by this printer
     */
    private final TextBuilder m_builder;

    /**
     * Output stream of this printer
     */
    private final PrintStream m_outStream;

    /**
     * Text that already printed
     */
    private String m_alreadyPrinted;

    /**
     * Constructor with default builder and output stream
     */
    public TextPrinter() {
	this(System.out, new TextBuilder());
    }

    /**
     * Constructor with default builder
     *
     * @param stream
     *            output stream for this printer
     */
    public TextPrinter(final PrintStream stream) {
	this(stream, new TextBuilder());
    }

    /**
     * Constructor without default elements
     *
     * @param stream
     *            output stream for this printer
     * @param builder
     *            text builder used by this provider
     */
    public TextPrinter(final PrintStream stream, final TextBuilder builder) {
	m_outStream = stream;
	m_builder = builder;
	m_alreadyPrinted = "";
    }

    /**
     * Constructor with default output stream
     *
     * @param builder
     *            text builder used by this provider
     */
    public TextPrinter(final TextBuilder builder) {
	this(System.out, builder);
    }

    /**
     * Print a string.
     *
     * @param s
     *            the string.
     */
    public void print(final String s) {
	m_builder.append(s);
	print();
    }

    /**
     * Print a string in indented mode.
     *
     * @param s
     *            the string.
     */
    public void printIndented(final String s) {
	m_builder.appedIndented(s);
	print();
    }

    /**
     * Print a string in indented mode and go down a line.
     *
     * @param s
     *            the string.
     */
    public void printIndentedln(final String s) {
	m_builder.appedIndentedln(s);
	print();
    }

    /**
     * Go down a line.
     */
    public void println() {
	m_builder.appendln();
	print();
    }

    /**
     * Print a string and go down a line.
     *
     * @param s
     *            the string.
     */
    public void println(final String s) {
	m_builder.appendln(s);
	print();
    }

    /**
     * Method used by all others, used to print to the actual output stream
     */
    private void print() {
	final String text = m_builder.getText();
	final String toPrint = text.substring(m_alreadyPrinted.length());
	m_alreadyPrinted = text;
	m_outStream.print(toPrint);
    }

}
