package com.argusoft.path.tht.systemconfiguration.models.dto;

import com.argusoft.path.tht.systemconfiguration.constant.ErrorLevel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * Information about the results of a data validation.
 *
 * @author Dhruv
 */
public class ValidationResultInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(notes = "The name of the element that has error",
            example = "elementName",
            accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    private String element;

    @ApiModelProperty(notes = "Level of the error",
            example = "ERROR/WARN/OK",
            accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    private ErrorLevel level;

    @ApiModelProperty(notes = "Error message for the error",
            example = "message",
            accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    private String message;

    @ApiModelProperty(notes = "stackTrace for the error",
            example = "exception path",
            accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    private String stackTrace;

    public ValidationResultInfo() {
        this.level = ErrorLevel.OK;
    }

    public ValidationResultInfo(String element) {
        this();
        this.element = element;
    }

    public ValidationResultInfo(String element, ErrorLevel level, String message) {
        this.element = element;
        this.level = level;
        this.message = message;
    }

    public ValidationResultInfo(ErrorLevel level, String message) {
        this.level = level;
        this.message = message;
    }

    public static boolean isSurpassingThreshold(ErrorLevel currentLevel,
                                                ErrorLevel threshold) {
        return currentLevel.compareTo(threshold) >= 0;
    }

    public String getElement() {
        return element;
    }

    public void setElement(String element) {
        this.element = element;
    }

    public ErrorLevel getLevel() {
        return this.level;
    }

    public void setLevel(ErrorLevel level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ValidationResultInfo{" + "element=" + element + ", level=" + level + ", message=" + message + '}';
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

}
