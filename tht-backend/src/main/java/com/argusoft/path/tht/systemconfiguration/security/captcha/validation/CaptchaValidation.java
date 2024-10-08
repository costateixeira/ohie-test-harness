package com.argusoft.path.tht.systemconfiguration.security.captcha.validation;

import com.argusoft.path.tht.systemconfiguration.constant.ErrorLevel;
import com.argusoft.path.tht.systemconfiguration.models.dto.ValidationResultInfo;
import com.argusoft.path.tht.systemconfiguration.utils.ValidationUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * validation methods for captcha
 *
 * @author ishita
 */

public class CaptchaValidation {

    private CaptchaValidation() {
    }

    public static List<ValidationResultInfo> validateCaptchaEmpty(String code, String encryptedString) {


        List<ValidationResultInfo> errors = new ArrayList<>();

        //trim
        code = trimCaptcha(code);
        encryptedString = trimCaptcha(encryptedString);

        //check required fields
        ValidationUtils.validateNotEmpty(code, "captcha", errors);
        ValidationUtils.validateNotEmpty(encryptedString, "encrypted String", errors);
        return errors;
    }

    public static String trimCaptcha(String code) {
        return code == null ? null : code.trim();
    }

    public static void validateCaptcha(String code, Date expiryTime, String userCode, List<ValidationResultInfo> errors) {
        ValidationUtils.validateNotEmpty(code, "actual code", errors);
        ValidationUtils.validateRequired(expiryTime, "expiry time", errors);
        ValidationUtils.validateNotEmpty(userCode, "User code", errors);

        Date currentTime = new Date();

        if (!expiryTime.before(currentTime)) {
            if (!code.equals(userCode)) {
                errors.add(new ValidationResultInfo("code", ErrorLevel.ERROR, "Invalid CAPTCHA."));
            }
        } else {
            errors.add(new ValidationResultInfo("code", ErrorLevel.ERROR, "CAPTCHA expired."));
        }
    }


}
