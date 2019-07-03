package in.nimbo.exception;

public class RecordNotFoundException extends RuntimeException {
    public RecordNotFoundException() {
        super();
    }

    public RecordNotFoundException(String s) {
        super(s);
    }

    public RecordNotFoundException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public RecordNotFoundException(Throwable throwable) {
        super(throwable);
    }

    protected RecordNotFoundException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
