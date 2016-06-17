package breakingtherules.dao.csv;

/**
 * Unchecked version of {@link CSVParseException}.
 * <p>
 * Used when implementing or overriding a method that doesn't throw super class
 * exception of {@link CSVParseException}.
 * <p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 */
class UncheckedCSVParseException extends RuntimeException {

    private static final long serialVersionUID = 6371272539188428352L;

    UncheckedCSVParseException(CSVParseException cause) {
	super(cause);
    }

    UncheckedCSVParseException(String message, CSVParseException cause) {
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
