package breakingtherules.dao;

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
