package breakingtherules.dao.xml;

import breakingtherules.dao.ParseException;

public class XMLParseException extends ParseException {

    private static final long serialVersionUID = 1L;

    public XMLParseException() {
	super();
    }

    public XMLParseException(String message) {
	super(message);
    }

    public XMLParseException(Throwable cause) {
	super(cause);
    }

}
