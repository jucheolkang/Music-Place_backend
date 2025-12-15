package org.musicplace.follow.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.musicplace.user.domain.UserEntity;

@Entity
@Getter
@Table(name = "FOLLOW")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("팔로우id")
    private Long follow_id;

    @Column(name = "target_id", nullable = false, length = 64)
    @Comment("대상id")
    private String target_id;

    @Column(name = "nickname", nullable = false, length = 50)
    @Comment("닉네임")
    private String nickname;

    @Column(name = "profile_img_url", nullable = true)
    @Comment("프로필 이미지")
    private String profile_img_url;

    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private UserEntity userEntity;

    @Builder
    public FollowEntity(String target_id, String nickname, String profile_img_url) {
        this.target_id = target_id;
        this.nickname = nickname;
        this.profile_img_url = profile_img_url;
    }

    public void SignInEntity(UserEntity userEntity) {this.userEntity = userEntity; }
}
