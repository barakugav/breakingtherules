package breakingtherules.firewall;

import breakingtherules.firewall.IP.AnyIP;

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
	if (ip == null) {
	    throw new IllegalArgumentException("Tried to create source with null ip arg");
	}
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
    public String getType() {
	return "Destination";
    }

    @Override
    public boolean contains(Attribute other) {
	if (other == null) {
	    return false;
	} else if (!(other instanceof Destination)) {
	    return false;
	}

	Destination o = (Destination) other;
	return m_ip.contains(o.m_ip);
    }

    public boolean equals(Object o) {
	if (o == this) {
	    return true;
	} else if (o == null) {
	    return false;
	} else if (!(o instanceof Destination)) {
	    return false;
	}

	Destination other = (Destination) o;
	return this.m_ip.equals(other.m_ip);
    }

    @Override
    public int hashCode() {
	return m_ip.hashCode();
    }

    @Override
    public String toString() {
	return m_ip.toString();
    }

    public static Destination createAnyDestination() {
	return new Destination(AnyIP.createNew());
    }

}
