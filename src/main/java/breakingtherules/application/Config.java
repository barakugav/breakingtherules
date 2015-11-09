package breakingtherules.application;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({ "dao", "controllers" })
public class Config {

}
