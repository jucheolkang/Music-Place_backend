package org.musicplace.chat.interceptor;

import lombok.RequiredArgsConstructor;
import org.musicplace.chat.security.JwtTokenProvider;
import org.musicplace.user.domain.UserEntity;
import org.musicplace.user.service.SignInService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final SignInService signInService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String authorizationHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);

            if (jwtTokenProvider.validateToken(token)) {
                String userId = jwtTokenProvider.getUsernameFromToken(token);

                // SignInEntity에서 nickname을 가져와 세션에 저장
                UserEntity user = signInService.SignInFindById(userId);  // 사용자 ID로 사용자 엔티티 검색
                attributes.put("username", user.getNickname());  // nickname 저장
                return true;
            }
        }
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return false;
    }


    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 인증 후 후처리가 필요한 경우 여기에 추가
    }
}
