package com.vivriti.investron.common.exception;

public class CoreException extends RuntimeException {

    private final int statusCode;

    public CoreException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public CoreException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
