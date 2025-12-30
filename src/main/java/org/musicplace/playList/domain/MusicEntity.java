package org.musicplace.playList.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "PLMUSIC")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MusicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MUSIC_ID")
    private Long musicId;

    @Column(name = "playlist_id", nullable = false)
    private Long playlistId;

    @Column(name = "TITLE", nullable = false)
    private String videoTitle;

    @Column(name = "VIDIO_ID", nullable = false)
    private String videoId;

    @Column(name = "VIDIO_IMGE")
    private String videoImage;

    @Column(name = "DELETE_STATE", nullable = false)
    private boolean musicDelete = false;

    @Builder
    public MusicEntity(Long playlistId, String videoTitle, String videoId, String videoImage) {
        this.playlistId = playlistId;
        this.videoTitle = videoTitle;
        this.videoId = videoId;
        this.videoImage = videoImage;
    }

    public void delete() {
        this.musicDelete = true;
    }
}
