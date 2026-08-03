package org.musicplace.global.security.controller;

import lombok.RequiredArgsConstructor;
import org.musicplace.global.security.dto.LoginRequestDto;
import org.musicplace.global.security.dto.LoginResponseDto;
import org.musicplace.global.security.jwt.JwtTokenUtil;
import org.musicplace.user.domain.UserEntity;
import org.musicplace.user.service.SignInService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenUtil jwtTokenUtil;
    private final SignInService signInService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @RequestBody LoginRequestDto loginRequestDto
    ) {

        UserEntity user = signInService.authenticate(
                loginRequestDto.getMember_id(),
                loginRequestDto.getPw()
        );

        String token = jwtTokenUtil.generateToken(user.getMemberId());

        return ResponseEntity.ok(new LoginResponseDto(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String token
    ) {

        String actualToken = token.substring(7);

        String memberId = jwtTokenUtil.getUserIdFromToken(actualToken);

        jwtTokenUtil.invalidateToken(memberId, actualToken);

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok().build();
    }
}
