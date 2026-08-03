package org.musicplace.comment.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.musicplace.global.exception.BusinessException;
import org.musicplace.global.exception.ErrorCode;
import org.musicplace.playList.domain.CommentEntity;
import org.musicplace.playList.dto.CommentSaveDto;
import org.musicplace.playList.dto.ResponseCommentDto;
import org.musicplace.playList.repository.CommentRepository;
import org.musicplace.playList.service.CommentService;
import org.musicplace.playList.service.PLService;
import org.musicplace.user.domain.Gender;
import org.musicplace.user.domain.UserEntity;
import org.musicplace.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PLService plService;

    @InjectMocks
    private CommentService commentService;

    private UserEntity activeUser() {
        return UserEntity.builder()
                .memberId("tester01")
                .pw("encodedPw")
                .gender(Gender.male)
                .email("tester01@test.com")
                .nickname("nick")
                .name("홍길동")
                .role("ROLE_USER")
                .build();
    }

    private UserEntity deletedUser() {
        UserEntity user = activeUser();
        user.deleteAccount();
        return user;
    }

    private CommentEntity activeComment() {
        return CommentEntity.builder()
                .playlistId(1L)
                .memberId("author01")
                .nickname("authorNick")
                .userComment("좋은 곡이네요")
                .profileImgUrl("http://profile")
                .build();
    }

    private void assertBusinessException(Runnable action, ErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(expected);
    }

    @Nested
    @DisplayName("commentSave")
    class CommentSaveTest {

        @Test
        @DisplayName("정상 회원 + 활성 플레이리스트면 댓글이 저장된다")
        void save_activeUserAndPlaylist_savesComment() {
            UserEntity user = activeUser();
            when(userRepository.findByMemberId("tester01")).thenReturn(Optional.of(user));
            doNothing().when(plService).validatePlaylistActive(1L);

            CommentSaveDto dto = CommentSaveDto.builder()
                    .nickName("ignored")
                    .comment("좋은 곡이네요")
                    .profile_img_url("http://profile")
                    .build();

            commentService.commentSave("tester01", 1L, dto);

            ArgumentCaptor<CommentEntity> captor = ArgumentCaptor.forClass(CommentEntity.class);
            verify(commentRepository).save(captor.capture());

            CommentEntity saved = captor.getValue();
            assertThat(saved.getPlaylistId()).isEqualTo(1L);
            assertThat(saved.getMemberId()).isEqualTo("tester01");
            assertThat(saved.getNickname()).isEqualTo("nick"); // user.getNickname()에서 온 값
            assertThat(saved.getUserComment()).isEqualTo("좋은 곡이네요");
        }

        @Test
        @DisplayName("존재하지 않는 회원이면 ID_NOT_FOUND 예외가 발생하고 저장되지 않는다")
        void save_userNotFound_throws() {
            when(userRepository.findByMemberId("noone")).thenReturn(Optional.empty());

            CommentSaveDto dto = CommentSaveDto.builder().comment("t").build();

            assertBusinessException(
                    () -> commentService.commentSave("noone", 1L, dto),
                    ErrorCode.ID_NOT_FOUND
            );
            verify(commentRepository, never()).save(any());
            verify(plService, never()).validatePlaylistActive(any());
        }

        @Test
        @DisplayName("탈퇴한 회원이면 MEMBER_DELETED 예외가 발생하고 저장되지 않는다")
        void save_deletedUser_throws() {
            when(userRepository.findByMemberId("tester01")).thenReturn(Optional.of(deletedUser()));

            CommentSaveDto dto = CommentSaveDto.builder().comment("t").build();

            assertBusinessException(
                    () -> commentService.commentSave("tester01", 1L, dto),
                    ErrorCode.MEMBER_DELETED
            );
            verify(commentRepository, never()).save(any());
            verify(plService, never()).validatePlaylistActive(any());
        }

        @Test
        @DisplayName("삭제된 플레이리스트면 PLAYLIST_DELETED 예외가 발생하고 저장되지 않는다")
        void save_deletedPlaylist_throws() {
            UserEntity user = activeUser();
            when(userRepository.findByMemberId("tester01")).thenReturn(Optional.of(user));
            doThrow(new BusinessException(ErrorCode.PLAYLIST_DELETED))
                    .when(plService).validatePlaylistActive(1L);

            CommentSaveDto dto = CommentSaveDto.builder().comment("t").build();

            assertBusinessException(
                    () -> commentService.commentSave("tester01", 1L, dto),
                    ErrorCode.PLAYLIST_DELETED
            );
            verify(commentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("commentDelete")
    class CommentDeleteTest {

        @Test
        @DisplayName("활성 댓글은 삭제 처리되고 true를 반환한다")
        void delete_activeComment_marksDeletedAndReturnsTrue() {
            CommentEntity comment = activeComment();
            doNothing().when(plService).validatePlaylistActive(1L);
            when(commentRepository.findByCommentIdAndPlaylistId(10L, 1L))
                    .thenReturn(Optional.of(comment));

            Boolean result = commentService.commentDelete(1L, 10L);

            assertThat(result).isTrue();
            assertThat(comment.isCommentDelete()).isTrue();
        }

        @Test
        @DisplayName("삭제된 플레이리스트면 PLAYLIST_DELETED 예외가 발생한다")
        void delete_deletedPlaylist_throws() {
            doThrow(new BusinessException(ErrorCode.PLAYLIST_DELETED))
                    .when(plService).validatePlaylistActive(1L);

            assertBusinessException(
                    () -> commentService.commentDelete(1L, 10L),
                    ErrorCode.PLAYLIST_DELETED
            );
            verify(commentRepository, never()).findByCommentIdAndPlaylistId(any(), any());
        }

        @Test
        @DisplayName("존재하지 않는 댓글이면 ID_NOT_FOUND 예외가 발생한다")
        void delete_commentNotFound_throws() {
            doNothing().when(plService).validatePlaylistActive(1L);
            when(commentRepository.findByCommentIdAndPlaylistId(999L, 1L))
                    .thenReturn(Optional.empty());

            assertBusinessException(
                    () -> commentService.commentDelete(1L, 999L),
                    ErrorCode.ID_NOT_FOUND
            );
        }

        @Test
        @DisplayName("이미 삭제된 댓글이면 MEMBER_DELETED 예외가 발생한다 (현재 코드 기준)")
        void delete_alreadyDeletedComment_throws() {
            CommentEntity comment = activeComment();
            comment.delete();
            doNothing().when(plService).validatePlaylistActive(1L);
            when(commentRepository.findByCommentIdAndPlaylistId(10L, 1L))
                    .thenReturn(Optional.of(comment));

            assertBusinessException(
                    () -> commentService.commentDelete(1L, 10L),
                    ErrorCode.COMMENT_DELETED
            );
        }
    }

    @Nested
    @DisplayName("commentFindAll")
    class CommentFindAllTest {

        @Test
        @DisplayName("정상 회원 + 활성 플레이리스트면 댓글 목록을 반환한다")
        void findAll_activeUserAndPlaylist_returnsComments() {
            UserEntity user = activeUser(); // nickname = "nick"
            when(userRepository.findByMemberId("tester01")).thenReturn(Optional.of(user));
            doNothing().when(plService).validatePlaylistActive(1L);

            CommentEntity comment = activeComment(); // 작성자 닉네임 = "authorNick"
            when(commentRepository.findByPlaylistIdAndCommentDeleteFalse(1L))
                    .thenReturn(List.of(comment));

            List<ResponseCommentDto> result = commentService.commentFindAll("tester01", 1L);

            assertThat(result).hasSize(1);
            ResponseCommentDto dto = result.get(0);
            assertThat(dto.getMemberId()).isEqualTo("author01");
            assertThat(dto.getUserComment()).isEqualTo("좋은 곡이네요");
            assertThat(dto.getNickName()).isEqualTo("authorNick");
        }

        @Test
        @DisplayName("존재하지 않는 회원이면 ID_NOT_FOUND 예외가 발생한다")
        void findAll_userNotFound_throws() {
            when(userRepository.findByMemberId("noone")).thenReturn(Optional.empty());

            assertBusinessException(
                    () -> commentService.commentFindAll("noone", 1L),
                    ErrorCode.ID_NOT_FOUND
            );
        }

        @Test
        @DisplayName("탈퇴한 회원이면 MEMBER_DELETED 예외가 발생한다")
        void findAll_deletedUser_throws() {
            when(userRepository.findByMemberId("tester01")).thenReturn(Optional.of(deletedUser()));

            assertBusinessException(
                    () -> commentService.commentFindAll("tester01", 1L),
                    ErrorCode.MEMBER_DELETED
            );
        }

        @Test
        @DisplayName("삭제된 플레이리스트면 PLAYLIST_DELETED 예외가 발생한다")
        void findAll_deletedPlaylist_throws() {
            UserEntity user = activeUser();
            when(userRepository.findByMemberId("tester01")).thenReturn(Optional.of(user));
            doThrow(new BusinessException(ErrorCode.PLAYLIST_DELETED))
                    .when(plService).validatePlaylistActive(1L);

            assertBusinessException(
                    () -> commentService.commentFindAll("tester01", 1L),
                    ErrorCode.PLAYLIST_DELETED
            );
        }

        @Test
        @DisplayName("댓글이 없으면 빈 리스트를 반환한다")
        void findAll_noComments_returnsEmptyList() {
            UserEntity user = activeUser();
            when(userRepository.findByMemberId("tester01")).thenReturn(Optional.of(user));
            doNothing().when(plService).validatePlaylistActive(1L);
            when(commentRepository.findByPlaylistIdAndCommentDeleteFalse(1L))
                    .thenReturn(List.of());

            List<ResponseCommentDto> result = commentService.commentFindAll("tester01", 1L);

            assertThat(result).isEmpty();
        }
    }
}
