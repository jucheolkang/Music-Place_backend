package org.musicplace.playList.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.musicplace.global.jpa.AuditInformation;

@Entity
@Table(name = "COMMENTS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentEntity extends AuditInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COMMENT_ID")
    private Long commentId;

    @Column(name = "playlist_id", nullable = false)
    private Long playlistId;

    @Column(name = "member_id", nullable = false)
    private String memberId;

    @Column(name = "NICKNAME", nullable = false)
    private String nickname;

    @Column(name = "COMMENT", nullable = false)
    private String userComment;

    @Column(name = "profile_img_url")
    private String profileImgUrl;

    @Column(name = "DELETE_STATE", nullable = false)
    private boolean commentDelete = false;

    @Builder
    public CommentEntity(Long playlistId, String memberId, String nickname,
                         String userComment, String profileImgUrl) {
        this.playlistId = playlistId;
        this.memberId = memberId;
        this.nickname = nickname;
        this.userComment = userComment;
        this.profileImgUrl = profileImgUrl;
    }

    public void delete() {
        this.commentDelete = true;
    }
}
