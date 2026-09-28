package com.jiralite.tickets.error;

public final class ErrorCodes {
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String TICKET_NOT_FOUND = "TICKET_NOT_FOUND";
    public static final String PRODUCT_ACCESS_DENIED = "PRODUCT_ACCESS_DENIED";
    public static final String INVALID_STATE_TRANSITION = "INVALID_STATE_TRANSITION";
    public static final String TICKET_FROZEN = "TICKET_FROZEN";
    public static final String STALE_VERSION = "STALE_VERSION";
    public static final String AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";

    private ErrorCodes() {}
}
