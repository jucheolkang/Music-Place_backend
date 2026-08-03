package org.musicplace.playlist.service;

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
import org.musicplace.playList.domain.OnOff;
import org.musicplace.playList.domain.PLEntity;
import org.musicplace.playList.dto.PLSaveDto;
import org.musicplace.playList.dto.PLUpdateDto;
import org.musicplace.playList.dto.ResponsePLDto;
import org.musicplace.playList.repository.PLRepository;
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
class PLServiceTest {

    @Mock
    private PLRepository plRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PLService plService;

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

    private PLEntity activePlaylist() {
        return PLEntity.builder()
                .memberId("tester01")
                .title("my playlist")
                .nickname("nick")
                .onOff(OnOff.Public)
                .coverImg("http://cover")
                .comment("comment")
                .build();
    }

    private void assertBusinessException(Runnable action, ErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(expected);
    }

    @Nested
    @DisplayName("plSave")
    class PlSaveTest {

        @Test
        @DisplayName("정상 회원은 자신의 nickname으로 플레이리스트를 생성한다")
        void save_activeUser_createsPlaylistWithUserNickname() {
            UserEntity user = activeUser();
            when(userRepository.findByMemberId("tester01")).thenReturn(Optional.of(user));

            PLSaveDto dto = PLSaveDto.builder()
                    .title("my playlist")
                    .onOff(OnOff.Public)
                    .coverImg("http://cover")
                    .comment("comment")
                    .build();

            plService.plSave("tester01", dto);

            ArgumentCaptor<PLEntity> captor = ArgumentCaptor.forClass(PLEntity.class);
            verify(plRepository).save(captor.capture());

            PLEntity saved = captor.getValue();
            assertThat(saved.getMemberId()).isEqualTo("tester01");
            assertThat(saved.getTitle()).isEqualTo("my playlist");
            assertThat(saved.getNickname()).isEqualTo("nick");
            assertThat(saved.getOnOff()).isEqualTo(OnOff.Public);
        }

        @Test
        @DisplayName("존재하지 않는 회원이면 ID_NOT_FOUND 예외가 발생한다")
        void save_userNotFound_throws() {
            when(userRepository.findByMemberId("noone")).thenReturn(Optional.empty());

            PLSaveDto dto = PLSaveDto.builder().title("t").onOff(OnOff.Public).build();

            assertBusinessException(
                    () -> plService.plSave("noone", dto),
                    ErrorCode.ID_NOT_FOUND
            );
            verify(plRepository, never()).save(any());
        }

        @Test
        @DisplayName("탈퇴한 회원이면 MEMBER_DELETED 예외가 발생한다")
        void save_deletedUser_throws() {
            when(userRepository.findByMemberId("tester01")).thenReturn(Optional.of(deletedUser()));

            PLSaveDto dto = PLSaveDto.builder().title("t").onOff(OnOff.Public).build();

            assertBusinessException(
                    () -> plService.plSave("tester01", dto),
                    ErrorCode.MEMBER_DELETED
            );
            verify(plRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("plUpdate")
    class PlUpdateTest {

        @Test
        @DisplayName("활성 플레이리스트는 정상 수정된다")
        void update_activePlaylist_updates() {
            PLEntity pl = activePlaylist();
            when(plRepository.findById(1L)).thenReturn(Optional.of(pl));

            PLUpdateDto dto = PLUpdateDto.builder()
                    .title("new title")
                    .onOff(OnOff.Private)
                    .coverImg("http://new-cover")
                    .comment("new comment")
                    .build();

            plService.plUpdate(1L, dto);

            assertThat(pl.getTitle()).isEqualTo("new title");
            assertThat(pl.getOnOff()).isEqualTo(OnOff.Private);
            assertThat(pl.getCoverImg()).isEqualTo("http://new-cover");
            assertThat(pl.getComment()).isEqualTo("new comment");
        }

        @Test
        @DisplayName("존재하지 않는 플레이리스트면 PLAYLIST_NOT_FOUND 예외가 발생한다")
        void update_notFound_throws() {
            when(plRepository.findById(999L)).thenReturn(Optional.empty());

            PLUpdateDto dto = PLUpdateDto.builder().title("t").onOff(OnOff.Public).build();

            assertBusinessException(
                    () -> plService.plUpdate(999L, dto),
                    ErrorCode.PLAYLIST_NOT_FOUND
            );
        }

        @Test
        @DisplayName("삭제된 플레이리스트면 PLAYLIST_DELETED 예외가 발생한다")
        void update_deletedPlaylist_throws() {
            PLEntity pl = activePlaylist();
            pl.delete();
            when(plRepository.findById(1L)).thenReturn(Optional.of(pl));

            PLUpdateDto dto = PLUpdateDto.builder().title("t").onOff(OnOff.Public).build();

            assertBusinessException(
                    () -> plService.plUpdate(1L, dto),
                    ErrorCode.PLAYLIST_DELETED
            );
        }
    }

    @Nested
    @DisplayName("plDelete")
    class PlDeleteTest {

        @Test
        @DisplayName("활성 플레이리스트는 soft delete 처리된다")
        void delete_activePlaylist_marksDeleted() {
            PLEntity pl = activePlaylist();
            when(plRepository.findById(1L)).thenReturn(Optional.of(pl));

            plService.plDelete(1L);

            assertThat(pl.isDeleteState()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 플레이리스트면 PLAYLIST_NOT_FOUND 예외가 발생한다")
        void delete_notFound_throws() {
            when(plRepository.findById(999L)).thenReturn(Optional.empty());

            assertBusinessException(
                    () -> plService.plDelete(999L),
                    ErrorCode.PLAYLIST_NOT_FOUND
            );
        }

        @Test
        @DisplayName("이미 삭제된 플레이리스트면 PLAYLIST_DELETED 예외가 발생한다")
        void delete_alreadyDeleted_throws() {
            PLEntity pl = activePlaylist();
            pl.delete();
            when(plRepository.findById(1L)).thenReturn(Optional.of(pl));

            assertBusinessException(
                    () -> plService.plDelete(1L),
                    ErrorCode.PLAYLIST_DELETED
            );
        }
    }

    @Nested
    @DisplayName("조회/카운트 메서드")
    class QueryMethodsTest {

        @Test
        @DisplayName("findMyPlaylists는 Repository 결과를 그대로 반환한다")
        void findMyPlaylists_delegatesToRepository() {
            List<ResponsePLDto> expected = List.of(mock(ResponsePLDto.class));
            when(plRepository.findMyPlaylists("tester01")).thenReturn(expected);

            List<ResponsePLDto> result = plService.findMyPlaylists("tester01");

            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("countMyPlaylists는 Repository 결과를 그대로 반환한다")
        void countMyPlaylists_delegatesToRepository() {
            when(plRepository.countMyPlaylists("tester01")).thenReturn(5L);

            Long result = plService.countMyPlaylists("tester01");

            assertThat(result).isEqualTo(5L);
        }

        @Test
        @DisplayName("countOtherPublicPlaylists는 Repository 결과를 그대로 반환한다")
        void countOtherPublicPlaylists_delegatesToRepository() {
            when(plRepository.countOtherPublicPlaylists("other01")).thenReturn(3L);

            Long result = plService.countOtherPublicPlaylists("other01");

            assertThat(result).isEqualTo(3L);
        }

        @Test
        @DisplayName("getOtherUserPublicPlaylists는 Repository 결과를 그대로 반환한다")
        void getOtherUserPublicPlaylists_delegatesToRepository() {
            List<ResponsePLDto> expected = List.of(mock(ResponsePLDto.class));
            when(plRepository.findOtherUserPublicPlaylists("other01")).thenReturn(expected);

            List<ResponsePLDto> result = plService.getOtherUserPublicPlaylists("other01");

            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("findPublicPlaylists는 Repository 결과를 그대로 반환한다")
        void findPublicPlaylists_delegatesToRepository() {
            List<ResponsePLDto> expected = List.of(mock(ResponsePLDto.class));
            when(plRepository.findAllPublicPlaylists()).thenReturn(expected);

            List<ResponsePLDto> result = plService.findPublicPlaylists();

            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("validatePlaylistActive")
    class ValidatePlaylistActiveTest {

        @Test
        @DisplayName("활성 플레이리스트면 예외가 발생하지 않는다")
        void active_doesNotThrow() {
            when(plRepository.existsByPlaylistIdAndDeleteStateFalse(1L)).thenReturn(true);

            plService.validatePlaylistActive(1L);

            verify(plRepository).existsByPlaylistIdAndDeleteStateFalse(1L);
        }

        @Test
        @DisplayName("삭제되었거나 존재하지 않으면 PLAYLIST_DELETED 예외가 발생한다")
        void inactive_throws() {
            when(plRepository.existsByPlaylistIdAndDeleteStateFalse(1L)).thenReturn(false);

            assertBusinessException(
                    () -> plService.validatePlaylistActive(1L),
                    ErrorCode.PLAYLIST_DELETED
            );
        }
    }
}
