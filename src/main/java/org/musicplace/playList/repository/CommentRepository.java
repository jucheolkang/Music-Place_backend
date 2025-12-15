package org.musicplace.playList.repository;

import org.musicplace.playList.domain.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity,Long> {
    List<CommentEntity> findByPlaylistIdAndCommentDeleteFalse(Long playlistId);
}
