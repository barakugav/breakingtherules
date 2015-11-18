package breakingtherules.firewall;

/**
 * Destination attribute
 */
public class Destination extends Attribute {

    /**
     * IP of the destination
     */
    private IP m_ip;

    /**
     * Constructor
     * 
     * @param ip
     *            IP of the destination
     */
    public Destination(IP ip) {
	super(AttType.Destination);
	m_ip = ip;
    }

    /**
     * Constructor from string IP
     * 
     * @param ip
     *            string IP of the destination
     */
    public Destination(String ip) {
	this(IP.fromString(ip));
    }

    /**
     * Gets the IP of the destination
     * 
     * @return IP of the destination
     */
    public IP getIP() {
	return m_ip;
    }

    @Override
    public boolean contains(Attribute other) {
	if (other == null)
	    return false;
	if (!(other instanceof Destination))
	    return false;

	Destination o = (Destination) other;
	return m_ip.contain(o.m_ip);
    }

    @Override
    public String toString() {
	return m_ip.toString();
    }
}
