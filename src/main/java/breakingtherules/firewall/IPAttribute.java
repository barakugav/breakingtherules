package breakingtherules.firewall;

import breakingtherules.utilities.CloneablePublic;

/**
 * The IPAttribute class represents an attribute with an IP
 */
public abstract class IPAttribute implements Attribute, Comparable<IPAttribute>, CloneablePublic {

    /**
     * IP of this attribute
     */
    private IP m_ip;

    /**
     * Constructor
     * 
     * @param ip
     *            the IP of this attribute
     */
    public IPAttribute(IP ip) {
	if (ip == null) {
	    throw new IllegalArgumentException("Tried to create source with null ip arg");
	}
	m_ip = ip;
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
	if (this == other) {
	    return true;
	} else if (other == null) {
	    return false;
	} else if (!(other instanceof IPAttribute)) {
	    return false;
	}

	IPAttribute o = (IPAttribute) other;
	return m_ip.contains(o.m_ip);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return m_ip.toString();
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
	} else if (!(o instanceof IPAttribute)) {
	    return false;
	}

	IPAttribute other = (IPAttribute) o;
	return m_ip.equals(other.m_ip);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	return m_ip.hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(IPAttribute o) {
	return m_ip.compareTo(o.m_ip);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    @Override
    public abstract IPAttribute clone();

    /**
     * Get the IP of this attribute
     * 
     * @return this attribute's IP
     */
    public IP getIp() {
	return m_ip;
    }

    /**
     * Set the IP of the attribute to a new one
     * 
     * @param ip
     *            new IP
     */
    public void setIp(IP ip) {
	m_ip = ip;
    }

}
