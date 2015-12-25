package breakingtherules.firewall;

/**
 * Destination attribute
 */
public class Destination implements Attribute {

    /**
     * IP of the destination
     */
    private final IP m_ip;

    /**
     * Constructor
     * 
     * @param ip
     *            IP of the destination
     */
    public Destination(IP ip) throws IllegalArgumentException {
	if (ip == null)
	    throw new IllegalArgumentException("Tried to create source with null ip arg");
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
	return m_ip.contains(o.m_ip);
    }

    @Override
    public String toString() {
	return m_ip.toString();
    }
    
    @Override
    public boolean equals(Object o) {
	if (!(o instanceof Destination))
	    return false;
	return this.m_ip.equals(((Destination)o).m_ip);
    }
    @Override
    public int hashCode() {
	return m_ip.hashCode();
    }

    @Override
    public String getType() {
	return "Destination";
    }

    public static Destination createAnySourceIPv4() {
	return new Destination(IPv4.createAnyIPv4());
    }

    public static Destination createAnySourceIPv6() {
	return new Destination(IPv6.createAnyIPv6());
    }

}
