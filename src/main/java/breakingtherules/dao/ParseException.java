package breakingtherules.dao;

/**
 * The parse exception is thrown when a DAO object parse data and it's invalid.
 * <p>
 * The {@link HitsDao} and {@link RulesDao} are parsing data. When they are
 * encounter invalid data they should throw this exception.
 * <p>
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 */
public class ParseException extends Exception {

    @SuppressWarnings("javadoc")
    private static final long serialVersionUID = 1L;

    /**
     * Construct new ParseException without a message or a cause.
     */
    public ParseException() {
	super();
    }

    /**
     * Construct new ParseException with a message.
     *
     * @param message
     *            the exception's message.
     */
    public ParseException(final String message) {
	super(message);
    }

    /**
     *
     * Construct new ParseException with a message and cause.
     *
     * @param message
     *            the exception's message.
     * @param cause
     *            the cause of this exception.
     */
    public ParseException(final String message, final Throwable cause) {
	super(message, cause);
    }

    /**
     * Construct new ParseException with a cause.
     *
     * @param cause
     *            the cause of this exception.
     */
    public ParseException(final Throwable cause) {
	super(cause);
    }

}
