package breakingtherules.firewall;

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
	if (ip == null)
	    throw new IllegalArgumentException("Tried to create source with null ip arg");
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
	if (other == null)
	    return false;
	if (!(other instanceof Source))
	    return false;

	Source o = (Source) other;
	return m_ip.contains(o.m_ip);
    }

    @Override
    public String toString() {
	return m_ip.toString();
    }

    @Override
    public String getType() {
	return "Source";
    }

    public static Source createAnySourceIPv4() {
	return new Source(IPv4.createAnyIPv4());
    }

    public static Source createAnySourceIPv6() {
	return new Source(IPv6.createAnyIPv6());
    }

}
