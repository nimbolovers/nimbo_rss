package in.nimbo.exception;

public class QueryException extends RuntimeException {
    public QueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
