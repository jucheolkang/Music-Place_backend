package org.musicplace.comment.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.musicplace.playList.domain.CommentEntity;
import org.musicplace.playList.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TestEntityManager em;

    private CommentEntity persistComment(Long playlistId, String memberId, boolean deleted) {
        CommentEntity comment = CommentEntity.builder()
                .playlistId(playlistId)
                .memberId(memberId)
                .nickname("nick_" + memberId)
                .userComment("comment")
                .profileImgUrl("http://profile")
                .build();

        em.persist(comment);

        if (deleted) {
            comment.delete();
            em.persist(comment);
        }

        return comment;
    }

    @Nested
    @DisplayName("findByCommentIdAndPlaylistId")
    class FindByCommentIdAndPlaylistIdTest {

        @Test
        @DisplayName("commentId와 playlistId가 모두 일치하면 댓글을 반환한다")
        void matchingBoth_returnsComment() {
            CommentEntity comment = persistComment(1L, "tester01", false);
            em.flush();
            em.clear();

            Optional<CommentEntity> result =
                    commentRepository.findByCommentIdAndPlaylistId(comment.getCommentId(), 1L);

            assertThat(result).isPresent();
            assertThat(result.get().getMemberId()).isEqualTo("tester01");
        }

        @Test
        @DisplayName("playlistId가 다르면 빈 Optional을 반환한다")
        void mismatchedPlaylistId_returnsEmpty() {
            CommentEntity comment = persistComment(1L, "tester01", false);
            em.flush();
            em.clear();

            Optional<CommentEntity> result =
                    commentRepository.findByCommentIdAndPlaylistId(comment.getCommentId(), 999L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 commentId면 빈 Optional을 반환한다")
        void notFound_returnsEmpty() {
            Optional<CommentEntity> result =
                    commentRepository.findByCommentIdAndPlaylistId(999L, 1L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByPlaylistIdAndCommentDeleteFalse")
    class FindByPlaylistIdAndCommentDeleteFalseTest {

        @Test
        @DisplayName("삭제되지 않은 댓글만 반환한다")
        void returnsOnlyActiveComments() {
            persistComment(1L, "tester01", false);
            persistComment(1L, "tester02", false);
            persistComment(1L, "tester03", true); // 삭제됨 - 제외
            persistComment(2L, "tester04", false); // 다른 플레이리스트 - 제외
            em.flush();
            em.clear();

            List<CommentEntity> result =
                    commentRepository.findByPlaylistIdAndCommentDeleteFalse(1L);

            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(CommentEntity::getMemberId)
                    .containsExactlyInAnyOrder("tester01", "tester02");
        }

        @Test
        @DisplayName("댓글이 없으면 빈 리스트를 반환한다")
        void noComments_returnsEmptyList() {
            List<CommentEntity> result =
                    commentRepository.findByPlaylistIdAndCommentDeleteFalse(999L);

            assertThat(result).isEmpty();
        }
    }
}
