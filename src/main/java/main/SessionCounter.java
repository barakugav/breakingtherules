package main;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "session",  proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionCounter {

	private int m_count = 0;
	
	public int incrementAndGet() {
		return ++m_count;
	}
	
}
