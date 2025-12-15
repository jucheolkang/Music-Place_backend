package org.musicplace.global.security.authorizaion;

import org.musicplace.global.security.config.CustomUserDetails;
import org.musicplace.user.domain.UserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class MemberAuthorizationUtil {

    private MemberAuthorizationUtil() {
        throw new AssertionError();
    }
    public static String getLoginMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getSignInEntity().getMemberId();
        } else if (principal instanceof UserEntity) {
            return ((UserEntity) principal).getMemberId();
        }

        throw new ClassCastException("Unexpected Principal type: " + principal.getClass().getName());
    }


}
