package breakingtherules.dao;

/**
 * Service attribute
 * 
 * Have a type member and port number
 */
public class Service extends Attribute {

	/**
	 * Type of the service (Http, Https, etc)
	 */
	private String m_type;

	/**
	 * Port number of the service
	 */
	private int m_port;

	private static final String ANY_TYPE = null;

	private static final int ANY_PORT = -1;

	public Service(String service) {
		super(AttType.Service);
		// TODO from string
	}

	/**
	 * Constructor
	 * 
	 * @param type
	 */
	public Service(String type, int port) {
		super(AttType.Service);
		m_type = type;
		m_port = port;
	}

	@Override
	public boolean contain(Attribute other) {
		if (!(other instanceof Service)) {
			return false;
		}

		Service o = (Service) other;
		if (this.m_type == ANY_TYPE)
			return true;
		if (o.m_type == ANY_TYPE)
			return false;
		if (!this.m_type.equals(o.m_type)) {
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
		return m_type.hashCode() + m_port;
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
		if (!m_type.equals(other.m_type))
			return false;
		return true;
	}

}
