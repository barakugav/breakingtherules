package breakingtherules.firewall;

/**
 * Service attribute
 * 
 * Have a type member and port number
 */
public class Service extends Attribute {

    /**
     * Type of the service (HTTP, HTTPS, etc)
     */
    private String m_protocol;

    /**
     * Port number of the service
     */
    private int m_port;

    /**
     * Service of type 'Any protocol'
     */
    private static final String ANY_PROTOCOL = null;

    /**
     * Port number 'Any port'
     */
    private static final int ANY_PORT = -1;

    /**
     * Constructor
     * 
     * @param type
     *            type of the service
     * @param port
     *            port number of the service
     */
    public Service(String type, int port) {
	super(AttType.Service);
	m_protocol = type;
	m_port = port;
    }

    /**
     * Constructor of String service
     * 
     * @param service
     *            String service in format ('port' 'service type')
     */
    public Service(String service) {
	super(AttType.Service);
	int separatorIndex = service.indexOf(' ');

	if (separatorIndex < 0) {
	    m_port = ANY_PORT;
	    m_protocol = service;
	    return;
	}

	String stPort = service.substring(0, separatorIndex);
	m_port = Integer.parseInt(stPort);
	m_protocol = service.substring(separatorIndex + 1);
    }

    /**
     * Get the type of the service
     * 
     * @return String type of the service
     */
    public String getType() {
	return m_protocol;
    }

    /**
     * Get the port of the service
     * 
     * @return port number of the service
     */
    public int getPort() {
	return m_port;
    }

    @Override
    public boolean contains(Attribute other) {
	if (!(other instanceof Service)) {
	    return false;
	}

	Service o = (Service) other;
	if (this.m_protocol == ANY_PROTOCOL)
	    return true;
	if (o.m_protocol == ANY_PROTOCOL)
	    return false;
	if (!this.m_protocol.equals(o.m_protocol)) {
	    return false;
	}

	if (this.m_port == ANY_PORT)
	    return true;
	if (o.m_port == ANY_PORT)
	    return false;
	if (this.m_port != o.m_port)
	    return false;

	return true;
    }

    @Override
    public int hashCode() {
	return m_protocol.hashCode() + m_port;
    }

    @Override
    public boolean equals(Object o) {
	if (o == null)
	    return false;
	if (!(o instanceof Service))
	    return false;

	Service other = (Service) o;
	if (m_port != other.m_port)
	    return false;
	if (!m_protocol.equals(other.m_protocol))
	    return false;
	return true;
    }

    @Override
    public String toString() {
	return m_protocol + " " + m_port;
    }
}
