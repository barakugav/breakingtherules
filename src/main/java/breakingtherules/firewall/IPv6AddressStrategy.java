package breakingtherules.firewall;

import breakingtherules.utilities.Hashs.Strategy;

class IPv6AddressStrategy implements Strategy<IPv6> {

    static final IPv6AddressStrategy INSTANCE = new IPv6AddressStrategy();

    private IPv6AddressStrategy() {
    }

    @Override
    public boolean equals(final IPv6 a, final IPv6 b) {
	for (int i = IPv6.ADDRESS_ARRAY_SIZE; i-- != 0;)
	    if (a.m_address[i] != b.m_address[i])
		return false;
	return true;
    }

    @Override
    public int hashCode(final IPv6 k) {
	int h = 17;
	for (int i = IPv6.ADDRESS_ARRAY_SIZE; i-- != 0;)
	    h = h * 31 + k.m_address[i];
	return h;
    }

}
