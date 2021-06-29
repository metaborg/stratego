package benchmark.exception;

public class SkipException extends Exception {
    public SkipException(String msg) {
            super(msg);
    }

    public SkipException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
