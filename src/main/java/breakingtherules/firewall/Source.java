package breakingtherules.firewall;

/**
 * Source attribute
 * 
 * Have IP
 */
public class Source extends Attribute {

    /**
     * IP of the source
     */
    private IP m_ip;

    /**
     * Constructor
     * 
     * @param ip
     *            IP of the source
     */
    public Source(IP ip) throws IllegalArgumentException {
	super(AttType.Source);

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
}
