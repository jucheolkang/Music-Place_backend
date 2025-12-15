package org.musicplace.global.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.musicplace.global.security.config.CustomUserDetails;
import org.musicplace.global.security.service.CustomUserDetailsService;
import org.musicplace.user.domain.UserEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");

        String memberId = null;
        String jwt = null;

        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);
                memberId = jwtTokenUtil.getUserIdFromToken(jwt);
            }

            if (memberId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserEntity userDetails = userDetailsService.loadUserByUsername(memberId);

                if (jwtTokenUtil.validateToken(jwt, userDetails.getMemberId())) {
                    CustomUserDetails customUserDetails = new CustomUserDetails(userDetails);
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            customUserDetails, null, customUserDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    logger.warn("Invalid JWT token for user: " + memberId);
                }
            }
        } catch (ExpiredJwtException e) {
            logger.warn("Expired JWT token", e);
        } catch (JwtException e) {
            logger.error("Invalid JWT token", e);
        } catch (Exception e) {
            logger.error("Unexpected error during JWT authentication", e);
        }

        chain.doFilter(request, response);
    }

}
