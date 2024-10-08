package com.argusoft.path.tht.systemconfiguration.security.captcha.restcontroller;

import com.argusoft.path.tht.systemconfiguration.models.dto.ValidationResultInfo;
import com.argusoft.path.tht.systemconfiguration.security.captcha.models.CaptchaInfo;
import com.argusoft.path.tht.systemconfiguration.security.captcha.service.CaptchaService;
import com.argusoft.path.tht.systemconfiguration.security.model.dto.ContextInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * This captchaServiceRestController maps end points with standard service.
 *
 * @author ishita
 */

@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    private CaptchaService captchaService;

    @Autowired
    public void setCaptchaService(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    @GetMapping("")
    public CaptchaInfo getCaptcha(@RequestAttribute("contextInfo") ContextInfo contextInfo) throws Exception {
        return captchaService.createCaptcha(contextInfo);
    }

    @PostMapping("/validate")
    public List<ValidationResultInfo> validateCaptcha(@RequestHeader("captchaCode") String captchaCode,
                                                      @RequestHeader("captcha") String captcha,
                                                      @RequestAttribute("contextInfo") ContextInfo contextInfo) throws Exception {
        return captchaService.validateCaptcha(captchaCode, captcha, contextInfo);
    }

}
