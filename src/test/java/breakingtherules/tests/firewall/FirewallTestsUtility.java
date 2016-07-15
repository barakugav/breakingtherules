package breakingtherules.tests.firewall;

import java.util.ArrayList;
import java.util.List;

import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.IP;
import breakingtherules.firewall.IPv4;
import breakingtherules.firewall.IPv6;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;
import breakingtherules.tests.TestBase;

@SuppressWarnings("javadoc")
public class FirewallTestsUtility extends TestBase {

    public static int[] getRandomAddressIPv4() {
	final int[] address = new int[4];
	for (int i = 0; i < address.length; i++)
	    address[i] = rand.nextInt(1 << 8);
	return address;
    }

    public static int[] getRandomAddressIPv6() {
	final int[] address = new int[8];
	for (int i = 0; i < address.length; i++)
	    address[i] = rand.nextInt(1 << 16);
	return address;
    }

    public static List<Attribute> getRandomAttributes() {
	final List<Attribute> attributes = new ArrayList<>();
	attributes.add(getRandomSource());
	attributes.add(getRandomDestination());
	attributes.add(getRandomService());
	return attributes;
    }

    public static Destination getRandomDestination() {
	return Destination.valueOf(FirewallTestsUtility.getRandomIP());
    }

    public static int getRandomID() {
	return rand.nextInt((1 << 31) - 1) + 1; // only positive numbers
    }

    public static IP getRandomIP() {
	final int ipID = rand.nextInt(2) * 2 + 4; // 4 or 6
	final int[] address = new int[ipID * 2 - 4]; // 4 or 8

	for (int i = 0; i < address.length; i++)
	    address[i] = rand.nextInt(1 << ipID * 4 - 8); // rand(256) or
							  // rand(65536)

	final short prefixLength = (short) rand.nextInt(ipID * 48 - 160); // 32
									  // or
									  // 128

	if (ipID == 4)
	    return IPv4.valueOf(address, prefixLength);
	else if (ipID == 6)
	    return IPv6.valueOf(address, prefixLength);
	else
	    return null;
    }

    public static IPv4 getRandomIPv4() {
	final int[] address = getRandomAddressIPv4();
	final short prefixLength = getRandomMaskSizeIPv4();
	return IPv4.valueOf(address, prefixLength);
    }

    public static IPv6 getRandomIPv6() {
	final int[] address = getRandomAddressIPv6();
	final short prefixLength = getRandomMaskSizeIPv6();
	return IPv6.valueOf(address, prefixLength);
    }

    public static short getRandomMaskSizeIPv4() {
	return (short) rand.nextInt(33);
    }

    public static short getRandomMaskSizeIPv6() {
	return (short) rand.nextInt(129);
    }

    public static int getRandomPort() {
	return rand.nextInt(1 << 16);
    }

    public static int[] getRandomPortRange() {
	int rangeStart, rangeEnd;

	do {
	    rangeStart = getRandomPort();
	    rangeEnd = getRandomPort();
	} while (!(rangeStart < rangeEnd));

	return new int[] { rangeStart, rangeEnd };
    }

    public static short getRandomProtocolCode() {
	return (short) rand.nextInt(100);
    }

    public static Service getRandomService() {
	final short protocolCode = getRandomProtocolCode();

	int portRangeStart, portRangeEnd;
	do {
	    portRangeStart = getRandomPort();
	    portRangeEnd = getRandomPort();
	} while (portRangeStart > portRangeEnd);

	return Service.valueOf(protocolCode, portRangeStart, portRangeEnd);
    }

    public static Source getRandomSource() {
	return Source.valueOf(FirewallTestsUtility.getRandomIP());
    }

    /**
     * Convert an <code>int</code> number to a booleans array that represents
     * the number by bits
     *
     * @param num
     *            the number to convert
     * @param length
     *            requested boolean array length
     * @return boolean array that represents the number
     */
    public static boolean[] intToBooleans(int num, final int length) {
	if (length < 0)
	    throw new IllegalArgumentException("length can't be negaive " + length);

	final boolean[] result = new boolean[length];
	for (int i = 0; i < length; i++) {
	    result[result.length - i - 1] = (num & 1) == 1;
	    num >>= 1;
	}

	return result;
    }

    /**
     * Merge a set of boolean arrays to one
     *
     * @param arrays
     *            some boolean arrays
     * @return new boolean array that is a merge of all others
     */
    public static boolean[] merge(final boolean[]... arrays) {
	int length = 0;
	for (final boolean[] array : arrays)
	    length += array.length;

	final boolean[] result = new boolean[length];
	int offset = 0;
	for (final boolean[] array : arrays) {
	    System.arraycopy(array, 0, result, offset, array.length);
	    offset += array.length;
	}

	return result;
    }

}
