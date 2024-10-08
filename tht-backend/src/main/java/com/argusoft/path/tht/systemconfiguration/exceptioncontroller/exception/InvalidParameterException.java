package com.argusoft.path.tht.systemconfiguration.exceptioncontroller.exception;

/**
 * This exception mainly used when parameter are invalid. It Also contains error
 * message with HTTP status.
 *
 * @author Dhruv
 */
public class InvalidParameterException extends Exception {

    public InvalidParameterException(String message) {
        super(message);
    }

    public InvalidParameterException(String message, Throwable cause) {
        super(message, cause);
    }

}
