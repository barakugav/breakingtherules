package breakingtherules.firewall;

import breakingtherules.firewall.IP.AnyIP;

/**
 * Source attribute
 * 
 * Have IP
 */
public class Source implements Attribute {

    /**
     * IP of the source
     */
    private final IP m_ip;

    /**
     * Constructor
     * 
     * @param ip
     *            IP of the source
     */
    public Source(IP ip) throws IllegalArgumentException {
	if (ip == null) {
	    throw new IllegalArgumentException("Tried to create source with null ip arg");
	}
	m_ip = ip;
    }

    /**
     * Constructor with String IP
     * 
     * @param ip
     *            String IP of the source
     */
    public Source(String ip) throws IllegalArgumentException {
	this(IP.fromString(ip));
    }

    /**
     * Get the IP of the source
     * 
     * @return IP of the source
     */
    public IP getIP() {
	return m_ip;
    }

    @Override
    public boolean contains(Attribute other) {
	if (this == other) {
	    return true;
	} else if (other == null) {
	    return false;
	} else if (!(other instanceof Source)) {
	    return false;
	}

	Source o = (Source) other;
	return m_ip.contains(o.m_ip);
    }

    @Override
    public String toString() {
	return m_ip.toString();
    }

    public boolean equals(Object o) {
	if (o == this) {
	    return true;
	} else if (o == null) {
	    return false;
	} else if (!(o instanceof Source)) {
	    return false;
	}

	Source other = (Source) o;
	return this.m_ip.equals(other.m_ip);
    }

    @Override
    public int hashCode() {
	return m_ip.hashCode();
    }
    @Override
    public String getType() {
	return "Source";
    }

    public static Source createAnySource() {
	return new Source(AnyIP.createNew());
    }

}
