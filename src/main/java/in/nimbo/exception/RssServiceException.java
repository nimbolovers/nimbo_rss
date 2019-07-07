package in.nimbo.exception;

public class RssServiceException extends Exception {
    public RssServiceException() {
        super();
    }

    public RssServiceException(String message) {
        super(message);
    }

    public RssServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public RssServiceException(Throwable cause) {
        super(cause);
    }
}
