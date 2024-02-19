package mz.githubrepogetter;

import mz.githubrepogetter.exception.ErrorResponse;
import mz.githubrepogetter.exception.ServiceUnavailableException;
import mz.githubrepogetter.exception.UsernameNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApplicationExceptionHandlerTest {

    private final ApplicationExceptionHandler exceptionHandler = new ApplicationExceptionHandler();

    @Test
    public void testHandleEntityNotFoundException() {
        UsernameNotFoundException exception = new UsernameNotFoundException("User not found");

        ResponseEntity<Object> responseEntity = exceptionHandler.handleEntityNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());

        ErrorResponse errorResponse = (ErrorResponse) responseEntity.getBody();
        assertEquals("404", errorResponse.getStatus());
        assertEquals("User with username User not found does not exist.", errorResponse.getMessage());
    }

    @Test
    public void testHandleServiceUnavailableException() {
        ServiceUnavailableException exception = new ServiceUnavailableException();

        ResponseEntity<Object> responseEntity = exceptionHandler.handleServiceUnavailableException(exception);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, responseEntity.getStatusCode());

        ErrorResponse errorResponse = (ErrorResponse) responseEntity.getBody();
        assertEquals("503", errorResponse.getStatus());
        assertEquals("Service unavailable. Try again later.", errorResponse.getMessage());
    }
}