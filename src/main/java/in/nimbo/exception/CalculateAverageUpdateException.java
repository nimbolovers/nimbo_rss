package in.nimbo.exception;

/**
 * represent a exception during calculating average update time of
 * site in schedule service
 */
public class CalculateAverageUpdateException extends RuntimeException {
    public CalculateAverageUpdateException() {
        super();
    }

    public CalculateAverageUpdateException(String message) {
        super(message);
    }

    public CalculateAverageUpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    public CalculateAverageUpdateException(Throwable cause) {
        super(cause);
    }
}
