package breakingtherules.dao.csv;

import breakingtherules.dao.ParseException;

public class CSVParseException extends ParseException {

    private static final long serialVersionUID = 1L;

    public CSVParseException() {
	super();
    }

    public CSVParseException(String message) {
	super(message);
    }

    public CSVParseException(Throwable cause) {
	super(cause);
    }

    public CSVParseException(String message, Throwable cause) {
	super(message, cause);
    }

}
