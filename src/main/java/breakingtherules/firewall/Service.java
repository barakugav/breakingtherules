package breakingtherules.firewall;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnore;

import breakingtherules.utilities.Cache;
import breakingtherules.utilities.Caches;
import breakingtherules.utilities.SoftHashCache;
import breakingtherules.utilities.Utility;

/**
 * TODO javadoc
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 */
public class Service extends Attribute {

    /**
     * Code of the service's protocol.
     * <p>
     * Value in range [{@value #MIN_PORT}, {@value #MAX_PORT}].
     */
    private final int m_protocolCode;

    /**
     * The range of the ports of this service.
     * <p>
     * The 16 lower bits of this integer is the port range start, and the 16
     * upper bits of this integer is the port range end.
     */
    private final int m_portsRange;

    /**
     * The minimum protocol code.
     */
    private static final int MIN_PROTOCOL = 0;

    /**
     * The maximum protocol code.
     */
    private static final int MAX_PROTOCOL = 255;

    /**
     * Protocol code that represent 'AnyProtocol'
     */
    public static final int ANY_PROTOCOL = MAX_PROTOCOL + 1;

    /**
     * Service that represents 'Any' service (contains all others)
     */
    public static final Service ANY_SERVICE = new AnyService();

    /**
     * The minimum legal port number.
     */
    private static final int MIN_PORT = 0;

    /**
     * The maximum legal port number.
     */
    private static final int MAX_PORT = (1 << 16) - 1; // 65535

    /**
     * Mask of 16 bits of a port from an integer.
     */
    private static final int PORT_MASK = 0xffff; // 65535

    /**
     * All names of the protocol.
     * <p>
     * Name for protocol with code x is in PROTOCOL_NAMES[x]
     */
    private static final String[] PROTOCOL_NAMES;

    /**
     * Map from protocol names to their codes
     */
    private static final Map<String, Integer> PROTOCOL_CODES;

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
	PROTOCOL_NAMES[84] = "TTP"; // TODO - two 84 protocols
	PROTOCOL_NAMES[84] = "IPTM"; // TODO - two 84 protocols
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
	map.put(ANY, Integer.valueOf(ANY_PROTOCOL));
	PROTOCOL_CODES = Collections.unmodifiableMap(map);
    }

    /**
     * Construct new Service.
     * 
     * @param protocolCode
     *            the code of the protocol
     * @param portsRange
     *            the ports range. see {@link #m_portsRange}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final Attribute other) {
	if (!(other instanceof Service)) {
	    return false;
	}

	final Service o = (Service) other;
	if (m_protocolCode != ANY_PROTOCOL) {
	    if (o.m_protocolCode == ANY_PROTOCOL) {
		return false;
	    } else if (m_protocolCode != o.m_protocolCode) {
		return false;
	    }
	}

	return getPortRangeStart() <= o.getPortRangeStart() && o.getPortRangeEnd() <= getPortRangeEnd();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
	return SERVICE_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTypeId() {
	return SERVICE_TYPE_ID;
    }

    /**
     * {@inheritDoc}
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
	return m_portsRange ^ m_protocolCode;
    }

    /**
     * {@inheritDoc}
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
	    }
	    return "Ports " + portRangeStart + '-' + portRangeEnd;
	}

	// Some ports, one protocol
	if (portRangeStart == portRangeEnd) {
	    return protocolName(m_protocolCode) + ' ' + Integer.toString(portRangeStart);
	}
	return protocolName(m_protocolCode) + ' ' + portRangeStart + '-' + portRangeEnd;
    }

    /**
     * Convert protocol name to it's code
     * 
     * @param protocolName
     *            name of the protocol
     * @return code of the protocol
     * @throws IllegalArgumentException
     *             if there is no such protocol
     */
    public static int protocolCode(final String protocolName) {
	final Integer code;
	if (protocolName == null || (code = PROTOCOL_CODES.get(protocolName)) == null) {
	    throw new IllegalArgumentException("Unknown protocol: " + String.valueOf(protocolName));
	}
	return code.intValue();
    }

    /**
     * Convert protocol code to it's name
     * 
     * @param protocolCode
     *            code of the protocol
     * @return the protocol's name
     * @throws IllegalArgumentException
     *             if there is no such protocol code
     */
    public static String protocolName(final int protocolCode) {
	if (protocolCode == ANY_PROTOCOL) {
	    return ANY;
	}
	if (protocolCode < MIN_PROTOCOL || protocolCode > MAX_PROTOCOL) {
	    throw new IllegalArgumentException(
		    "Protocol code:" + Utility.formatRange(MIN_PROTOCOL, MAX_PROTOCOL, protocolCode));
	}
	return PROTOCOL_NAMES[protocolCode];
    }

    /**
     * Get Service object parsed from string.
     * <p>
     * 
     * TODO - specified expected input format
     * 
     * @param s
     *            string representation of a service.
     * @return parse service from string.
     * 
     *         TODO - specified thrown exceptions
     */
    public static Service valueOf(String s) {

	// TODO - better implementation

	// If Any port, specific protocol
	if (s.startsWith(ANY)) {

	    // If service,equals(ANY) or "Any Any"
	    if (s.length() == ANY.length() || s.equals("Any Any")) {
		return ANY_SERVICE;
	    }
	    if (s.charAt(ANY.length()) != ' ') {
		throw new IllegalArgumentException("Expected space after 'Any': " + s);
	    }

	    final String protocolString = s.substring(ANY.length() + 1);
	    return new Service(protocolCode(protocolString), (MAX_PORT << 16) | MIN_PORT);
	}

	// Service is in the format "[Protocol] [Port(s)]"

	// Find separators
	final int numOfSeparators = Utility.countOccurrencesOf(s, ' ') + Utility.countOccurrencesOf(s, '-');
	final int separatorIndex = s.indexOf(' ');
	final int portSeparatorIndex = s.indexOf('-');

	int portRangeStart;
	int portRangeEnd;

	switch (numOfSeparators) {
	case 0:
	    throw new IllegalArgumentException("Unknown format, missing port or protocol");
	case 1:
	    // only one port number
	    final String portStr = s.substring(separatorIndex + 1);
	    if (portStr.equals(ANY)) {
		portRangeStart = MIN_PORT;
		portRangeEnd = MAX_PORT;
	    } else {
		final int port = Integer.parseInt(portStr);
		if (port != ANY_PROTOCOL && (port < MIN_PORT || port > MAX_PORT)) {
		    throw new IllegalArgumentException(
			    "Service port: " + Utility.formatRange(MIN_PORT, MAX_PORT, port));
		}
		portRangeStart = portRangeEnd = port;
	    }
	    s = s.substring(0, separatorIndex);
	    break;
	case 2:
	    // start port
	    final String portRangeStartStr = s.substring(separatorIndex + 1, portSeparatorIndex);
	    if (portRangeStartStr.equals(ANY)) {
		portRangeStart = MIN_PORT;
	    } else {
		final int port = Integer.parseInt(portRangeStartStr);
		if (port != ANY_PROTOCOL && (port < MIN_PORT || port > MAX_PORT)) {
		    throw new IllegalArgumentException(
			    "Service port: " + Utility.formatRange(MIN_PORT, MAX_PORT, port));
		}
		portRangeStart = port;
	    }

	    // end port
	    String portRangeEndStr = s.substring(portSeparatorIndex + 1);
	    if (portRangeEndStr.equals(ANY)) {
		portRangeEnd = MAX_PORT;
	    } else {
		final int port = Integer.parseInt(portRangeEndStr);
		if (port != ANY_PROTOCOL && (port < MIN_PORT || port > MAX_PORT)) {
		    throw new IllegalArgumentException(
			    "Service port: " + Utility.formatRange(MIN_PORT, MAX_PORT, port));
		}
		portRangeEnd = port;
	    }

	    s = s.substring(0, separatorIndex);
	    break;
	default:
	    throw new IllegalArgumentException("Unknow format: too many words");

	}

	if (portRangeStart > portRangeEnd) {
	    throw new IllegalArgumentException("portRangeStart > portRangeEnd");
	}
	if (s.isEmpty()) {
	    throw new IllegalArgumentException("No protocol");
	}

	int protocolCode;
	if (s.equals(ANY) || s.equals("Port") || s.equals("Ports")) {
	    protocolCode = ANY_PROTOCOL;
	} else {
	    protocolCode = protocolCode(s);
	}

	if (portRangeEnd == portRangeStart) {
	    return valueOfSinglePortServiceInternal(protocolCode, portRangeStart);
	}
	return new Service(protocolCode, (portRangeEnd << 16) | portRangeStart);
    }

    /**
     * Get Service object with the specified protocol and port.
     * 
     * @param protocol
     *            the protocol.
     * @param port
     *            the service port.
     * @return Service object with the specified protocol and port.
     * @throws IllegalArgumentException
     *             if there is no such protocol or if the port is out of range (
     *             {@link #MIN_PORT} to {@link #MAX_PORT}).
     */
    public static Service valueOf(final String protocol, final int port) {
	return valueOf(protocolCode(protocol), port, port);
    }

    /**
     * Get Service object with the specified protocol and ports range.
     * 
     * @param protocol
     *            the protocol.
     * @param portRangeStart
     *            the lower bound for the ports range
     * @param portRangeEnd
     *            the upper bound for the ports range
     * @return Service object with the specified protocol and ports range
     * @throws IllegalArgumentException
     *             if there is no such protocol , or the ports range bounds are
     *             out of range ({@value #MIN_PORT} to {@value #MAX_PORT}), if
     *             upper ports bound is lower then lower ports bound.
     */
    public static Service valueOf(final String protocol, final int portRangeStart, final int portRangeEnd) {
	return valueOf(protocolCode(protocol), portRangeStart, portRangeEnd);
    }

    /**
     * Get Service object with the specified protocol and port.
     * 
     * @param protocolCode
     *            the protocol code.
     * @param port
     *            the service port.
     * @return Service object with the specified protocol and port.
     * @throws IllegalArgumentException
     *             if there is no such protocol code or if the port is out of
     *             range ({@link #MIN_PORT} to {@link #MAX_PORT}).
     */
    public static Service valueOf(final int protocolCode, final int port) {
	if (protocolCode != ANY_PROTOCOL && (protocolCode < MIN_PROTOCOL || protocolCode > MAX_PROTOCOL)) {
	    throw new IllegalArgumentException(
		    "protocol should be in range [" + MIN_PROTOCOL + ", " + MAX_PROTOCOL + "]: " + protocolCode);
	}
	if (port < MIN_PORT || port > MAX_PORT) {
	    throw new IllegalArgumentException(
		    "Port not in range: " + port + ". should be in range [" + MIN_PORT + ", " + MAX_PORT + "]");
	}
	return valueOfSinglePortServiceInternal(protocolCode, port);
    }

    /**
     * Get Service object with the specified protocol and ports range.
     * 
     * @param protocolCode
     *            the protocol code
     * @param portRangeStart
     *            the lower bound for the ports range
     * @param portRangeEnd
     *            the upper bound for the ports range
     * @return Service object with the specified protocol and ports range
     * @throws IllegalArgumentException
     *             if there is no such protocol code, or the ports range bounds
     *             are out of range ({@value #MIN_PORT} to {@value #MAX_PORT}),
     *             if upper ports bound is lower then lower ports bound.
     */
    public static Service valueOf(final int protocolCode, final int portRangeStart, final int portRangeEnd) {
	if (protocolCode != ANY_PROTOCOL && (protocolCode < MIN_PROTOCOL || protocolCode > MAX_PROTOCOL)) {
	    throw new IllegalArgumentException(
		    "protocol should be in range [" + MIN_PROTOCOL + ", " + MAX_PROTOCOL + "]: " + protocolCode);
	}
	if (portRangeStart < MIN_PORT || portRangeStart > MAX_PORT) {
	    throw new IllegalArgumentException("Port not in range: " + portRangeStart + ". should be in range ["
		    + MIN_PORT + ", " + MAX_PORT + "]");
	}
	if (portRangeEnd < MIN_PORT || portRangeEnd > MAX_PORT) {
	    throw new IllegalArgumentException(
		    "Port not in range: " + portRangeEnd + ". should be in range [" + MIN_PORT + ", " + MAX_PORT + "]");
	}
	if (portRangeStart >= portRangeEnd) {
	    if (portRangeStart == portRangeEnd) {
		return valueOfSinglePortServiceInternal(protocolCode, portRangeStart);
	    }
	    throw new IllegalArgumentException("portRangeStart > portRangeEnd");
	}
	return new Service(protocolCode, (portRangeEnd << 16) | portRangeStart);
    }

    /**
     * Get Service object with the specified single port (not a port range),
     * used internally.
     * 
     * @param protocolCode
     *            the protocol code
     * @param port
     *            the service port
     * @return Service object of the specified protocol and ports
     */
    private static Service valueOfSinglePortServiceInternal(final int protocolCode, final int port) {
	// Intentionally using 'new Integer(int)' and not 'Integer.valueOf(int)'
	return ServiceCache.caches[protocolCode].getOrAdd(new Integer(port), ServiceCache.suppliers[protocolCode]);
    }

    /**
     * Cache of {@link Service} objects.
     * 
     * @author Barak Ugav
     * @author Yishai Gronich
     *
     */
    private static class ServiceCache {

	/**
	 * Array of service caches.
	 * <p>
	 * Each cache is contains only services with single port (not a port
	 * range) of specific protocol. Services with protocol 'x' are in cache
	 * number x.
	 */
	static final Cache<Integer, Service>[] caches;

	/**
	 * Array of suppliers for each service cache.
	 * <p>
	 * The supplier supply new service of a single port.
	 * <p>
	 * Used by {@link Cache#getOrAdd(Object, Function)}.
	 */
	static final Function<Integer, Service>[] suppliers;

	static {
	    final int numberOfCaches = 257; // 256 and 1 for 'any protocol'

	    // Used dummy to suppress warnings
	    @SuppressWarnings({ "unchecked", "unused" })
	    Object dummy = caches = new Cache[numberOfCaches];

	    // Used dummy to suppress warnings
	    @SuppressWarnings({ "unchecked", "unused" })
	    Object dummy2 = suppliers = new Function[numberOfCaches];

	    for (int i = caches.length; i-- != 0;) {
		caches[i] = Caches.synchronizedCache(new SoftHashCache<>());
	    }

	    for (int i = suppliers.length; i-- != 0;) {
		final int protocolCode = i;
		suppliers[i] = portInteger -> {
		    final int port = portInteger.intValue();
		    return new Service(protocolCode, (port << 16) | port);
		};
	    }
	}

    }

    /**
     * 'Any' service, contains all other services.
     * 
     * @author Barak Ugav
     * @author Yishai Gronich
     *
     */
    private static class AnyService extends Service {

	/**
	 * Construct new AnyService. Called once.
	 */
	AnyService() {
	    super(ANY_PROTOCOL, (MAX_PORT << 16) | MIN_PORT);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(Attribute other) {
	    return other instanceof Service;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
	    return ANY;
	}

    }

}
