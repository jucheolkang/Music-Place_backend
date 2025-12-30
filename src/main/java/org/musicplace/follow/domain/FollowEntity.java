package org.musicplace.follow.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Table(
        name = "FOLLOW",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_follow_member_target",
                        columnNames = {"member_id", "target_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "follow_id")
    @Comment("팔로우 ID")
    private Long followId;

    @Column(name = "member_id", nullable = false, length = 64)
    @Comment("팔로우 요청자 ID")
    private String memberId;

    @Column(name = "target_id", nullable = false, length = 64)
    @Comment("팔로우 대상 사용자 ID")
    private String targetId;

    /**
     * 팔로우 대상 사용자의 닉네임 스냅샷
     * - 팔로우 시점 기준
     * - 사용자 정보 변경 시 실시간 동기화하지 않음
     */
    @Column(name = "target_nickname", nullable = false, length = 50)
    private String targetNickname;

    /**
     * 팔로우 대상 사용자의 프로필 이미지 스냅샷
     * - 조회 성능 최적화를 위한 비정규화 컬럼
     */
    @Column(name = "target_profile_img_url")
    private String targetProfileImgUrl;

    @Builder
    public FollowEntity(
            String memberId,
            String targetId,
            String targetNickname,
            String targetProfileImgUrl
    ) {
        this.memberId = memberId;
        this.targetId = targetId;
        this.targetNickname = targetNickname;
        this.targetProfileImgUrl = targetProfileImgUrl;
    }

    /**
     * 부분 동기화를 위한 업데이트 메서드
     * - 현재는 사용하지 않지만,
     * - 향후 이벤트 기반 / 배치 동기화 요구사항 대비
     */
    public void updateTargetProfile(String targetNickname, String targetProfileImgUrl) {
        this.targetNickname = targetNickname;
        this.targetProfileImgUrl = targetProfileImgUrl;
    }
}
