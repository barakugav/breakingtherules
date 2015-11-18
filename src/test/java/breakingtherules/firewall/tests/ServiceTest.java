package breakingtherules.firewall.tests;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import breakingtherules.firewall.Service;

public class ServiceTest {

    private String EX_SERVICE = "TCP";

    private int EX_PORT = 80;

    private Service s;

    @Before
    public void before() {
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

	any = new Service(Service.ANY_PROTOCOL, Service.ANY_PORT_START_RANGE, Service.ANY_PORT_END_RANGE);
	assertTrue(any.contains(s));

	any = new Service(EX_SERVICE, Service.ANY_PORT_START_RANGE, Service.ANY_PORT_END_RANGE);
	assertTrue(any.contains(s));

	any = new Service(Service.ANY_PROTOCOL, EX_PORT);
	assertTrue(any.contains(s));
    }

}
