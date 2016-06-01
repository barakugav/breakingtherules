package breakingtherules.firewall;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import breakingtherules.utilities.Utility;
import breakingtherules.utilities.WeakCache;

public class Service extends Attribute {

    private final int m_protocolCode;

    private final int m_portsRange;

    private static final int MIN_PROTOCOL = 0;

    private static final int MAX_PROTOCOL = 255;

    /**
     * Service of protocol 'Any protocol'
     */
    public static final int ANY_PROTOCOL = MAX_PROTOCOL + 1;

    /**
     * Service that represents 'Any' service (contains all others)
     */
    public static final Service ANY_SERVICE = new AnyService();

    /**
     * The minimum legal port number
     */
    private static final int MIN_PORT = 0;

    /**
     * The maximum legal port number
     */
    private static final int MAX_PORT = (1 << 16) - 1;

    private static final int PORT_MASK = (1 << 16) - 1;

    /**
     * All names of the protocol. Name for protocol with code x is in
     * PROTOCOL_NAMES[x]
     */
    private static final String[] PROTOCOL_NAMES;

    /**
     * Map from protocol names to their codes
     */
    private static final Map<String, Integer> PROTOCOL_CODES;

    private static final String ANY = "Any";

    static {
	PROTOCOL_NAMES = new String[256];
	PROTOCOL_NAMES[0] = "HOPORT";
	PROTOCOL_NAMES[1] = "ICMP";
	PROTOCOL_NAMES[2] = "IGMP";
	PROTOCOL_NAMES[3] = "GGP";
	PROTOCOL_NAMES[4] = "IPv4";
	PROTOCOL_NAMES[5] = "ST";
	PROTOCOL_NAMES[6] = "TCP";
	PROTOCOL_NAMES[7] = "CBT";
	PROTOCOL_NAMES[8] = "EGP";
	PROTOCOL_NAMES[9] = "IGP";
	PROTOCOL_NAMES[10] = "BBN-RCC-MON";
	PROTOCOL_NAMES[11] = "NVP-II";
	PROTOCOL_NAMES[12] = "PUP";
	PROTOCOL_NAMES[13] = "ARGUS";
	PROTOCOL_NAMES[14] = "EMCON";
	PROTOCOL_NAMES[15] = "XNET";
	PROTOCOL_NAMES[16] = "CHAOS";
	PROTOCOL_NAMES[17] = "UDP";
	PROTOCOL_NAMES[18] = "MUX";
	PROTOCOL_NAMES[19] = "DCN-MEAS";
	PROTOCOL_NAMES[20] = "HMP";
	PROTOCOL_NAMES[21] = "PRM";
	PROTOCOL_NAMES[22] = "XNS-IDP";
	PROTOCOL_NAMES[23] = "TRUNK-1";
	PROTOCOL_NAMES[24] = "TRUNK-2";
	PROTOCOL_NAMES[25] = "LEAF-1";
	PROTOCOL_NAMES[26] = "LEAF-2";
	PROTOCOL_NAMES[27] = "RDP";
	PROTOCOL_NAMES[28] = "IRTP";
	PROTOCOL_NAMES[29] = "ISO-TP4";
	PROTOCOL_NAMES[30] = "NETBLT";
	PROTOCOL_NAMES[31] = "MFE-NSP";
	PROTOCOL_NAMES[32] = "MERIT-INP";
	PROTOCOL_NAMES[33] = "DCCP";
	PROTOCOL_NAMES[34] = "3PC";
	PROTOCOL_NAMES[35] = "IDPR";
	PROTOCOL_NAMES[36] = "XTP";
	PROTOCOL_NAMES[37] = "DDP";
	PROTOCOL_NAMES[38] = "IDPR-CMTP";
	PROTOCOL_NAMES[39] = "TP++";
	PROTOCOL_NAMES[40] = "IL";
	PROTOCOL_NAMES[41] = "IPv6";
	PROTOCOL_NAMES[42] = "SDRP";
	PROTOCOL_NAMES[43] = "IPv6-Route";
	PROTOCOL_NAMES[44] = "IPv6-Frag";
	PROTOCOL_NAMES[45] = "IDRP";
	PROTOCOL_NAMES[46] = "RSVP";
	PROTOCOL_NAMES[47] = "GRE";
	PROTOCOL_NAMES[48] = "DSR";
	PROTOCOL_NAMES[49] = "BNA";
	PROTOCOL_NAMES[50] = "ESP";
	PROTOCOL_NAMES[51] = "AH";
	PROTOCOL_NAMES[52] = "I-NLSP";
	PROTOCOL_NAMES[53] = "SWIPE";
	PROTOCOL_NAMES[54] = "NARP";
	PROTOCOL_NAMES[55] = "MOBILE";
	PROTOCOL_NAMES[56] = "TLSP";
	PROTOCOL_NAMES[57] = "SKIP";
	PROTOCOL_NAMES[58] = "IPv6-ICMP";
	PROTOCOL_NAMES[59] = "IPv6-NoNxt";
	PROTOCOL_NAMES[60] = "IPv6-Opts";
	PROTOCOL_NAMES[61] = "[any host internal protocol]";
	PROTOCOL_NAMES[62] = "CFTP";
	PROTOCOL_NAMES[63] = "[any local network]";
	PROTOCOL_NAMES[64] = "SAT-EXPAK";
	PROTOCOL_NAMES[65] = "KRYPTOLAN";
	PROTOCOL_NAMES[66] = "RVD";
	PROTOCOL_NAMES[67] = "IPPC";
	PROTOCOL_NAMES[68] = "";
	PROTOCOL_NAMES[69] = "SAT-MON";
	PROTOCOL_NAMES[70] = "VISA";
	PROTOCOL_NAMES[71] = "IPCV";
	PROTOCOL_NAMES[72] = "CPNX";
	PROTOCOL_NAMES[73] = "CPHB";
	PROTOCOL_NAMES[74] = "WSN";
	PROTOCOL_NAMES[75] = "PVP";
	PROTOCOL_NAMES[76] = "BR-SAT-MON";
	PROTOCOL_NAMES[77] = "SUN-ND";
	PROTOCOL_NAMES[78] = "WB-MON";
	PROTOCOL_NAMES[79] = "WB-EXPAK";
	PROTOCOL_NAMES[80] = "ISO-IP";
	PROTOCOL_NAMES[81] = "VMTP";
	PROTOCOL_NAMES[82] = "SECURE-VMTP";
	PROTOCOL_NAMES[83] = "VINES";
	PROTOCOL_NAMES[84] = "TTP";
	PROTOCOL_NAMES[84] = "IPTM"; // Two 84 protocols
	PROTOCOL_NAMES[85] = "NSFNET-IGP";
	PROTOCOL_NAMES[86] = "DGP";
	PROTOCOL_NAMES[87] = "TCF";
	PROTOCOL_NAMES[88] = "EIGRP";
	PROTOCOL_NAMES[89] = "OSPFIGP";
	PROTOCOL_NAMES[90] = "Sprite-RPC";
	PROTOCOL_NAMES[91] = "LARP";
	PROTOCOL_NAMES[92] = "MTP";
	PROTOCOL_NAMES[93] = "AX.25";
	PROTOCOL_NAMES[94] = "IPIP";
	PROTOCOL_NAMES[95] = "MICP";
	PROTOCOL_NAMES[96] = "SCC-SP";
	PROTOCOL_NAMES[97] = "ETHERIP";
	PROTOCOL_NAMES[98] = "ENCAP";
	PROTOCOL_NAMES[99] = "[any private encryption scheme]";
	PROTOCOL_NAMES[100] = "GMTP";
	PROTOCOL_NAMES[101] = "IFMP";
	PROTOCOL_NAMES[102] = "PNNI";
	PROTOCOL_NAMES[103] = "PIM";
	PROTOCOL_NAMES[104] = "ARIS";
	PROTOCOL_NAMES[105] = "SCPS";
	PROTOCOL_NAMES[106] = "QNX";
	PROTOCOL_NAMES[107] = "A/N";
	PROTOCOL_NAMES[108] = "IPComp";
	PROTOCOL_NAMES[109] = "SNP";
	PROTOCOL_NAMES[110] = "Compaq-Peer";
	PROTOCOL_NAMES[111] = "IPX-in-IP";
	PROTOCOL_NAMES[112] = "VRRP";
	PROTOCOL_NAMES[113] = "PGM";
	PROTOCOL_NAMES[114] = "[any 0-hop protocol]";
	PROTOCOL_NAMES[115] = "L2TP";
	PROTOCOL_NAMES[116] = "DDX";
	PROTOCOL_NAMES[117] = "IATP";
	PROTOCOL_NAMES[118] = "STP";
	PROTOCOL_NAMES[119] = "SRP";
	PROTOCOL_NAMES[120] = "UTI";
	PROTOCOL_NAMES[121] = "SMP";
	PROTOCOL_NAMES[122] = "SM";
	PROTOCOL_NAMES[123] = "PTP";
	PROTOCOL_NAMES[124] = "ISIS over IPv4";
	PROTOCOL_NAMES[125] = "FIRE";
	PROTOCOL_NAMES[126] = "CRTP";
	PROTOCOL_NAMES[127] = "CRUDP";
	PROTOCOL_NAMES[128] = "SSCOPMCE";
	PROTOCOL_NAMES[129] = "IPLT";
	PROTOCOL_NAMES[130] = "SPS";
	PROTOCOL_NAMES[131] = "PIPE";
	PROTOCOL_NAMES[132] = "SCTP";
	PROTOCOL_NAMES[133] = "FC";
	PROTOCOL_NAMES[134] = "RSVP-E2E-IGNORE";
	PROTOCOL_NAMES[135] = "Mobility Header";
	PROTOCOL_NAMES[136] = "UDPLite";
	PROTOCOL_NAMES[137] = "MPLS-in-IP";
	PROTOCOL_NAMES[138] = "manet";
	PROTOCOL_NAMES[139] = "HIP";
	PROTOCOL_NAMES[140] = "Shim6";
	PROTOCOL_NAMES[141] = "WESP";
	PROTOCOL_NAMES[142] = "ROHC";
	//
	// 143 - 252
	// Unassigned
	//
	PROTOCOL_NAMES[253] = "[experimentation and testing]";
	PROTOCOL_NAMES[254] = "[experimentation and testing]";
	PROTOCOL_NAMES[255] = "Reserved";

	final Map<String, Integer> map = new HashMap<>();
	for (int protocolCode = PROTOCOL_NAMES.length; protocolCode-- > 0;) {
	    final String protocolName = PROTOCOL_NAMES[protocolCode];
	    if (protocolName != null) {
		map.put(protocolName, Integer.valueOf(protocolCode));
	    }
	}
	PROTOCOL_CODES = Collections.unmodifiableMap(map);
    }

    private Service(final int protocolCode, final int portsRange) {
	m_protocolCode = protocolCode;
	m_portsRange = portsRange;
    }

    /**
     * Get the protocol of the service
     * 
     * @return String protocol of the service
     */
    public String getProtocol() {
	return protocolName(m_protocolCode);
    }

    /**
     * Get the code of this service protocol
     * 
     * @return the protocol's code
     */
    @JsonIgnore
    public int getProtocolCode() {
	return m_protocolCode;
    }

    /**
     * Get the start of the port range of the service
     * 
     * @return start of the port range of the service
     */
    public int getPortRangeStart() {
	return m_portsRange & PORT_MASK;
    }

    /**
     * Get the start of the port range of the service
     * 
     * @return start of the port range of the service
     */
    public int getPortRangeEnd() {
	return (m_portsRange >> 16) & PORT_MASK;
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
	if (!(other instanceof Service)) {
	    return false;
	}

	Service o = (Service) other;
	if (m_protocolCode == ANY_PROTOCOL) {
	    return containsPort(o);
	} else if (o.m_protocolCode == ANY_PROTOCOL) {
	    return false;
	} else if (m_protocolCode != o.m_protocolCode) {
	    return false;
	}

	return containsPort(o);
    }

    /**
     * Check if this contains the other service only by port range parameters
     * 
     * @param other
     *            other service to compare to
     * @return true if this service contains the other service by only port
     *         range comparing
     */
    protected boolean containsPort(Service other) {
	return getPortRangeStart() <= other.getPortRangeStart() && other.getPortRangeEnd() <= getPortRangeEnd();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	return m_portsRange ^ m_protocolCode;
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
	} else if (!(o instanceof Service)) {
	    return false;
	}

	Service other = (Service) o;
	return m_portsRange == other.m_portsRange && m_protocolCode == other.m_protocolCode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	final int portRangeStart = getPortRangeStart();
	final int portRangeEnd = getPortRangeEnd();
	if (m_protocolCode == ANY_PROTOCOL && portRangeStart == MIN_PORT && portRangeEnd == MAX_PORT) {
	    // All ports, all protocols
	    return ANY;
	}

	if (portRangeStart == MIN_PORT && portRangeEnd == MAX_PORT) {
	    // All ports, one protocol
	    return ANY + ' ' + protocolName(m_protocolCode);
	}

	if (m_protocolCode == ANY_PROTOCOL) {
	    // Some ports, any protocol
	    if (portRangeStart == portRangeEnd) {
		return "Port " + Integer.toString(portRangeStart);
	    } else {
		return "Ports " + portRangeStart + '-' + portRangeEnd;
	    }
	}

	// Some ports, one protocol
	if (portRangeStart == portRangeEnd) {
	    return protocolName(m_protocolCode) + ' ' + Integer.toString(portRangeStart);
	} else {
	    return protocolName(m_protocolCode) + ' ' + portRangeStart + '-' + portRangeEnd;
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.Attribute#getType()
     */
    @Override
    public String getType() {
	return SERVICE_TYPE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.firewall.Attribute#getTypeId()
     */
    @Override
    public int getTypeId() {
	return SERVICE_TYPE_ID;
    }

    /**
     * Convert protocol name to it's code
     * 
     * @param protocolName
     *            name of the protocol
     * @return code of the protocol
     */
    public static int protocolCode(final String protocolName) {
	final Integer code = PROTOCOL_CODES.get(protocolName);
	if (protocolName == null || code == null)
	    throw new IllegalArgumentException("Unknown protocol: " + String.valueOf(protocolName));
	return code.intValue();
    }

    /**
     * Convert protocol code to it's name
     * 
     * @param protocolCode
     *            code of the protocol
     * @return the protocol's name
     */
    public static String protocolName(final int protocolCode) {
	if (protocolCode == ANY_PROTOCOL) {
	    return ANY;
	}
	if (protocolCode < MIN_PROTOCOL || protocolCode > MAX_PROTOCOL) {
	    throw new IllegalArgumentException(
		    "Protocol code:" + Utility.format(MIN_PROTOCOL, MAX_PROTOCOL, protocolCode));
	}
	return PROTOCOL_NAMES[protocolCode];
    }

    public static void refreshCache() {
	for (WeakCache<Integer, Service> cache : ServiceCache.cache)
	    cache.cleanCache();
    }

    public static Service create(final String protocol, final int port) {
	return create(protocolCode(protocol), port, port);
    }

    public static Service create(final String protocol, final int portRangeStart, final int portRangeEnd) {
	return create(protocolCode(protocol), portRangeStart, portRangeEnd);
    }

    public static Service create(final int protocol, final int port) {
	return create(protocol, port, port);
    }

    public static Service create(final int protocol, final int portRangeStart, final int portRangeEnd) {
	if (protocol != ANY_PROTOCOL && (protocol < 0 || protocol > 255)) {
	    throw new IllegalArgumentException("protocol should be in range [-1, 255]: " + protocol);
	} else if (portRangeStart < MIN_PORT || portRangeStart > MAX_PORT) {
	    throw new IllegalArgumentException("Port not in range: " + portRangeStart + ". should be in range ["
		    + MIN_PORT + ", " + MAX_PORT + "]");
	} else if (portRangeEnd < MIN_PORT || portRangeEnd > MAX_PORT) {
	    throw new IllegalArgumentException(
		    "Port not in range: " + portRangeEnd + ". should be in range [" + MIN_PORT + ", " + MAX_PORT + "]");
	} else if (portRangeStart > portRangeEnd) {
	    throw new IllegalArgumentException("portRangeStart > portRangeEnd");
	}
	return createInternal(protocol, (portRangeEnd << 16) | portRangeStart);
    }

    public static Service create(String service) {
	if (service == null) {
	    throw new IllegalArgumentException("Null args");
	}

	// If Any Service
	if (service.equals(ANY) || service.equals("Any Any")) {
	    return createInternal(ANY_PROTOCOL, (MAX_PORT << 16) | MIN_PORT);
	}

	// If Any port, specific protocol
	if (service.length() > ANY.length() && service.substring(0, ANY.length()).equals(ANY)) {
	    int portRangeStart = MIN_PORT;
	    int portRangeEnd = MAX_PORT;
	    String protString = service.substring(ANY.length() + 1); // +1 for
								     // the
								     // space
	    if (protString.matches("[a-zA-Z]+")) {
		return createInternal(protocolCode(protString), (portRangeEnd << 16) | portRangeStart);
	    }
	    throw new IllegalArgumentException("Bad protocol name in 'any port' option.");
	}

	// Service is in the format "[Protocol] [Port(s)]"

	// Find separators
	final int numOfSeparators = Utility.countOccurrencesOf(service, ' ') + Utility.countOccurrencesOf(service, '-');
	final int separatorIndex = service.indexOf(' ');
	final int portSeparatorIndex = service.indexOf('-');

	int portRangeStart;
	int portRangeEnd;

	switch (numOfSeparators) {
	case 0:
	    throw new IllegalArgumentException("Unknown format, missing port or protocol");
	case 1:
	    // only one port number
	    final String portStr = service.substring(separatorIndex + 1);
	    if (portStr.equals(ANY)) {
		portRangeStart = MIN_PORT;
		portRangeEnd = MAX_PORT;
	    } else {
		final int port = Integer.parseInt(portStr);
		if (port != ANY_PROTOCOL && (port < MIN_PORT || port > MAX_PORT)) {
		    throw new IllegalArgumentException("Service port: " + Utility.format(MIN_PORT, MAX_PORT, port));
		}
		portRangeStart = portRangeEnd = port;
	    }
	    service = service.substring(0, separatorIndex);
	    break;
	case 2:
	    // start port
	    final String portRangeStartStr = service.substring(separatorIndex + 1, portSeparatorIndex);
	    if (portRangeStartStr.equals(ANY)) {
		portRangeStart = MIN_PORT;
	    } else {
		final int port = Integer.parseInt(portRangeStartStr);
		if (port != ANY_PROTOCOL && (port < MIN_PORT || port > MAX_PORT)) {
		    throw new IllegalArgumentException("Service port: " + Utility.format(MIN_PORT, MAX_PORT, port));
		}
		portRangeStart = port;
	    }

	    // end port
	    String portRangeEndStr = service.substring(portSeparatorIndex + 1);
	    if (portRangeEndStr.equals(ANY)) {
		portRangeEnd = MAX_PORT;
	    } else {
		final int port = Integer.parseInt(portRangeEndStr);
		if (port != ANY_PROTOCOL && (port < MIN_PORT || port > MAX_PORT)) {
		    throw new IllegalArgumentException("Service port: " + Utility.format(MIN_PORT, MAX_PORT, port));
		}
		portRangeEnd = port;
	    }

	    service = service.substring(0, separatorIndex);
	    break;
	default:
	    throw new IllegalArgumentException("Unknow format: too many words");

	}

	if (portRangeStart > portRangeEnd) {
	    throw new IllegalArgumentException("portRangeStart > portRangeEnd");
	} else if (service.isEmpty()) {
	    throw new IllegalArgumentException("No protocol");
	}

	int protocolCode;
	if (service.equals(ANY) || service.equals("Port") || service.equals("Ports")) {
	    protocolCode = ANY_PROTOCOL;
	} else {
	    protocolCode = protocolCode(service);
	}

	return createInternal(protocolCode, (portRangeEnd << 16) | portRangeStart);
    }

    private static Service createInternal(final int protocolCode, final int portsRange) {
	final WeakCache<Integer, Service> cache = ServiceCache.cache[protocolCode];

	// We don't use the static constructor Integer.valueOf(int)
	// intentionally here. The Integer.valueOf(int) method can help
	// performance in general cases because it will use the cached Integers
	// that are in range [-128, 127] and will not create new objects. The
	// chance that the portsRange will be in that range is negligible, so we
	// prefer to use the straight up constructor to avoid unnecessary
	// (probably) range checks.
	final Integer portsRangeInteger = new Integer(portsRange);

	Service service = cache.get(portsRangeInteger);
	if (service == null) {
	    service = new Service(protocolCode, portsRange);
	    cache.add(portsRangeInteger, service);
	}
	return service;
    }

    private static class ServiceCache {

	static final WeakCache<Integer, Service>[] cache;

	static {
	    /**
	     * Used dummy variable to suppress only warnings of cache creation
	     * and not to the whole ServiceCache class (which is necessary if we
	     * don't use temporary variable).
	     */
	    @SuppressWarnings({ "unchecked", "unused" })
	    Object dummy = cache = new WeakCache[257]; // 256 and 1 for 'any
						       // protocol'

	    for (int i = cache.length; i-- != 0;)
		cache[i] = new WeakCache<>();
	}

    }

    private static class AnyService extends Service {

	private AnyService() {
	    super(ANY_PROTOCOL, (MAX_PORT << 16) | MIN_PORT);
	}

	@Override
	public boolean contains(Attribute other) {
	    return other instanceof Service;
	}

	@Override
	protected boolean containsPort(Service other) {
	    return true;
	}

	@Override
	public String toString() {
	    return ANY;
	}

    }

}
