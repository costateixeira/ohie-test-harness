package com.argusoft.path.tht.usermanagement.constant;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.List;

/**
 * Constant for UserService.
 *
 * @author Dhruv
 */
public class UserServiceConstants {

    //User status
    public static final String USER_STATUS_VERIFICATION_PENDING = "user.status.verification.pending";
    public static final String USER_STATUS_APPROVAL_PENDING = "user.status.approval.pending";
    public static final String USER_STATUS_REJECTED = "user.status.rejected";
    public static final String USER_STATUS_ACTIVE = "user.status.active";
    public static final String USER_STATUS_INACTIVE = "user.status.inactive";
    //Role Ids
    public static final String ROLE_ID_ADMIN = "role.admin";
    public static final String ROLE_ID_TESTER = "role.tester";
    public static final String ROLE_ID_ASSESSEE = "role.assessee";
    //validation
    public static final String EMAIL_REGEX =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    public static final List<String> USER_STATUS = new ArrayList<>();

    static {
        USER_STATUS.add(USER_STATUS_VERIFICATION_PENDING);
        USER_STATUS.add(USER_STATUS_APPROVAL_PENDING);
        USER_STATUS.add(USER_STATUS_ACTIVE);
        USER_STATUS.add(USER_STATUS_REJECTED);
        USER_STATUS.add(USER_STATUS_INACTIVE);
    }

    public static final Multimap<String, String> USER_STATUS_MAP = ArrayListMultimap.create();

    static {
        USER_STATUS_MAP.put(USER_STATUS_VERIFICATION_PENDING, USER_STATUS_APPROVAL_PENDING);
        USER_STATUS_MAP.put(USER_STATUS_VERIFICATION_PENDING, USER_STATUS_REJECTED);
        USER_STATUS_MAP.put(USER_STATUS_APPROVAL_PENDING, USER_STATUS_ACTIVE);
        USER_STATUS_MAP.put(USER_STATUS_APPROVAL_PENDING, USER_STATUS_REJECTED);
        USER_STATUS_MAP.put(USER_STATUS_ACTIVE, USER_STATUS_INACTIVE);
        USER_STATUS_MAP.put(USER_STATUS_INACTIVE, USER_STATUS_ACTIVE);
    }
}
