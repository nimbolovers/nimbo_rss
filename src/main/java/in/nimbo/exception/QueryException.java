package in.nimbo.exception;

public class QueryException extends RuntimeException {
    public QueryException() {
        super();
    }

    public QueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
