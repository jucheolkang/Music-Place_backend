package org.musicplace.user.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignInUpdateDto {

    private String name;

    private String profile_img_url;

    private String email;

    private String nickname;

    @Builder
    public SignInUpdateDto(String name, String email, String nickname, String profile_img_url){
        this.profile_img_url = profile_img_url;
        this.email = email;
        this.nickname = nickname;
        this.name = name;
    }
}
