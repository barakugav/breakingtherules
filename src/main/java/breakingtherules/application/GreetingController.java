package breakingtherules.application;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

	private static final String m_template = "Hello, %s!";

	// private final AtomicLong m_counter = new AtomicLong();
	@Resource
	private SessionCounter m_counter;

	@RequestMapping("/greeting")
	public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
		return new Greeting(m_counter.incrementAndGet(), String.format(m_template, name));
	}

}
