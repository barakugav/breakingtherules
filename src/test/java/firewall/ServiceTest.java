package firewall;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import breakingtherules.firewall.Service;

public class ServiceTest {

    static String EX_SERVICE = "TCP";
    static int EX_PORT = 80;

    static Service s;

    @BeforeClass
    public static void before() {
	s = new Service(EX_SERVICE, EX_PORT);
    }

    @Test
    public void serviceEqualToItself() {
	assertTrue(s.equals(s));
    }

    @Test
    public void serviceContainsItself() {
	assertTrue(s.contains(s));
    }

    @Test
    public void anyContainsService() {
	Service any;

	any = new Service(Service.ANY_PROTOCOL, Service.ANY_PORT);
	assertTrue(any.contains(s));

	any = new Service(EX_SERVICE, Service.ANY_PORT);
	assertTrue(any.contains(s));

	any = new Service(Service.ANY_PROTOCOL, EX_PORT);
	assertTrue(any.contains(s));

    }

}
