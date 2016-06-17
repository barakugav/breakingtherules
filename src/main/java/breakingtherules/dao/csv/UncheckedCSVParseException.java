package breakingtherules.dao.csv;

class UncheckedCSVParseException extends RuntimeException {

    private static final long serialVersionUID = 6371272539188428352L;

    UncheckedCSVParseException(CSVParseException cause) {
	super(cause);
    }

    UncheckedCSVParseException(String message, CSVParseException cause) {
	super(message, cause);
    }

    @Override
    public synchronized CSVParseException getCause() {
	return (CSVParseException) super.getCause();
    }

}
