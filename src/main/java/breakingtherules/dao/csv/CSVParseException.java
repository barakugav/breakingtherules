package breakingtherules.dao.csv;

import breakingtherules.dao.ParseException;

/**
 * The CSVParseException is a parse exception of CSV files.
 * <p>
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 */
public class CSVParseException extends ParseException {

    @SuppressWarnings("javadoc")
    private static final long serialVersionUID = 1L;

    /**
     * Construct new CSVParseException without a message or a cause.
     */
    public CSVParseException() {
	super();
    }

    /**
     * Construct new CSVParseException with a message.
     *
     * @param message
     *            the exception's message.
     */
    public CSVParseException(final String message) {
	super(message);
    }

    /**
     *
     * Construct new CSVParseException with a message and cause.
     *
     * @param message
     *            the exception's message.
     * @param cause
     *            the cause of this exception.
     */
    public CSVParseException(final String message, final Throwable cause) {
	super(message, cause);
    }

    /**
     * Construct new CSVParseException with a cause.
     *
     * @param cause
     *            the cause of this exception.
     */
    public CSVParseException(final Throwable cause) {
	super(cause);
    }

}
