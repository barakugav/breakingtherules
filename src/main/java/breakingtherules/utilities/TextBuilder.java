package breakingtherules.utilities;

/**
 * The TextBuilder in a tool used to create text that doesn't go over max line
 * size and provide easy interface for indented text writing.
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @see StringBuilder
 */
public class TextBuilder {

    /**
     * String builder used by this builder
     */
    private StringBuilder m_builder;

    /**
     * Cache for the indent string.
     */
    private final String m_indent;

    /**
     * Max line size
     */
    private final int m_maxLine;

    /**
     * Indent size
     */
    private final int m_indentSize;

    /**
     * Array of sequences treated by this builder as words separators
     */
    private final String[] m_separatorSequences;

    /**
     * The line separator in the system.
     */
    private static final String LINE_SEPARATOR = System.lineSeparator();

    /**
     * Default max line
     */
    private static final int DEFAULT_MAX_LINE = 80;

    /**
     * Default tab size (number of spaces)
     */
    private static final int DEFAULT_TAB_SIZE = 4;

    /**
     * Default separators used by the builder to separate words.
     */
    private static final String[] DEFAULT_SEPARATORS_SEQUENCES = { Utility.SPACE_STR };

    /**
     * Construct new TextBuilder with default setting.
     */
    public TextBuilder() {
	this(DEFAULT_MAX_LINE, DEFAULT_TAB_SIZE, DEFAULT_SEPARATORS_SEQUENCES);
    }

    /**
     * Construct new TextBuilder with specified max line and default indent size
     * and separator.
     *
     * @param max
     *            max line length.
     * @throws IllegalArgumentException
     *             if max < 0.
     */
    public TextBuilder(final int max) {
	this(max, DEFAULT_TAB_SIZE, DEFAULT_SEPARATORS_SEQUENCES);
    }

    /**
     * Construct new TextBuilder with specified.
     *
     * @param max
     *            max line length.
     * @param indentSize
     *            size of the indent used by this builder.
     * @param separators
     *            separators used to separate words.
     * @throws IllegalArgumentException
     *             if max < 0 or indentSize < 0.
     * @throws NullPointerException
     *             if the separators array is null or one of the separators is
     *             null.
     */
    public TextBuilder(final int max, final int indentSize, final String... separators) {
	if (max < 0)
	    throw new IllegalArgumentException("max can't be negative: " + max);
	if (indentSize < 0)
	    throw new IllegalArgumentException("indentSize < 0: " + indentSize);
	if (separators == null)
	    throw new NullPointerException();
	for (final String separator : separators)
	    if (separator == null)
		throw new NullPointerException();

	m_builder = new StringBuilder();
	m_maxLine = max;
	m_indentSize = indentSize;
	m_separatorSequences = separators;
	m_indent = indent(indentSize);
    }

    /**
     * Append the builder text with indented text
     *
     * @param text
     *            new text
     */
    public void appedIndented(final String text) {
	// Go down a row if needed
	int lineLength = lineLength();
	if (lineLength == 0)
	    m_builder.append(m_indent);
	else
	    m_builder.append(indentedLineSeparator());

	final int intentedMaxLine = m_maxLine - m_indentSize;
	final String[] words = Utility.breakToWords(text, m_separatorSequences);
	for (final String word : words) {
	    lineLength = lineLength(getText(), indentedLineSeparator());
	    if (lineLength != 0)
		// Add space if needed, or line separator if went over max line
		m_builder.append(lineLength + word.length() + 1 <= intentedMaxLine ? Utility.SPACE_STR
			: indentedLineSeparator());
	    m_builder.append(word);
	}
    }

    /**
     * Append the builder text with indented text and the go down a line
     *
     * @param text
     *            new text
     */
    public void appedIndentedln(final String text) {
	appedIndented(text + LINE_SEPARATOR);
    }

    /**
     * Append the builder text with new text
     *
     * @param text
     *            new text
     */
    public void append(final String text) {
	final String[] words = Utility.breakToWords(text, m_separatorSequences);
	for (final String word : words) {
	    final int lineLength = lineLength();
	    if (lineLength != 0)
		// Add space if needed, or line separator if went over max line
		m_builder.append(lineLength + word.length() + 1 <= m_maxLine ? Utility.SPACE_STR : LINE_SEPARATOR);
	    m_builder.append(word);
	}
    }

    /**
     * Append the builder text with new line
     */
    public void appendln() {
	append(LINE_SEPARATOR);
    }

    /**
     * Append the builder text and the go down a line
     *
     * @param text
     *            new text
     */
    public void appendln(final String text) {
	append(text + LINE_SEPARATOR);
    }

    /**
     * Clear the built text
     */
    public void clear() {
	m_builder = new StringBuilder();
    }

    /**
     * Get the text of this builder
     *
     * @return the builder's text
     */
    public String getText() {
	return m_builder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
	return getText();
    }

    /**
     * Get string that represent new line in indented mode
     *
     * @return indented new line string
     */
    private String indentedLineSeparator() {
	return LINE_SEPARATOR + m_indent;
    }

    /**
     * Get the current line length
     *
     * @return length of the current line
     */
    private int lineLength() {
	final String text = getText();
	return lineLength(text, LINE_SEPARATOR);
    }

    /**
     * Get string that represent a indent
     *
     * @param indentSize
     *            the number of spaces in the indent.
     * @return the indent's string
     */
    private static String indent(final int indentSize) {
	final StringBuilder tabBuilder = new StringBuilder();
	for (int i = 0; i < indentSize; i++)
	    tabBuilder.append(Utility.SPACE_STR);
	return tabBuilder.toString();
    }

    /**
     * Get the length of the last list in a text
     *
     * @param text
     *            the looked text
     * @param lineSeparator
     *            string sequence represent a new line
     * @return size of the last line in the text
     */
    private static int lineLength(final String text, final String lineSeparator) {
	final int[] separatorPosition = Utility.lastPositionOf(text, lineSeparator);
	final int length = text.length();
	final int separatorIndex = separatorPosition[0];
	final int separatorLength = separatorPosition[1];
	return separatorIndex == -1 ? length : text.length() - (separatorIndex + separatorLength);
    }

}
