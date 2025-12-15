package org.musicplace.playList.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.musicplace.global.jpa.AuditInformation;
import org.musicplace.user.domain.UserEntity;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "PLAYLIST")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PLEntity extends AuditInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PLAYLIST_ID")
    private Long playlistId;

    @Column(name = "member_id", nullable = false)
    private String memberId; // 🔥 FK 직접 참조

    @Column(name = "TITLE", nullable = false)
    private String title;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "COVER_IMG")
    private String coverImg;

    @Column(name = "ONOFF", nullable = false)
    @Enumerated(EnumType.STRING)
    private OnOff onOff;

    @Column(name = "COMMENT")
    private String comment;

    @Column(name = "DELETE_STATE", nullable = false)
    private boolean deleteState = false;

    @Builder
    public PLEntity(String memberId, String title, String nickname,
                    OnOff onOff, String coverImg, String comment) {
        this.memberId = memberId;
        this.title = title;
        this.nickname = nickname;
        this.onOff = onOff;
        this.coverImg = coverImg;
        this.comment = comment;
    }

    public void delete() {
        this.deleteState = true;
    }
}
