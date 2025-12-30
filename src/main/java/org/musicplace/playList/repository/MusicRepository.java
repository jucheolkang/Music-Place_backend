package org.musicplace.playList.repository;

import org.musicplace.playList.domain.MusicEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MusicRepository extends JpaRepository<MusicEntity,Long> {
    List<MusicEntity> findAllByIdInAndPlaylistId(List<Long> ids, Long playlistId);

    List<MusicEntity> findByPlaylistIdAndMusicDeleteFalse(Long playlistId);
}
