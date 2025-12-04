package org.musicplace.global.security.controller;

import lombok.RequiredArgsConstructor;
import org.musicplace.global.security.config.CustomUserDetails;
import org.musicplace.global.security.dto.LoginResponseDto;
import org.musicplace.global.security.jwt.JwtTokenUtil;
import org.musicplace.global.security.dto.LoginRequestDto;
import org.musicplace.member.service.SignInService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenUtil jwtTokenUtil;
    private final SignInService signInService;

    // JWT 로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        CustomUserDetails user = signInService.authenticate(loginRequestDto.getMember_id(), loginRequestDto.getPw());
        String token = jwtTokenUtil.generateToken(user.getSignInEntity().getMemberId());
        return ResponseEntity.ok(new LoginResponseDto(token));

    }

    // 로그아웃 기능
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization") String token) {
        String actualToken = token.substring(7);
        String memberId = jwtTokenUtil.getUserIdFromToken(actualToken);

        jwtTokenUtil.invalidateToken(memberId, actualToken);
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }
}
