package breakingtherules.firewall;

import java.util.Objects;

/**
 * The IPAttribute class represents an attribute with an IP.
 * <p>
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @see IP
 */
public abstract class IPAttribute extends Attribute implements Comparable<IPAttribute> {

    /**
     * IP of this attribute.
     */
    private final IP m_ip;

    /**
     * Construct new IPAttribute of an IP
     *
     * @param ip
     *            the IP of this attribute
     * @throws NullPointerException
     *             if the ip is null
     */
    public IPAttribute(final IP ip) {
	m_ip = Objects.requireNonNull(ip);
    }

    /**
     * Compare the two attributes by their IPs.
     */
    @Override
    public int compareTo(final IPAttribute o) {
	return m_ip.compareTo(o.m_ip);
    }

    /**
     * {@inheritDoc}
     *
     * Return true only if the other attribute is an IPAttribute and this
     * attribute's IP contains the other attribute's IP.
     * <p>
     */
    @Override
    public boolean contains(final Attribute other) {
	if (this == other)
	    return true;
	else if (!(other instanceof IPAttribute))
	    return false;

	final IPAttribute o = (IPAttribute) other;
	return m_ip.contains(o.m_ip);
    }

    /**
     * Create a copy of this attribute with an IP mutation
     *
     * @param ip
     *            the desire change/mutation
     * @return mutation of this attribute with the IP mutation
     */
    public abstract IPAttribute createMutation(IP ip);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
	if (o == this)
	    return true;
	else if (!(o instanceof IPAttribute))
	    return false;

	final IPAttribute other = (IPAttribute) o;
	return m_ip.equals(other.m_ip);
    }

    /**
     * Get the IP of this attribute
     *
     * @return this attribute's IP
     */
    public IP getIp() {
	return m_ip;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
	return m_ip.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
	return m_ip.toString();
    }

}
