package com.openiptv.code.htsp;

public class HTSPException extends Exception {
    public HTSPException() {
    }

    public HTSPException(String message) {
        super(message);
    }

    public HTSPException(String message, Throwable cause) {
        super(message, cause);
    }

    public HTSPException(Throwable cause) {
        super(cause);
    }
}
