package breakingtherules.dao.xml;

import breakingtherules.dao.ParseException;

public class XMLParseException extends ParseException {

    private static final long serialVersionUID = 1L;

    public XMLParseException() {
	super();
    }

    public XMLParseException(final String message) {
	super(message);
    }

    public XMLParseException(final Throwable cause) {
	super(cause);
    }

}
