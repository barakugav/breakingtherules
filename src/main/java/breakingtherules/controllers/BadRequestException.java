package breakingtherules.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * An exception thrown when the user (or the JavaScript) issues a request with
 * unexpected parameters
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    /**
     * Version of the exception
     */
    private static final long serialVersionUID = 1L;

}
