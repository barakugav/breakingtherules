package breakingtherules.firewall;

import org.springframework.util.StringUtils;

/**
 * Service attribute
 * 
 * Has a protocol member and port number
 */
public class Service implements Attribute {

    /**
     * Type of the service (HTTP, HTTPS, etc)
     */
    private final String m_protocol;

    /**
     * Start of the ports range
     */
    private final int m_portRangeStart;

    /**
     * End of the ports range
     */
    private final int m_portRangeEnd;

    /**
     * Service that represents 'Any' service (contains all others)
     */
    private static final Service ANY_SERVICE;

    /**
     * Service of protocol 'Any protocol'
     */
    public static final String ANY_PROTOCOL = "__AnyProtocol__";

    /**
     * The minimum legal port number
     */
    public static final int MIN_PORT = 0;

    /**
     * The maximum legal port number
     */
    public static final int MAX_PORT = (1 << 16) - 1;

    static {
	ANY_SERVICE = new Service(ANY_PROTOCOL, MIN_PORT, MAX_PORT);
    }

    /**
     * Constructor
     * 
     * @param type
     *            type of the service
     * @param port
     *            port number of the service
     */
    public Service(String protocol, int port) throws IllegalArgumentException {
	m_protocol = protocol;
	if (port < MIN_PORT || port > MAX_PORT) {
	    throw new IllegalArgumentException(
		    "Port not in range: " + port + ". should be in range [" + MIN_PORT + ", " + MAX_PORT + "]");
	}
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
	m_protocol = protocol;

	if (portRangeStart < MIN_PORT || portRangeStart > MAX_PORT) {
	    throw new IllegalArgumentException("Port not in range: " + portRangeStart + ". should be in range ["
		    + MIN_PORT + ", " + MAX_PORT + "]");
	} else if (portRangeEnd < MIN_PORT || portRangeEnd > MAX_PORT) {
	    throw new IllegalArgumentException(
		    "Port not in range: " + portRangeEnd + ". should be in range [" + MIN_PORT + ", " + MAX_PORT + "]");
	} else if (portRangeStart > portRangeEnd) {
	    throw new IllegalArgumentException("portRangeStart > portRangeEnd");
	}
	m_portRangeStart = portRangeStart;
	m_portRangeEnd = portRangeEnd;
    }

    /**
     * Constructor of String service (parse)
     * 
     * @param service
     *            String service in format ('port' 'service type')
     */
    public Service(String service) throws IllegalArgumentException {

	if (service == null) {
	    throw new IllegalArgumentException("Null args");
	}

	// If Any Service
	if (service.equals("Any") || service.equals("Any Any")) {
	    m_protocol = ANY_PROTOCOL;
	    m_portRangeStart = MIN_PORT;
	    m_portRangeEnd = MAX_PORT;
	    return;
	}

	// If Any port, specific protocol
	if (service.length() > "Any".length() && service.substring(0, "Any".length()).equals("Any")) {
	    this.m_portRangeStart = MIN_PORT;
	    this.m_portRangeEnd = MAX_PORT;
	    String protString = service.substring("Any ".length());
	    if (protString.matches("[a-zA-Z]+")) {
		this.m_protocol = protString;
		return;
	    }
	    throw new IllegalArgumentException("Bad protocol name in 'any port' option.");
	}

	// Service is in the format "[Protocol] [Port(s)]"

	// Find separators
	int numOfSeparators = 0;
	numOfSeparators += StringUtils.countOccurrencesOf(service, " ");
	numOfSeparators += StringUtils.countOccurrencesOf(service, "-");
	int separatorIndex = service.indexOf(' ');
	int portSeparatorIndex = service.indexOf('-');

	switch (numOfSeparators) {

	case 0:
	    throw new IllegalArgumentException("Unknown format, missing port or protocol");
	case 1:
	    // only one port number
	    String portStr = service.substring(separatorIndex + 1);
	    if (portStr.equals("Any")) {
		m_portRangeStart = MIN_PORT;
		m_portRangeEnd = MAX_PORT;
	    } else {
		int port = Integer.parseInt(portStr);
		if (port < MIN_PORT || port > MAX_PORT)
		    throw new IllegalArgumentException("Port should be in range [" + MIN_PORT + ", " + MAX_PORT + "]");
		m_portRangeStart = m_portRangeEnd = port;
	    }
	    service = service.substring(0, separatorIndex);
	    break;
	case 2:
	    // start port
	    String portRangeStartStr = service.substring(separatorIndex + 1, portSeparatorIndex);
	    if (portRangeStartStr.equals("Any")) {
		m_portRangeStart = MIN_PORT;
	    } else {
		int port = Integer.parseInt(portRangeStartStr);
		if (port < MIN_PORT || port > MAX_PORT)
		    throw new IllegalArgumentException("Port should be in range [" + MIN_PORT + ", " + MAX_PORT + "]");
		m_portRangeStart = port;
	    }

	    // end port
	    String portRangeEndStr = service.substring(portSeparatorIndex + 1);
	    if (portRangeEndStr.equals("Any")) {
		m_portRangeEnd = MAX_PORT;
	    } else {
		int port = Integer.parseInt(portRangeEndStr);
		if (port < MIN_PORT || port > MAX_PORT)
		    throw new IllegalArgumentException("Port should be in range [" + MIN_PORT + ", " + MAX_PORT + "]");
		m_portRangeEnd = port;
	    }

	    service = service.substring(0, separatorIndex);
	    break;
	default:
	    throw new IllegalArgumentException("Unknow format: too many words");

	}

	if (m_portRangeStart > m_portRangeEnd) {
	    throw new IllegalArgumentException("portRangeStart > portRangeEnd");
	} else if (service.equals("")) {
	    throw new IllegalArgumentException("No protocol");
	}

	if (service.equals("Any") || service.equals("Port") || service.equals("Ports"))
	    m_protocol = ANY_PROTOCOL;
	else
	    m_protocol = service;

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
	return m_portRangeEnd;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * breakingtherules.firewall.Attribute#contains(breakingtherules.firewall.
     * Attribute)
     */
    @Override
    public boolean contains(Attribute other) {
	if (!(other instanceof Service)) {
	    return false;
	}

	Service o = (Service) other;
	if (m_protocol.equals(ANY_PROTOCOL)) {
	    return containsPort(o);
	} else if (o.m_protocol.equals(ANY_PROTOCOL)) {
	    return false;
	} else if (!m_protocol.equals(o.m_protocol)) {
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
	if (m_portRangeStart > other.m_portRangeStart) {
	    return false;
	} else if (m_portRangeEnd < other.m_portRangeEnd) {
	    return false;
	}
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	return m_protocol.hashCode() + m_portRangeStart * (1 << 16) + m_portRangeEnd;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
	if (o == this) {
	    return true;
	} else if (o == null) {
	    return false;
	} else if (!(o instanceof Service)) {
	    return false;
	}

	Service other = (Service) o;
	if (m_portRangeStart != other.m_portRangeStart) {
	    return false;
	} else if (m_portRangeEnd != other.m_portRangeEnd) {
	    return false;
	} else if (!m_protocol.equals(other.m_protocol)) {
	    return false;
	}
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

	if (m_protocol.equals(ANY_PROTOCOL) && m_portRangeStart == MIN_PORT && m_portRangeEnd == MAX_PORT) {
	    // All ports, all protocols
	    return "Any";
	}

	if (m_portRangeStart == MIN_PORT && m_portRangeEnd == MAX_PORT) {
	    // All ports, one protocol
	    return "Any " + m_protocol;
	}

	if (m_protocol.equals(ANY_PROTOCOL)) {
	    // Some ports, any protocol
	    if (m_portRangeStart == m_portRangeEnd) {
		return "Port " + Integer.toString(m_portRangeStart);
	    } else {
		return "Ports " + m_portRangeStart + "-" + m_portRangeEnd;
	    }
	}

	// Some ports, one protocol
	if (m_portRangeStart == m_portRangeEnd) {
	    return m_protocol + " " + Integer.toString(m_portRangeStart);
	} else {
	    return m_protocol + " " + m_portRangeStart + "-" + m_portRangeEnd;
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.Attribute#getType()
     */
    @Override
    public String getType() {
	return SERVICE_TYPE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.Attribute#getTypeId()
     */
    public int getTypeId() {
	return SERVICE_TYPE_ID;
    }

    /**
     * Get service that represents 'Any' service (contains all others)
     * 
     * @return 'Any' service
     */
    public static Service getAnyService() {
	return ANY_SERVICE;
    }

}
