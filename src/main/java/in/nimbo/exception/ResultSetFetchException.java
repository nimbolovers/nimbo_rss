package in.nimbo.exception;

public class ResultSetFetchException extends RuntimeException {
    public ResultSetFetchException() {
        super();
    }

    public ResultSetFetchException(String message) {
        super(message);
    }

    public ResultSetFetchException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResultSetFetchException(Throwable cause) {
        super(cause);
    }
}
