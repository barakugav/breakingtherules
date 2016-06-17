package breakingtherules.firewall;

import breakingtherules.utilities.Hashs.Strategy;

class IPv4AddressStrategy implements Strategy<IPv4> {

    static final IPv4AddressStrategy INSTANCE = new IPv4AddressStrategy();

    private IPv4AddressStrategy() {
    }

    @Override
    public boolean equals(final IPv4 a, final IPv4 b) {
	return a.m_address == b.m_address;
    }

    @Override
    public int hashCode(final IPv4 k) {
	return k.m_address;
    }

}
