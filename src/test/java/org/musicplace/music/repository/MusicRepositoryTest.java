package org.musicplace.music.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.musicplace.playList.domain.MusicEntity;
import org.musicplace.playList.repository.MusicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MusicRepositoryTest {

    @Autowired
    private MusicRepository musicRepository;

    @Test
    @DisplayName("playlist와 musicId들로 조회")
    void findAllByMusicIdInAndPlaylistId() {

        MusicEntity music1 = MusicEntity.builder()
                .playlistId(1L)
                .videoId("v1")
                .videoTitle("t1")
                .build();

        MusicEntity music2 = MusicEntity.builder()
                .playlistId(1L)
                .videoId("v2")
                .videoTitle("t2")
                .build();

        musicRepository.save(music1);
        musicRepository.save(music2);

        List<MusicEntity> result =
                musicRepository.findAllByMusicIdInAndPlaylistId(
                        List.of(
                                music1.getMusicId(),
                                music2.getMusicId()),
                        1L);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("삭제되지 않은 음악만 조회")
    void findByPlaylistIdAndMusicDeleteFalse() {

        MusicEntity music1 = MusicEntity.builder()
                .playlistId(1L)
                .videoId("v1")
                .videoTitle("t1")
                .build();

        MusicEntity music2 = MusicEntity.builder()
                .playlistId(1L)
                .videoId("v2")
                .videoTitle("t2")
                .build();

        music2.delete();

        musicRepository.save(music1);
        musicRepository.save(music2);

        List<MusicEntity> result =
                musicRepository.findByPlaylistIdAndMusicDeleteFalse(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getVideoId()).isEqualTo("v1");
    }
}
