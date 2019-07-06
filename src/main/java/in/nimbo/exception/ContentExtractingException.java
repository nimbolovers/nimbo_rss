package in.nimbo.exception;

public class ContentExtractingException extends RuntimeException {

    public ContentExtractingException() {
        super();
    }

    public ContentExtractingException(String message) {
        super(message);
    }

    public ContentExtractingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContentExtractingException(Throwable cause) {
        super(cause);
    }
}
