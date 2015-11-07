package dao;

/**
 * Destination attribute
 */
public class Destination extends Attribute {

    /**
     * Ip of the destination
     */
    private IP m_ip;

    /**
     * Constructor from string ip
     * 
     * @param ip
     *            string ip of the destination
     */
    public Destination(String ip) {
	this(IP.fromString(ip));
    }

    /**
     * Constructor
     * 
     * @param ip
     *            ip of the destination
     */
    public Destination(IP ip) {
	super(AttType.Destination);
	m_ip = ip;
    }

    /**
     * Gets the ip of the destination
     * 
     * @return ip of the destination
     */
    public IP getIp() {
	return m_ip;
    }

    @Override
    public boolean contain(Attribute other) {
	if (other == null)
	    return false;
	if (!(other instanceof Destination))
	    return false;
	
	Destination o = (Destination) other;
	return m_ip.contain(o.m_ip);
    }

}
