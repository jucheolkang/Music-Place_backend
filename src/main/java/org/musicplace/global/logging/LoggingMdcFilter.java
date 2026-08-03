package org.musicplace.global.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class LoggingMdcFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        String traceId = UUID.randomUUID().toString();

        try {

            /*
             * ==============================
             * Request 정보 저장
             * ==============================
             */

            MDC.put(MdcConstants.TRACE_ID, traceId);
            MDC.put(MdcConstants.REQUEST_ID, traceId);
            MDC.put(MdcConstants.METHOD, request.getMethod());
            MDC.put(MdcConstants.URI, request.getRequestURI());

            /*
             * 인증 전이므로 anonymous
             */
            MDC.put(MdcConstants.USER_ID, "anonymous");

            log.info("HTTP_REQUEST");

            filterChain.doFilter(request, response);

        } finally {

            /*
             * JWT 인증이 끝난 이후
             * userId 갱신
             */
            updateUserId();

            long elapsedTime = System.currentTimeMillis() - startTime;

            MDC.put(MdcConstants.STATUS,
                    String.valueOf(response.getStatus()));

            MDC.put(MdcConstants.ELAPSED_TIME,
                    String.valueOf(elapsedTime));

            log.info("HTTP_RESPONSE");

            MDC.clear();
        }

    }

    /**
     * JWT 인증 완료 후 SecurityContext에서 userId 조회
     */
    private void updateUserId() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return;
        }

        if (authentication instanceof AnonymousAuthenticationToken) {
            return;
        }

        MDC.put(
                MdcConstants.USER_ID,
                authentication.getName()
        );
    }

}
