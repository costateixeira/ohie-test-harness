package com.argusoft.path.tht.systemconfiguration.exceptioncontroller.exception;

/**
 * This exception mainly used when repository operations are failed. It Also
 * contains error message with HTTP status.
 *
 * @author Dhruv
 * @since 2023-09-13
 */
public class OperationFailedException extends Exception {

    public OperationFailedException(String message) {
        super(message);
    }

    public OperationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public OperationFailedException(DataValidationErrorException ex) {
        super(ex.toString(), ex);
    }
}
