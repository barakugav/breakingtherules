package breakingtherules.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * An exception thrown when the user (or the JavaScript) issues a request with
 * unexpected parameters.
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * 
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    @SuppressWarnings("javadoc")
    private static final long serialVersionUID = 1L;

}
