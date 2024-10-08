package com.argusoft.path.tht.systemconfiguration.constant;

import com.argusoft.path.tht.systemconfiguration.security.model.dto.ContextInfo;
import com.argusoft.path.tht.usermanagement.constant.UserServiceConstants;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * This Constant class contains all the common constant variables
 *
 * @author Dhruv
 */
public final class Constant {

    public static final String DEFAULT_SYSTEM_USER_ID = "SYSTEM_USER";
    public static final String ANONYMOUS_USER = "anonymousUser";
    public static final String ANONYMOUS_USER_NAME = "ANONYMOUS_USER";
    // Constants for validating
    public static final String CREATE_VALIDATION = "create.validation";

    public static final String UPDATE_VALIDATION = "update.validation";
    public static final String SUBMIT_VALIDATION = "submit.validation";
    public static final String START_PROCESS_VALIDATION = "start.process.validation";
    public static final String STOP_PROCESS_VALIDATION = "stop.process.validation";
    public static final String RESET_PROCESS_VALIDATION = "reset.process.validation";
    public static final Pageable SINGLE_VALUE_PAGE = PageRequest.of(0, 1);
    public static final Pageable FULL_PAGE = PageRequest.of(0, Integer.MAX_VALUE);
    public static final Pageable FULL_PAGE_SORT_BY_RANK = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("rank"));
    public static final Pageable SINGLE_PAGE_SORT_BY_RANK = PageRequest.of(0, 1, Sort.by("rank").descending());
    public static final Pageable TWO_VALUE_PAGE = PageRequest.of(0, 2);
    public static final String ALLOWED_CHARS_IN_NAMES = "[[A-Z][a-z][0-9][/][\\s][_][@][#][(][)][.][,]['][-][*][`][/][&]]*";
    public static final ContextInfo SUPER_USER_CONTEXT = new ContextInfo(
            "",
            DEFAULT_SYSTEM_USER_ID,
            "",
            true,
            true,
            true,
            true,
            Arrays.asList(new SimpleGrantedAuthority(UserServiceConstants.ROLE_ID_ADMIN))
    );
    public static final ContextInfo ASSESSE_USER_CONTEXT = new ContextInfo(
            "assessee@yopmail.com",
            "user.01",
            "password",
            true,
            true,
            true,
            true,
            Arrays.asList(new SimpleGrantedAuthority(UserServiceConstants.ROLE_ID_ASSESSEE))
    );

    private Constant() {
    }
}
