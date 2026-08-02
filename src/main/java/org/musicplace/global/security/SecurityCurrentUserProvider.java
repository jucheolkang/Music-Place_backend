package org.musicplace.global.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityCurrentUserProvider
        implements CurrentUserProvider {

    @Override
    public String getMemberId() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new IllegalStateException("로그인 정보가 없습니다.");
        }

        return authentication.getName();
    }
}
