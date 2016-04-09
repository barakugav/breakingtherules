package breakingtherules.utilities;

import java.util.List;

/**
 * The TextBuilder in a tool used to create text that doesn't go over max line
 * size and provide easy interface for indented text writing.
 */
public class TextBuilder {

    /**
     * String builder used by this builder
     */
    private StringBuilder m_builder;

    /**
     * Max line size
     */
    private int m_maxLine;

    /**
     * Indent size
     */
    private int m_indentSize;

    /**
     * Array of sequences treated by this builder as words separators
     */
    private String[] m_separatorSequences;

    /**
     * Default max line
     */
    private static final int DEFAULT_MAX_LINE = 80;

    /**
     * Default tab size (number of spaces)
     */
    private static final int DEFAULT_TAB_SIZE = 4;

    private static final String SPACE = " ";
    private static final String LINE_SEPARATOR = System.lineSeparator();

    /**
     * Constructor
     */
    public TextBuilder() {
	m_builder = new StringBuilder();
	m_maxLine = DEFAULT_MAX_LINE;
	m_indentSize = DEFAULT_TAB_SIZE;
	m_separatorSequences = new String[] { SPACE };
    }

    /**
     * Append the builder text with new text
     * 
     * @param text
     *            new text
     */
    public void append(String text) {
	List<String> words = Utility.breakToWords(text, m_separatorSequences);
	for (String word : words) {
	    int lineLength = lineLength();
	    if (lineLength != 0) {
		// Add space if needed, or line separator if went over max line
		m_builder.append(lineLength + word.length() + 1 <= m_maxLine ? SPACE : LINE_SEPARATOR);
	    }
	    m_builder.append(word);
	}
    }

    /**
     * Append the builder text and the go down a line
     * 
     * @param text
     *            new text
     */
    public void appendln(String text) {
	append(text + LINE_SEPARATOR);
    }

    /**
     * Append the builder text with new line
     */
    public void appendln() {
	append(LINE_SEPARATOR);
    }

    /**
     * Append the builder text with indented text
     * 
     * @param text
     *            new text
     */
    public void appedIndented(String text) {
	// Go down a row if needed
	int lineLength = lineLength();
	if (lineLength == 0) {
	    m_builder.append(indent());
	} else {
	    m_builder.append(indentedLineSeparator());
	}

	int intentedMaxLine = m_maxLine - m_indentSize;
	List<String> words = Utility.breakToWords(text, m_separatorSequences);
	for (String word : words) {
	    lineLength = lineLength(getText(), indentedLineSeparator());
	    if (lineLength != 0) {
		// Add space if needed, or line separator if went over max line
		m_builder.append(lineLength + word.length() + 1 <= intentedMaxLine ? SPACE : indentedLineSeparator());
	    }
	    m_builder.append(word);
	}
    }

    /**
     * Append the builder text with indented text and the go down a line
     * 
     * @param text
     *            new text
     */
    public void appedIndentedln(String text) {
	appedIndented(text + LINE_SEPARATOR);
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
     * Set the max line size of this builder. This method intended to be used
     * when creating the builder and not during the build of the text
     * 
     * @param max
     *            new max size
     */
    public void setMaxLine(int max) {
	if (max <= 0) {
	    throw new IllegalArgumentException("max line should be positive " + max);
	}
	m_maxLine = max;
    }

    /**
     * Set the separator sequences this builder will use to different between
     * words. This method intended to be used when creating the builder and not
     * during the build of the text
     * 
     * @param separators
     *            list of separators
     */
    public void setSeparators(List<String> separators) {
	m_separatorSequences = ArraysUtilities.toArray(separators);
    }

    /**
     * Clear the built text
     */
    public void clear() {
	m_builder = new StringBuilder();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return getText();
    }

    /**
     * Get string that represent a indent
     * 
     * @return the indent's string
     */
    private String indent() {
	StringBuilder tabBuilder = new StringBuilder();
	for (int i = 0; i < m_indentSize; i++) {
	    tabBuilder.append(SPACE);
	}
	return tabBuilder.toString();
    }

    /**
     * Get string that represent new line in indented mode
     * 
     * @return indented new line string
     */
    private String indentedLineSeparator() {
	return LINE_SEPARATOR + indent();
    }

    /**
     * Get the current line length
     * 
     * @return length of the current line
     */
    private int lineLength() {
	String text = getText();
	return lineLength(text, LINE_SEPARATOR);
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
    private static int lineLength(String text, String lineSeparator) {
	int[] separatorPosition = Utility.lastPositionOf(text, lineSeparator);
	int length = text.length();
	int separatorIndex = separatorPosition[0];
	int separatorLength = separatorPosition[1];
	return separatorIndex == -1 ? length : text.length() - (separatorIndex + separatorLength);
    }

}
