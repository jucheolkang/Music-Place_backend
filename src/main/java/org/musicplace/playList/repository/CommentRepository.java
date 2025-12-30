package org.musicplace.playList.repository;

import org.musicplace.playList.domain.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<CommentEntity,Long> {
    Optional<CommentEntity> findByIdAndPlaylistId(Long commentId, Long playlistId);
    List<CommentEntity> findByPlaylistIdAndCommentDeleteFalse(Long playlistId);
}
