package org.cloudbus.cloudsim.web;

import java.io.Serial;

/**
 * Indicates that a session has failed.
 * 
 * @author nikolay.grozev
 * 
 */
public class SessionFailedException extends RuntimeException {

    /**
     * 
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private final int sessionId;

    /**
     * 
     */
    public SessionFailedException(int sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * @param message
     */
    public SessionFailedException(int sessionId, String message) {
        super(message);
        this.sessionId = sessionId;
    }

    /**
     * @param cause
     */
    public SessionFailedException(int sessionId, Throwable cause) {
        super(cause);
        this.sessionId = sessionId;
    }

    /**
     * @param message
     * @param cause
     */
    public SessionFailedException(int sessionId, String message, Throwable cause) {
        super(message, cause);
        this.sessionId = sessionId;
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public SessionFailedException(int sessionId, String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.sessionId = sessionId;
    }

    /**
     * Returns the id of the failed session.
     * 
     * @return the id of the failed session.
     */
    public int getSessionId() {
        return sessionId;
    }
}
