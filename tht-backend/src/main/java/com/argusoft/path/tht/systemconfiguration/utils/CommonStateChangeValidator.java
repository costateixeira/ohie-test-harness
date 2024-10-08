package com.argusoft.path.tht.systemconfiguration.utils;

import com.argusoft.path.tht.systemconfiguration.constant.ErrorLevel;
import com.argusoft.path.tht.systemconfiguration.constant.ValidateConstant;
import com.argusoft.path.tht.systemconfiguration.exceptioncontroller.exception.DataValidationErrorException;
import com.argusoft.path.tht.systemconfiguration.models.dto.ValidationResultInfo;
import com.argusoft.path.tht.testcasemanagement.validator.ComponentValidator;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * validation method for common state change
 *
 * @author Dhruv
 */

public class CommonStateChangeValidator {

    public static final Logger LOGGER = LoggerFactory.getLogger(ComponentValidator.class);

    private CommonStateChangeValidator() {
    }

    public static void validateStateChange(List<String> correctStatus, Multimap<String, String> correctStatusMap, String state, String stateKey, List<ValidationResultInfo> errors) throws DataValidationErrorException {

        validateStateChangeByMap(correctStatus, correctStatusMap, state, stateKey, errors);

        if (ValidationUtils.containsErrors(errors, ErrorLevel.ERROR)) {
            throw new DataValidationErrorException(
                    ValidateConstant.ERRORS,
                    errors);
        }
    }

    public static List<ValidationResultInfo> validateStateChangeByMap(
            List<String> correctStatus,
            Multimap<String, String> correctStatusMap,
            String state,
            String stateKey,
            List<ValidationResultInfo> errors) {

        //validate given stateKey
        ValidationUtils.statusPresent(correctStatus, stateKey, errors);

        //validate transition
        ValidationUtils.transitionValid(correctStatusMap, state, stateKey, errors);

        return errors;
    }
}
