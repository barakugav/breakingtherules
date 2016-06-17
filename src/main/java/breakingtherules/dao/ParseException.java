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

    private static final long serialVersionUID = 1L;

    public ParseException() {
	super();
    }

    public ParseException(final String message) {
	super(message);
    }

    public ParseException(final Throwable cause) {
	super(cause);
    }

    public ParseException(final String message, final Throwable cause) {
	super(message, cause);
    }

}
