package breakingtherules.application;

public class Greeting {

    private final long m_id;
    
    private final String m_content;

    public Greeting(long id, String content) {
	m_id = id;
	m_content = content;
    }

    public long getId() {
	return m_id;
    }

    public String getContent() {
	return m_content;
    }
}
