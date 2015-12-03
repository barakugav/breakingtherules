package breakingtherules.firewall.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import breakingtherules.firewall.IP;
import breakingtherules.firewall.IPv4;

public class IPTest {

    @Test
    public void createIpFromString() {
	String ipString = "IPv4 192.68.0.0/24";
	IP ip1 = IP.fromString(ipString);
	IP ip2 = new IPv4(ipString);
	assertTrue(ip1.contain(ip2) && ip2.contain(ip1));
    }
    
    
}
