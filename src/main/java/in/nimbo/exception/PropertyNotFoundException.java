package in.nimbo.exception;

public class PropertyNotFoundException extends RuntimeException {
    public PropertyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
