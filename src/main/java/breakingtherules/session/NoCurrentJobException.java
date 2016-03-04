package breakingtherules.session;

/**
 * Exception that get thrown when trying to operate an operation that require a
 * job, and one isn't set
 */
public class NoCurrentJobException extends IllegalStateException {

    private static final long serialVersionUID = 7787453734717147987L;

    /**
     * Constructor
     */
    public NoCurrentJobException() {
	super();
    }

    /**
     * Constructor with a message
     * 
     * @param message
     *            massage of the exception
     */
    public NoCurrentJobException(String message) {
	super(message);
    }

}
