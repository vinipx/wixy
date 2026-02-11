package io.github.vinipx.wixy.exception;

/**
 * Thrown when a requested stub mapping cannot be found.
 */
public class StubNotFoundException extends WixyException {

    public StubNotFoundException(String id) {
        super("Stub mapping not found: " + id);
    }
}
