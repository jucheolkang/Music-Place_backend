package org.musicplace.playList.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.musicplace.global.jpa.AuditInformation;


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

    // 검색용 비정규화 컬럼: 재생목록에 포함된 곡들의 videoTitle을 합쳐서 저장
    @Column(name = "SEARCH_TEXT", columnDefinition = "TEXT")
    private String searchText;

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

    public void plUpdate(String title, OnOff onOff, String cover_img, String comment) {
        this.title = title;
        this.onOff = onOff;
        this.comment = comment;
        this.coverImg = cover_img;
    }

    // 재계산 결과를 반영하는 전용 메서드 (직접 setter 노출 대신 의도를 드러냄)
    public void updateSearchText(String searchText) {
        this.searchText = searchText;
    }

    public void delete() {
        this.deleteState = true;
    }
}
