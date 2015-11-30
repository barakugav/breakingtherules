package breakingtherules.application;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({ "breakingtherules.dao", "breakingtherules.controllers", "breakingtherules.algorithms", "breakingtherules.session" })
public class Config {

}
