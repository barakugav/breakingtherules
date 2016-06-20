package breakingtherules.dao.xml;

import breakingtherules.dao.ParseException;

/**
 * A XML parse exception thrown when parsing XML files, and the data is invalid.
 * <p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * 
 * @see XMLHitsDao
 * @see XMLRulesDao
 */
public class XMLParseException extends ParseException {

    @SuppressWarnings("javadoc")
    private static final long serialVersionUID = 1L;

    /**
     * Construct new XMLParseException without a message or a cause
     */
    public XMLParseException() {
	super();
    }

    /**
     * Construct new XMLParseException with a message.
     * 
     * @param message
     *            the exception's message
     */
    public XMLParseException(final String message) {
	super(message);
    }

    /**
     * Construct new XMLParseException with a cause.
     * 
     * @param cause
     *            the cause of this exception.
     */
    public XMLParseException(final Throwable cause) {
	super(cause);
    }

    /**
     * Construct new XMLParseException with message and cause.
     * 
     * @param message
     *            the exception's message.
     * @param cause
     *            the cause of this exception.
     */
    public XMLParseException(final String message, final Throwable cause) {
	super(message, cause);
    }

}
