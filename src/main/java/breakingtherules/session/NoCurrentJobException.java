package breakingtherules.session;

/**
 * Exception that get thrown when trying to operate an operation that require a
 * job, and one isn't set.
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @see JobManager
 */
public class NoCurrentJobException extends IllegalStateException {

    @SuppressWarnings("javadoc")
    private static final long serialVersionUID = 7787453734717147987L;

    /**
     * Construct new NoCurrentJobException.
     */
    public NoCurrentJobException() {
	super();
    }

    /**
     * Construct new NoCurrentJobException with a message
     *
     * @param message
     *            massage of the exception
     */
    public NoCurrentJobException(final String message) {
	super(message);
    }

}
