package breakingtherules.firewall;

import org.springframework.util.StringUtils;

/**
 * Service attribute
 * 
 * Has a protocol member and port number
 */
public class Service extends Attribute {

    /**
     * Type of the service (HTTP, HTTPS, etc)
     */
    private String m_protocol;

    /**
     * Start of the ports range
     */
    private int m_portRangeStart;

    /**
     * End of the ports range
     */
    private int m_portRangeEnd;

    /**
     * Service of protocol 'Any protocol'
     */
    public static final String ANY_PROTOCOL = null;

    /**
     * The minimum legal port number
     */
    public static final int MIN_PORT = 0;

    /**
     * The maximum legal port number
     */
    public static final int MAX_PORT = (1 << 16) - 1;

    /**
     * Port number 'Any start range port'
     */
    public static final int ANY_PORT_START_RANGE = Integer.MIN_VALUE;

    /**
     * Port number 'Any end range port'
     */
    public static final int ANY_PORT_END_RANGE = Integer.MAX_VALUE;

    /**
     * Constructor
     * 
     * @param type
     *            type of the service
     * @param port
     *            port number of the service
     */
    public Service(String protocol, int port) {
	super(AttType.Service);
	m_protocol = protocol;
	if (port < MIN_PORT || port > MAX_PORT)
	    throw new IllegalArgumentException(
		    "Port not in range: " + port + ". should be in range [" + MIN_PORT + ", " + MAX_PORT + "]");
	m_portRangeStart = m_portRangeEnd = port;
    }

    /**
     * Constructor
     * 
     * @param type
     *            type of the service
     * @param portRangeStart
     *            start of the port range of the service
     * @param portRangeEnd
     *            end of the port range of the service
     */
    public Service(String protocol, int portRangeStart, int portRangeEnd) throws IllegalArgumentException {
	super(AttType.Service);
	m_protocol = protocol;

	if (portRangeStart != ANY_PORT_START_RANGE)
	    if (portRangeStart < MIN_PORT || portRangeStart > MAX_PORT)
		throw new IllegalArgumentException("Port not in range: " + portRangeStart + ". should be in range ["
			+ MIN_PORT + ", " + MAX_PORT + "]");
	if (portRangeEnd != ANY_PORT_END_RANGE)
	    if (portRangeEnd < MIN_PORT || portRangeEnd > MAX_PORT)
		throw new IllegalArgumentException("Port not in range: " + portRangeEnd + ". should be in range ["
			+ MIN_PORT + ", " + MAX_PORT + "]");

	m_portRangeStart = portRangeStart;
	m_portRangeEnd = portRangeEnd;
    }

    /**
     * Constructor of String service
     * 
     * @param service
     *            String service in format ('port' 'service type')
     */
    public Service(String service) throws IllegalArgumentException {
	super(AttType.Service);

	int separatorIndex;

	switch (StringUtils.countOccurrencesOf(service, " ")) {
	case 0:
	    break;
	case 1:
	    // only one port number
	    separatorIndex = service.indexOf(' ');
	    String portStr = service.substring(0, separatorIndex);
	    if (portStr.equals("Any")) {
		m_portRangeStart = ANY_PORT_START_RANGE;
		m_portRangeEnd = ANY_PORT_END_RANGE;
	    } else {
		m_portRangeStart = m_portRangeEnd = Integer.parseInt(portStr);
	    }
	    service = service.substring(separatorIndex + 1);
	    break;
	case 2:
	    // start port
	    separatorIndex = service.indexOf(' ');
	    String portRangeStartStr = service.substring(0, separatorIndex);
	    if (portRangeStartStr.equals("Any")) {
		m_portRangeStart = ANY_PORT_START_RANGE;
	    } else {
		m_portRangeStart = Integer.parseInt(portRangeStartStr);
	    }
	    service = service.substring(separatorIndex + 1);
	    // end port
	    separatorIndex = service.indexOf(' ');
	    String portRangeEndStr = service.substring(0, separatorIndex);
	    if (portRangeEndStr.equals("Any")) {
		m_portRangeEnd = ANY_PORT_END_RANGE;
	    } else {
		m_portRangeEnd = Integer.parseInt(portRangeEndStr);
	    }
	    service = service.substring(separatorIndex + 1);
	    break;
	default:
	    throw new IllegalArgumentException("Unknow format: too many words");
	}

	m_protocol = service.equals("Any") ? ANY_PROTOCOL : service;
    }

    /**
     * Get the protocol of the service
     * 
     * @return String protocol of the service
     */
    public String getProtocol() {
	return m_protocol;
    }

    /**
     * Get the start of the port range of the service
     * 
     * @return start of the port range of the service
     */
    public int getPortRangeStart() {
	return m_portRangeStart;
    }

    /**
     * Get the start of the port range of the service
     * 
     * @return start of the port range of the service
     */
    public int getPortRangeEnd() {
	return m_portRangeStart;
    }

    @Override
    public boolean contains(Attribute other) {
	if (!(other instanceof Service)) {
	    return false;
	}

	Service o = (Service) other;
	if (m_protocol == ANY_PROTOCOL)
	    return containsPort(o);
	if (o.m_protocol == ANY_PROTOCOL)
	    return false;
	if (!m_protocol.equals(o.m_protocol)) {
	    return false;
	}

	return containsPort(o);
    }

    /**
     * Check if this contains the other service only by port range parameters
     * 
     * @param other
     *            other service to compare to
     * @return true if this service contains the other service by only port
     *         range comparing
     */
    private boolean containsPort(Service other) {
	if (m_portRangeStart > other.m_portRangeStart)
	    return false;
	if (m_portRangeEnd < other.m_portRangeEnd)
	    return false;
	return true;
    }

    @Override
    public int hashCode() {
	return m_protocol.hashCode() + m_portRangeStart * (1 << 16) + m_portRangeEnd;
    }

    @Override
    public boolean equals(Object o) {
	if (o == null)
	    return false;
	if (!(o instanceof Service))
	    return false;

	Service other = (Service) o;
	if (m_portRangeStart != other.m_portRangeStart)
	    return false;
	if (m_portRangeEnd != other.m_portRangeEnd)
	    return false;
	if (!m_protocol.equals(other.m_protocol))
	    return false;
	return true;
    }

    @Override
    public String toString() {
	if (m_portRangeStart == ANY_PORT_START_RANGE && m_portRangeEnd == ANY_PORT_END_RANGE)
	    return "Any " + (m_protocol == ANY_PROTOCOL ? "Any " : m_protocol);
	if (m_portRangeStart == m_portRangeEnd)
	    return m_portRangeStart + (m_protocol == ANY_PROTOCOL ? "Any " : m_protocol);
	return (m_portRangeStart == ANY_PORT_START_RANGE ? "Any " : ("" + m_portRangeStart))
		+ (m_portRangeEnd == ANY_PORT_START_RANGE ? "Any " : ("" + m_portRangeEnd))
		+ (m_protocol == ANY_PROTOCOL ? "Any " : m_protocol);
    }
}
