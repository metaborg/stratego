package benchmark.exception;

public class InvalidConfigurationException extends RuntimeException {
    public InvalidConfigurationException(String msg) {
        super(msg);
    }
}
