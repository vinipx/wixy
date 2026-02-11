package io.github.vinipx.wixy.exception;

/**
 * Base exception for all Wixy domain errors.
 */
public class WixyException extends RuntimeException {

    public WixyException(String message) {
        super(message);
    }

    public WixyException(String message, Throwable cause) {
        super(message, cause);
    }
}
