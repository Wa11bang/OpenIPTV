package com.openiptv.code.htsp;

public class HTSPException extends Exception {
    /**
     * Default constructor for HTSPException
     */
    public HTSPException() {
    }

    /**
     * Constructor for HTSPException, sends a message describing the exception,
     *
     * @param message description of exception
     */
    public HTSPException(String message) {
        super(message);
    }

    /**
     * Constructor with message and a cause of Throwable type.
     *
     * @param message description of exception
     * @param cause   throwable cause of exception
     */
    public HTSPException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with cause of Throwable type.
     *
     * @param cause throwable cause of exception
     */
    public HTSPException(Throwable cause) {
        super(cause);
    }
}
