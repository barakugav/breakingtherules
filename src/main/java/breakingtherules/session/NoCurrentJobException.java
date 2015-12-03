package breakingtherules.session;

public class NoCurrentJobException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 7787453734717147987L;

    public NoCurrentJobException() {
	super();
    }

    public NoCurrentJobException(String message) {
	super(message);
    }

}
