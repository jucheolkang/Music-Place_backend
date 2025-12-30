package org.musicplace.user.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignInGetUserDataDto {

    private String name;

    private String email;

    private String nickname;

    private String profile_img_url;

    @Builder
    public SignInGetUserDataDto(String name, String email, String nickname, String profile_img_url) {
        this.name = name;
        this.email = email;
        this.nickname = nickname;
        this.profile_img_url = profile_img_url;
    }
}
