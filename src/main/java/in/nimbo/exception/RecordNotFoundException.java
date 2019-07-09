package in.nimbo.exception;

public class RecordNotFoundException extends RuntimeException {
    public RecordNotFoundException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
