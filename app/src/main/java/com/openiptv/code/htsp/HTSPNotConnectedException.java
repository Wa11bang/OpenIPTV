package com.openiptv.code.htsp;

public class HTSPNotConnectedException extends ConnectionException {
    public HTSPNotConnectedException() {
    }

    public HTSPNotConnectedException(String message) {
        super(message);
    }

    public HTSPNotConnectedException(String message, Throwable cause) {
        super(message, cause);
    }

    public HTSPNotConnectedException(Throwable cause) {
        super(cause);
    }
}
