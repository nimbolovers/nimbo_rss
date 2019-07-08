package in.nimbo.exception;

public class PropertyNotFoundException extends RuntimeException {
    public PropertyNotFoundException() {
        super();
    }

    public PropertyNotFoundException(String message) {
        super(message);
    }

    public PropertyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PropertyNotFoundException(Throwable cause) {
        super(cause);
    }
}
