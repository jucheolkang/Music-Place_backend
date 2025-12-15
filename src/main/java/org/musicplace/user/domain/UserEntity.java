package org.musicplace.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Table(name = "USERS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity implements UserDetails {

    @Id
    @Column(name = "member_id", nullable = false, length = 64)
    @Comment("아이디")
    private String memberId;

    @Column(name = "pw", nullable = false, length = 64)
    @Comment("비밀번호")
    private String pw;

    @Column(name = "name", nullable = false, length = 20)
    @Comment("이름")
    private String name;

    @Column(name = "gender", nullable = false)
    @Comment("성별")
    private Gender gender;

    @Column(name = "profile_img_url")
    @Comment("프로필 이미지")
    private String profileImgUrl;

    @Column(name = "email", nullable = false, length = 100)
    @Comment("이메일")
    private String email;

    @Column(name = "nickname", nullable = false, length = 50)
    @Comment("닉네임")
    private String nickname;

    @Column(name = "delete_account", nullable = false)
    @Comment("탈퇴여부")
    private Boolean deleteAccount = false;

    @Column(name = "roles", nullable = false)
    @Comment("권한")
    private String role;

    @Builder
    public UserEntity(
            String memberId,
            String pw,
            Gender gender,
            String email,
            String nickname,
            String name,
            String role
    ) {
        this.memberId = memberId;
        this.pw = pw;
        this.gender = gender;
        this.email = email;
        this.nickname = nickname;
        this.name = name;
        this.role = role;
    }

    public void updateProfile(String name, String email, String nickname, String profileImgUrl) {
        this.name = name;
        this.email = email;
        this.nickname = nickname;
        this.profileImgUrl = profileImgUrl;
    }

    public void deleteAccount() {
        this.deleteAccount = true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return pw;
    }

    @Override
    public String getUsername() {
        return memberId;
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return !deleteAccount; }
}
