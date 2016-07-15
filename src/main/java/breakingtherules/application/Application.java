package breakingtherules.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This class is in charge of initiating the program - running the Spring
 * application, that initiates the controllers and the Autowired variables
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 */
@SpringBootApplication
public class Application {

    /**
     * Run the Spring-based server
     *
     * @param args
     *            Arguments for the Spring application
     */
    public static void main(final String[] args) {
	SpringApplication.run(Application.class, args);
    }

}
