package breakingtherules.dao.xml;

import breakingtherules.dao.ParseException;

/**
 * A XML parse exception thrown when parsing XML files, and the data is invalid.
 * <p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * 
 * @see HitsXmlDao
 * @see RulesXmlDao
 */
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

    public XMLParseException(final String message, final Throwable cause) {
	super(message, cause);
    }

}
