package io.github.vinipx.wixy.exception;

/**
 * Thrown when a stub definition is syntactically or semantically invalid.
 */
public class InvalidStubDefinitionException extends WixyException {

    public InvalidStubDefinitionException(String message) {
        super(message);
    }

    public InvalidStubDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
