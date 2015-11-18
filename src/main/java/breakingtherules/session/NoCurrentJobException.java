package breakingtherules.session;

public class NoCurrentJobException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 7787453734717147987L;
    
    private final String m_message;
    
    public NoCurrentJobException() {
	m_message = "";
    }
    
    public NoCurrentJobException(String message) {
	m_message = message;
    }
    
    @Override
    public String getMessage() {
	return m_message;
    }

}
