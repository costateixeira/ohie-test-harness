package com.argusoft.path.tht.systemconfiguration.audit.confige;

import com.argusoft.path.tht.systemconfiguration.constant.Constant;
import com.argusoft.path.tht.systemconfiguration.security.custom.CustomOauth2User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()) {
            return Optional.of(Constant.SUPER_USER_CONTEXT.getEmail());
        }
        //TODO: We don't have userEmail as ANONYMOUS_USER_NAME right now.
        if (Constant.ANONYMOUS_USER.equals(authentication.getPrincipal())) {
            return Optional.of(Constant.ANONYMOUS_USER_NAME);
        }
        if (authentication.getPrincipal() instanceof User) {
            return Optional.of(((User) authentication.getPrincipal()).getUsername());
        } else {
            return Optional.of(((CustomOauth2User) authentication.getPrincipal()).getCustomAttributes().get("userName"));
        }
    }

}
