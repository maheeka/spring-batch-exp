package au.com.maheeka.spring.springbatch.exceptions;

public class CustomerRetryableException extends RuntimeException {

    public CustomerRetryableException(String message) {
        super(message);
    }
}
