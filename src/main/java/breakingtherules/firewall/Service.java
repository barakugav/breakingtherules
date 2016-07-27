package breakingtherules.firewall;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;

import com.fasterxml.jackson.annotation.JsonIgnore;

import breakingtherules.util.Int2ObjectCache;
import breakingtherules.util.Int2ObjectOpenAddressingHashCache;
import breakingtherules.util.Utility;

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
    private final short m_protocolCode;

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
    public static final short MIN_PROTOCOL = 0;

    /**
     * The maximum protocol code.
     */
    public static final short MAX_PROTOCOL = 255;

    /**
     * Protocol code that represent 'AnyProtocol'
     */
    public static final short ANY_PROTOCOL = -1;

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
     * Max number of digits of a protocol code in base 10.
     */
    private static final int MAX_PROTOCOL_CODE_DIGITS_NUMBER = Utility.digitsCount(MAX_PROTOCOL); // 3

    /**
     * Max number of digits of a port in base 10.
     */
    private static final int MAX_PORT_DIGITIS_NUMBER = Utility.digitsCount(MAX_PORT); // 5

    /**
     * All names of the protocol.
     * <p>
     * Name for protocol with code x is in PROTOCOL_NAMES[x]
     */
    private static final String[] PROTOCOL_NAMES;

    /**
     * Map from protocol names to their codes
     */
    private static final Map<String, Short> PROTOCOL_CODES;

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
	PROTOCOL_NAMES[61] = "[any-host-internal-protocol]";
	PROTOCOL_NAMES[62] = "CFTP";
	PROTOCOL_NAMES[63] = "[any-local-network]";
	PROTOCOL_NAMES[64] = "SAT-EXPAK";
	PROTOCOL_NAMES[65] = "KRYPTOLAN";
	PROTOCOL_NAMES[66] = "RVD";
	PROTOCOL_NAMES[67] = "IPPC";
	PROTOCOL_NAMES[68] = "[any-distributed-file-system]";
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
	PROTOCOL_NAMES[99] = "[any-private-encryption-scheme]";
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
	PROTOCOL_NAMES[114] = "[any-0-hop-protocol]";
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
	for (int i = 253; i-- != 143;)
	    PROTOCOL_NAMES[i] = "Unassigned";
	//
	// 143 - 252
	// Unassigned
	//
	PROTOCOL_NAMES[253] = "[experimentation-and-testing]";
	PROTOCOL_NAMES[254] = "[experimentation-and-testing]";
	PROTOCOL_NAMES[255] = "Reserved";

	final Map<String, Short> map = new HashMap<>();
	for (short protocolCode = (short) PROTOCOL_NAMES.length; protocolCode-- > 0;) {
	    final String protocolName = PROTOCOL_NAMES[protocolCode];
	    if (protocolName != null)
		map.put(protocolName, Short.valueOf(protocolCode));
	}
	map.put(ANY, Short.valueOf(ANY_PROTOCOL));
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
    private Service(final short protocolCode, final int portsRange) {
	m_protocolCode = protocolCode;
	m_portsRange = portsRange;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final Attribute other) {
	if (!(other instanceof Service))
	    return false;

	final Service o = (Service) other;
	if (m_protocolCode != ANY_PROTOCOL) {
	    if (o.m_protocolCode == ANY_PROTOCOL)
		return false;
	    if (m_protocolCode != o.m_protocolCode)
		return false;
	}

	return getPortRangeStart() <= o.getPortRangeStart() && o.getPortRangeEnd() <= getPortRangeEnd();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
	if (o == this)
	    return true;
	else if (!(o instanceof Service))
	    return false;

	final Service other = (Service) o;
	return m_portsRange == other.m_portsRange && m_protocolCode == other.m_protocolCode;
    }

    /**
     * Get the start of the port range of the service
     *
     * @return start of the port range of the service
     */
    public int getPortRangeEnd() {
	return m_portsRange >> 16 & PORT_MASK;
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
    public short getProtocolCode() {
	return m_protocolCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributeType getType() {
	return AttributeType.SERVICE;
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
	if (m_protocolCode == ANY_PROTOCOL && portRangeStart == MIN_PORT && portRangeEnd == MAX_PORT)
	    // All ports, all protocols
	    return ANY;

	if (portRangeStart == MIN_PORT && portRangeEnd == MAX_PORT)
	    // All ports, one protocol
	    return protocolName(m_protocolCode) + ' ' + ANY;

	if (m_protocolCode == ANY_PROTOCOL) {
	    // Some ports, any protocol
	    if (portRangeStart == portRangeEnd)
		return "Any " + Integer.toString(portRangeStart);
	    return "Any " + portRangeStart + '-' + portRangeEnd;
	}

	// Some ports, one protocol
	if (portRangeStart == portRangeEnd)
	    return protocolName(m_protocolCode) + ' ' + Integer.toString(portRangeStart);
	return protocolName(m_protocolCode) + ' ' + portRangeStart + '-' + portRangeEnd;
    }

    /**
     * Parses string to port.
     * <p>
     * This method is faster then {@link Integer#parseInt(String)} when parsing
     * ports.
     *
     * @param s
     *            the string.
     * @return port parsed from string.
     * @throws NullPointerException
     *             if the string is null.
     * @throws IllegalArgumentException
     *             if the string is invalid port number.
     */
    public static int parsePort(final String s) {
	return Utility.parsePositiveIntUncheckedOverflow(s, 0, s.length(), MAX_PORT_DIGITIS_NUMBER);
    }

    /**
     * Parses string to protocol code.
     * <p>
     * This method is faster then {@link Integer#parseInt(String)} when parsing
     * protocol codes.
     *
     * @param s
     *            the string.
     * @return protocol code parsed from string.
     * @throws NullPointerException
     *             if the string is null.
     * @throws IllegalArgumentException
     *             if failed to parse to protocol code.
     */
    public static short parseProtocolCode(final String s) {
	return (short) Utility.parsePositiveIntUncheckedOverflow(s, 0, s.length(), MAX_PROTOCOL_CODE_DIGITS_NUMBER);
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
    public static short protocolCode(final String protocolName) {
	final Short code;
	if (protocolName == null || (code = PROTOCOL_CODES.get(protocolName)) == null)
	    throw new IllegalArgumentException("Unknown protocol: " + String.valueOf(protocolName));
	return code.shortValue();
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
    public static String protocolName(final short protocolCode) {
	if (protocolCode == ANY_PROTOCOL)
	    return ANY;
	if (protocolCode < MIN_PROTOCOL || protocolCode > MAX_PROTOCOL)
	    throw new IllegalArgumentException(
		    "Protocol code:" + Utility.formatRange(MIN_PROTOCOL, MAX_PROTOCOL, protocolCode));
	return PROTOCOL_NAMES[protocolCode];
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
    public static Service valueOf(final short protocolCode, final int port) {
	checkProtocol(protocolCode);
	checkPort(port);
	return new Service(protocolCode, toPortsRange(port));
    }

    /**
     * Get Service object with the specified protocol and ports range.
     *
     * @param protocolCode
     *            the protocol code
     * @param portRangeStart
     *            the lower bound for the ports range
     * @param portRangeEnd
     *            the upper bound for the ports range.
     * @return Service object with the specified protocol and ports range
     * @throws IllegalArgumentException
     *             if there is no such protocol code, or the ports range bounds
     *             are out of range ({@value #MIN_PORT} to {@value #MAX_PORT}),
     *             if upper ports bound is lower then lower ports bound.
     */
    public static Service valueOf(final short protocolCode, final int portRangeStart, final int portRangeEnd) {
	return valueOf(protocolCode, portRangeStart, portRangeEnd, null);
    }

    /**
     * Get Service object with the specified protocol and ports range.
     * <p>
     * If the cache isn't null, will used the cached Service from the cache if
     * one exist, or will create a new one and cache it to the cache otherwise.
     * <p>
     *
     * @param protocolCode
     *            the protocol code.
     * @param portRangeStart
     *            the lower bound for the ports range.
     * @param portRangeEnd
     *            the upper bound for the ports range.
     * @param cache
     *            the cached containing cached Service objects. Can be null.
     * @return Service object with the specified protocol and ports range
     * @throws IllegalArgumentException
     *             if there is no such protocol code, or the ports range bounds
     *             are out of range ({@value #MIN_PORT} to {@value #MAX_PORT}),
     *             if upper ports bound is lower then lower ports bound.
     */
    public static Service valueOf(final short protocolCode, final int portRangeStart, final int portRangeEnd,
	    final Service.Cache cache) {
	checkProtocol(protocolCode);
	checkPort(portRangeStart);
	checkPort(portRangeEnd);

	if (portRangeStart >= portRangeEnd) {
	    if (portRangeStart == portRangeEnd)
		return valueOfInternal(protocolCode, portRangeStart, cache);
	    throw new IllegalArgumentException("portRangeStart > portRangeEnd");
	}
	return new Service(protocolCode, toPortsRange(portRangeStart, portRangeEnd));
    }

    /**
     * Get Service object with the specified protocol and port.
     * <p>
     * If the cache isn't null, will used the cached Service from the cache if
     * one exist, or will create a new one and cache it to the cache otherwise.
     * <p>
     *
     * @param protocolCode
     *            the protocol code.
     * @param port
     *            the service port.
     * @param cache
     *            the cached containing cached Service objects. Can be null.
     * @return Service object with the specified protocol and port.
     * @throws IllegalArgumentException
     *             if there is no such protocol code or if the port is out of
     *             range ({@link #MIN_PORT} to {@link #MAX_PORT}).
     */
    public static Service valueOf(final short protocolCode, final int port, final Service.Cache cache) {
	checkProtocol(protocolCode);
	checkPort(port);
	return valueOfInternal(protocolCode, port, cache);
    }

    /**
     * Get Service object parsed from string.
     * <p>
     * The expected format of the string is [protocol] [port/ports], where
     * [protocol] is the protocol name (for example TCP) and the [port/ports]
     * are the service port or ports range, which are expected to be in format
     * [lowerPortsBound-upperPortsBound]. The protocol can be 'Any'. One of the
     * ports can be 'Any', or both. If there only one port, and his value is
     * 'Any' it's equivalent to 'Any-Any' value. The whole string can be 'Any',
     * which is equivalent to 'Any Any-Any'.
     * <p>
     *
     * @param s
     *            string representation of a service.
     * @return parse service from string.
     * @throws NullPointerException
     *             if the string is null.
     * @throws IllegalArgumentException
     *             if the format is invalid (as specified above), or the ports
     *             are not in range, or there is no such protocol as specified,
     *             or if the lower ports bound is greater then the upper ports
     *             bound.
     */
    public static Service valueOf(final String s) {
	return valueOf(s, null);
    }

    /**
     * Get Service object parsed from string.
     * <p>
     * The expected format of the string is [protocol] [port/ports], where
     * [protocol] is the protocol name (for example TCP) and the [port/ports]
     * are the service port or ports range, which are expected to be in format
     * [lowerPortsBound-upperPortsBound]. The protocol can be 'Any'. One of the
     * ports can be 'Any', or both. If there only one port, and his value is
     * 'Any' it's equivalent to 'Any-Any' value. The whole string can be 'Any',
     * which is equivalent to 'Any Any-Any'.
     * <p>
     * If the cache isn't null, will used the cached Service from the cache if
     * one exist, or will create a new one and cache it to the cache otherwise.
     * <p>
     *
     * @param s
     *            string representation of a service.
     * @param cache
     *            the cached containing cached Service objects. Can be null.
     * @return parse service from string.
     * @throws NullPointerException
     *             if the string is null.
     * @throws IllegalArgumentException
     *             if the format is invalid (as specified above), or the ports
     *             are not in range, or there is no such protocol as specified,
     *             or if the lower ports bound is greater then the upper ports
     *             bound.
     */
    public static Service valueOf(final String s, final Service.Cache cache) {
	int separatorIndex = s.indexOf(Utility.SPACE);
	if (separatorIndex <= 0) {
	    if (s.equals(ANY))
		return ANY_SERVICE;
	    throw new IllegalArgumentException("Unkown format: " + s);
	}

	// Parse protocol code
	final short protocolCode = protocolCode(s.substring(0, separatorIndex));

	int fromIndex = separatorIndex + 1;
	separatorIndex = s.indexOf('-', fromIndex);

	if (separatorIndex < 0) {
	    // Only one port
	    if (s.startsWith(ANY, fromIndex))
		return new Service(protocolCode, toPortsRange(MIN_PORT, MAX_PORT));

	    final int port = Utility.parsePositiveIntUncheckedOverflow(s, fromIndex, s.length(),
		    MAX_PORT_DIGITIS_NUMBER);
	    checkPort(port);
	    return valueOfInternal(protocolCode, port, cache);
	}

	// Two ports

	// First port
	final int portRangeStart;
	if (s.startsWith(ANY, fromIndex))
	    portRangeStart = MIN_PORT;
	else {
	    portRangeStart = Utility.parsePositiveIntUncheckedOverflow(s, fromIndex, separatorIndex,
		    MAX_PORT_DIGITIS_NUMBER);
	    checkPort(portRangeStart);
	}
	fromIndex = separatorIndex + 1;

	// Second port
	final int portRangeEnd;
	if (s.startsWith(ANY, fromIndex))
	    portRangeEnd = MAX_PORT;
	else {
	    portRangeEnd = Utility.parsePositiveIntUncheckedOverflow(s, fromIndex, s.length(), MAX_PORT_DIGITIS_NUMBER);
	    checkPort(portRangeEnd);
	}

	if (portRangeStart >= portRangeEnd) {
	    if (portRangeStart == portRangeEnd)
		return valueOfInternal(protocolCode, portRangeStart, cache);
	    throw new IllegalArgumentException("portRangeStart > portRangeEnd");
	}
	return new Service(protocolCode, toPortsRange(portRangeStart, portRangeEnd));
    }

    /**
     * Checks that a port is valid.
     *
     * @param port
     *            the checked port code.
     * @throws IllegalArgumentException
     *             if the port is not valid.
     */
    private static void checkPort(final int port) {
	if (port < MIN_PORT || port > MAX_PORT)
	    throw new IllegalArgumentException("Service port: " + Utility.formatRange(MIN_PORT, MAX_PORT, port));
    }

    /**
     * Checks that the protocol code is valid.
     *
     * @param protocolCode
     *            the checked protocol code.
     * @throws IllegalArgumentException
     *             if the protocol code is not valid.
     */
    private static void checkProtocol(final short protocolCode) {
	if (protocolCode != ANY_PROTOCOL && (protocolCode < MIN_PROTOCOL || protocolCode > MAX_PROTOCOL))
	    throw new IllegalArgumentException(
		    "Service protocol: " + Utility.formatRange(MIN_PROTOCOL, MAX_PROTOCOL, protocolCode));
    }

    /**
     * Convert a port to a integer in 'ports range' formant.
     *
     * @param port
     *            the single port.
     * @return 'ports range' integer of the port.
     *
     * @see Service#m_portsRange
     */
    private static int toPortsRange(final int port) {
	return toPortsRange(port, port);
    }

    /**
     * Convert a two integers of ports range to one integer in 'ports range'
     * formant.
     *
     * @param portRangeStart
     *            the start of the ports range.
     * @param portRangeEnd
     *            the end of the ports range.
     * @return 'ports range' integer of the two integers ports.
     *
     * @see Service#m_portsRange
     */
    private static int toPortsRange(final int portRangeStart, final int portRangeEnd) {
	return portRangeEnd << 16 | portRangeStart;
    }

    /**
     * Get Service object with the specified single port (not a port range),
     * used internally.
     * <p>
     * If the cache isn't null, will used the cached Service from the cache if
     * one exist, or will create a new one and cache it to the cache otherwise.
     * <p>
     *
     * @param protocolCode
     *            the protocol code.
     * @param port
     *            the service port.
     * @param cache
     *            the cached containing cached Service objects. Can be null.
     * @return Service object of the specified protocol and ports.
     */
    private static Service valueOfInternal(final short protocolCode, final int port, final Service.Cache cache) {
	return cache != null ? cache.caches[protocolCode + 1].getOrAdd(port, cache.mappingFunction[protocolCode + 1])
		: new Service(protocolCode, toPortsRange(port));
    }

    /**
     * Cache of {@link Service} objects.
     *
     * @author Barak Ugav
     * @author Yishai Gronich
     *
     */
    public final static class Cache {

	/**
	 * Array of service caches.
	 * <p>
	 * Each cache is contains only services with single port (not a port
	 * range) of specific protocol. Services with protocol 'x' are in cache
	 * number x.
	 */
	private final Int2ObjectCache<Service>[] caches;

	/**
	 * Array of mapping function for each service cache.
	 * <p>
	 * The functions supply new service of a single port.
	 * <p>
	 *
	 * Used by
	 * {@link breakingtherules.util.Object2ObjectCache#getOrAdd(Object, Function)}
	 */
	private final IntFunction<Service>[] mappingFunction;

	/**
	 * Construct new Services cache.
	 */
	public Cache() {
	    final short numberOfCaches = 257; // 256 and 1 for 'any protocol'

	    // Used dummy to suppress warnings
	    @SuppressWarnings({ "unchecked", "unused" })
	    final Object dummy = caches = new Int2ObjectCache[numberOfCaches];

	    // Used dummy to suppress warnings
	    @SuppressWarnings({ "unchecked", "unused" })
	    final Object dummy2 = mappingFunction = new IntFunction[numberOfCaches];

	    for (int i = caches.length; i-- != 0;)
		caches[i] = new Int2ObjectOpenAddressingHashCache<>();

	    for (short i = numberOfCaches; i-- != 0;) {
		final short protocolCode = (short) (i - 1);
		mappingFunction[i] = port -> new Service(protocolCode, toPortsRange(port));
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
	    super(ANY_PROTOCOL, toPortsRange(MIN_PORT, MAX_PORT));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean contains(final Attribute other) {
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
