package breakingtherules.dao;

import java.io.UncheckedIOException;

/**
 * Unchecked version of {@link ParseException}.
 * <p>
 * The UncheckedParseException is a wrapper for a checked {@link ParseException}
 * .
 * <p>
 * Used when implementing or overriding a method that doesn't throw super class
 * exception of {@link ParseException}.
 * <p>
 * This exception is similar to {@link UncheckedIOException}.
 * <p>
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 */
class UncheckedParseException extends RuntimeException {

    @SuppressWarnings("javadoc")
    private static final long serialVersionUID = 6371272539188428352L;

    /**
     * Construct new UncheckedParseException without a message.
     *
     * @param cause
     *            the original checked {@link ParseException}.
     */
    protected UncheckedParseException(final ParseException cause) {
	super(cause);
    }

    /**
     * Construct new UncheckedParseException with a message.
     *
     * @param message
     *            the exception's message.
     * @param cause
     *            the original checked {@link ParseException}.
     */
    protected UncheckedParseException(final String message, final ParseException cause) {
	super(message, cause);
    }

    /**
     * Get the {@link ParseException} cause of this unchecked exception.
     * <p>
     */
    @Override
    public synchronized ParseException getCause() {
	return (ParseException) super.getCause();
    }

}