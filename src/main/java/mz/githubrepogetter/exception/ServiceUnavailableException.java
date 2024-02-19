package mz.githubrepogetter.exception;

public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException() {
        super("Service unavailable. Try again later.");
    }
}