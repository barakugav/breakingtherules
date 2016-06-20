package breakingtherules.dao.csv;

import java.io.UncheckedIOException;

/**
 * Unchecked version of {@link CSVParseException}.
 * <p>
 * The UncheckedCSVParseException is a wrapper for a checked
 * {@link CSVParseException}.
 * <p>
 * Used when implementing or overriding a method that doesn't throw super class
 * exception of {@link CSVParseException}.
 * <p>
 * This exception is similar to {@link UncheckedIOException}.
 * <p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 */
class UncheckedCSVParseException extends RuntimeException {

    @SuppressWarnings("javadoc")
    private static final long serialVersionUID = 6371272539188428352L;

    /**
     * Construct new UncheckedCSVParseException without a message.
     * 
     * @param cause
     *            the original checked {@link CSVParseException}.
     */
    UncheckedCSVParseException(final CSVParseException cause) {
	super(cause);
    }

    /**
     * Construct new UncheckedCSVParseException with a message.
     * 
     * @param message
     *            the exception's message.
     * @param cause
     *            the original checked {@link CSVParseException}.
     */
    UncheckedCSVParseException(final String message, final CSVParseException cause) {
	super(message, cause);
    }

    /**
     * Get the {@link CSVParseException} cause of this unchecked exception.
     * <p>
     */
    @Override
    public synchronized CSVParseException getCause() {
	return (CSVParseException) super.getCause();
    }

}
