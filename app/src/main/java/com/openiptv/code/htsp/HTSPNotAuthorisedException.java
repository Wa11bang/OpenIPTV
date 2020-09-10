package com.openiptv.code.htsp;

public class HTSPNotAuthorisedException extends ConnectionException {
    public HTSPNotAuthorisedException() {
    }

    public HTSPNotAuthorisedException(String message) {
        super(message);
    }

    public HTSPNotAuthorisedException(String message, Throwable cause) {
        super(message, cause);
    }

    public HTSPNotAuthorisedException(Throwable cause) {
        super(cause);
    }
}
