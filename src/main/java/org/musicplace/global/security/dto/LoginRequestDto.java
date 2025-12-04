package org.musicplace.global.security.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginRequestDto {
    private String member_id;
    private String pw;

    @Builder
    public LoginRequestDto(String member_id, String pw) {
        this.member_id = member_id;
        this.pw = pw;
    }
}
