package breakingtherules.tests.firewall;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.IP;
import breakingtherules.firewall.IPv4;
import breakingtherules.firewall.IPv6;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;
import breakingtherules.tests.TestBase;

class FirewallTestsUtility extends TestBase {

    private static final Random rand = new Random();

    static IP getRandomIP() {
	int ipID = rand.nextInt(2) * 2 + 4; // 4 or 6
	int[] address = new int[ipID * 2 - 4]; // 4 or 8

	for (int i = 0; i < address.length; i++) {
	    address[i] = rand.nextInt(1 << (ipID * 4 - 8)); // rand(256) or
							    // rand(65536)
	}

	int prefixLength = rand.nextInt(ipID * 48 - 160); // 32 or 128

	if (ipID == 4) {
	    return IPv4.create(address, prefixLength);
	} else if (ipID == 6) {
	    return IPv6.create(address, prefixLength);
	} else {
	    return null;
	}
    }

    static int getRandomID() {
	return rand.nextInt((1 << 31) - 1) + 1; // only positive numbers
    }

    static List<Attribute> getRandomAttributes() {
	List<Attribute> attributes = new ArrayList<>();
	attributes.add(getRandomSource());
	attributes.add(getRandomDestination());
	attributes.add(getRandomService());
	return attributes;
    }

    static Source getRandomSource() {
	return Source.create(FirewallTestsUtility.getRandomIP());
    }

    static Destination getRandomDestination() {
	return Destination.create(FirewallTestsUtility.getRandomIP());
    }

    static Service getRandomService() {
	String protocol;
	if (rand.nextBoolean()) {
	    protocol = "TCP";
	} else {
	    protocol = "UDP";
	}

	int portRangeStart, portRangeEnd;
	do {
	    portRangeStart = rand.nextInt(1 << 16);
	    portRangeEnd = rand.nextInt(1 << 16);
	} while (portRangeStart > portRangeEnd);

	return Service.create(protocol, portRangeStart, portRangeEnd);
    }

    static int getRandomPort() {
	return rand.nextInt(1 << 16);
    }

    static int[] getRandomPortRange() {
	int rangeStart, rangeEnd;

	do {
	    rangeStart = getRandomPort();
	    rangeEnd = getRandomPort();
	} while (!(rangeStart < rangeEnd));

	return new int[] { rangeStart, rangeEnd };
    }

    static IPv4 getRandomIPv4() {
	int[] address = getRandomAddressIPv4();
	int prefixLength = getRandomPrefixLengthIPv4();
	return IPv4.create(address, prefixLength);
    }

    static int[] getRandomAddressIPv4() {
	int[] address = new int[4];
	for (int i = 0; i < address.length; i++) {
	    address[i] = rand.nextInt(1 << 8);
	}
	return address;
    }

    static int getRandomPrefixLengthIPv4() {
	return rand.nextInt(33);
    }

    static IPv6 getRandomIPv6() {
	int[] address = getRandomAddressIPv6();
	int prefixLength = getRandomPrefixLengthIPv6();
	return IPv6.create(address, prefixLength);
    }

    static int[] getRandomAddressIPv6() {
	int[] address = new int[8];
	for (int i = 0; i < address.length; i++) {
	    address[i] = rand.nextInt(1 << 16);
	}
	return address;
    }

    static int getRandomPrefixLengthIPv6() {
	return rand.nextInt(129);
    }

}
