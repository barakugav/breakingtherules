package breakingtherules.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import breakingtherules.session.NoCurrentJobException;

/**
 * Specifies how (all) the controllers behave when they run into different
 * exceptions.
 * 
 * See https://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc
 */
@ControllerAdvice
public class GlobalControllerExceptionHandler {

    /**
     * Handles the case where a user tries to get or set job-specific
     * information, before choosing a job. This is common when the session has
     * expired, and a new one starts, but the browser is not yet aware of it.
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(NoCurrentJobException.class)
    public void handleNoJob() {
	System.out.println("The current session hasn't yet initiated a job.");
    }
}
