package com.argusoft.path.tht.systemconfiguration.security.custom;

import com.argusoft.path.tht.systemconfiguration.constant.Constant;
import com.argusoft.path.tht.systemconfiguration.exceptioncontroller.exception.DoesNotExistException;
import com.argusoft.path.tht.systemconfiguration.models.dto.ContextInfo;
import com.argusoft.path.tht.usermanagement.constant.UserServiceConstants;
import com.argusoft.path.tht.usermanagement.models.entity.UserEntity;
import com.argusoft.path.tht.usermanagement.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * This CustomUserDetailService implements UserDetailsService and authenticates user.
 *
 * @author Dhruv
 */
@Service
@Transactional
public class CustomUserDetailService implements UserDetailsService {

    public static final Logger LOGGER = LoggerFactory.getLogger(CustomUserDetailService.class);

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            String password = request.getParameter("password");
            try {
                UserEntity user = userService.getUserByEmail(username, Constant.SUPER_USER_CONTEXT);
                if (!StringUtils.hasLength(user.getPassword()) || !Objects.equals(user.getPassword(), password)) {
                    LOGGER.error("caught UsernameNotFoundException in CustomUserDetailService ");
                    throw new UsernameNotFoundException("Credential are incorrect.");
                }

                //If User is not active
                if (!Objects.equals(UserServiceConstants.USER_STATUS_ACTIVE, user.getState())) {
                    if (Objects.equals(UserServiceConstants.USER_STATUS_VERIFICATION_PENDING, user.getState())) {
                        LOGGER.error("caught UsernameNotFoundException in CustomUserDetailService ");
                        throw new UsernameNotFoundException("Pending email verification.") {
                        };
                    } else if (Objects.equals(UserServiceConstants.USER_STATUS_APPROVAL_PENDING, user.getState())) {
                        LOGGER.error("caught UsernameNotFoundException in CustomUserDetailService ");
                        throw new UsernameNotFoundException("Pending admin approval.") {
                        };
                    } else if (Objects.equals(UserServiceConstants.USER_STATUS_REJECTED, user.getState())) {
                        LOGGER.error("caught UsernameNotFoundException in CustomUserDetailService ");
                        throw new UsernameNotFoundException("Admin approval has been rejected.") {
                        };
                    } else {
                        //Only state left is UserServiceConstants.USER_STATUS_INACTIVE.
                        LOGGER.error("caught UsernameNotFoundException in CustomUserDetailService ");
                        throw new UsernameNotFoundException("User is inactive.") {
                        };
                    }
                }

                List<GrantedAuthority> authorities
                        = user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getId()))
                        .collect(Collectors.toList());

                return new ContextInfo(
                        user.getEmail(),
                        user.getId(),
                        password,
                        true,
                        true,
                        true,
                        true,
                        authorities);

            } catch (NumberFormatException | DoesNotExistException e) {
                LOGGER.error("caught DoesNotExistException in CustomUserDetailService ", e);
                throw new UsernameNotFoundException("Credential are incorrect.") {
                };
            }
        }
        LOGGER.error("caught UsernameNotFoundException in CustomUserDetailService ");
        throw new UsernameNotFoundException("requestAttributes is incorrect") {
        };
    }
}
