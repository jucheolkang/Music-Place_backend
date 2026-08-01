package org.musicplace.playList.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.musicplace.common.config.BaseServiceTest;
import org.musicplace.global.exception.BusinessException;
import org.musicplace.global.exception.ErrorCode;
import org.musicplace.playList.domain.OnOff;
import org.musicplace.playList.domain.PLEntity;
import org.musicplace.playList.dto.PLSaveDto;
import org.musicplace.playList.dto.PLUpdateDto;
import org.musicplace.playList.dto.ResponsePLDto;
import org.musicplace.playList.repository.PLRepository;
import org.musicplace.user.domain.UserEntity;
import org.musicplace.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PlaylistServiceTest extends BaseServiceTest {

    @Mock
    private PLRepository plRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PLService plService;

    @Test
    @DisplayName("플레이리스트를 생성한다")
     void plSave() {

        // given
        PLSaveDto dto = PLSaveDto.builder()
                .title("My Playlist")
                .onOff(OnOff.Public)
                .coverImg("cover.jpg")
                .comment("comment")
                .build();

        UserEntity user = UserEntity.builder()
                .memberId("tester")
                .nickname("tester")
                .pw("pw")
                .email("test@test.com")
                .role("ROLE_USER")
                .build();

        when(userRepository.findByMemberId("tester"))
                .thenReturn(Optional.of(user));

        ArgumentCaptor<PLEntity> captor =
                ArgumentCaptor.forClass(PLEntity.class);

        // when
        plService.plSave("tester", dto);

        // then
        verify(plRepository).save(captor.capture());

        PLEntity saved = captor.getValue();

        assertThat(saved.getMemberId()).isEqualTo("tester");
        assertThat(saved.getTitle()).isEqualTo("My Playlist");
        assertThat(saved.getNickname()).isEqualTo("tester");
        assertThat(saved.getCoverImg()).isEqualTo("cover.jpg");
        assertThat(saved.getComment()).isEqualTo("comment");
        assertThat(saved.getOnOff()).isEqualTo(OnOff.Public);
    }

    @Test
    @DisplayName("존재하지 않는 회원이면 예외가 발생한다")
    void plSave_UserNotFound() {

        // given
        PLSaveDto dto = PLSaveDto.builder()
                .title("title")
                .onOff(OnOff.Public)
                .build();

        when(userRepository.findByMemberId("tester"))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                plService.plSave("tester", dto))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ID_NOT_FOUND);

        verify(userRepository).findByMemberId("tester");
    }

    @Test
    @DisplayName("탈퇴한 회원은 플레이리스트를 생성할 수 없다")
    void plSave_DeletedUser() {

        // given
        UserEntity user = UserEntity.builder()
                .memberId("tester")
                .nickname("tester")
                .pw("pw")
                .email("test@test.com")
                .role("ROLE_USER")
                .build();

        user.deleteAccount();

        when(userRepository.findByMemberId("tester"))
                .thenReturn(Optional.of(user));

        PLSaveDto dto = PLSaveDto.builder()
                .title("playlist")
                .build();

        // when & then
        assertThatThrownBy(() ->
                plService.plSave("tester", dto))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MEMBER_DELETED);
    }

    @Test
    @DisplayName("플레이리스트를 수정한다")
    void plUpdate() {

        // given
        PLEntity playlist = PLEntity.builder()
                .memberId("tester")
                .title("old")
                .nickname("tester")
                .onOff(OnOff.Public)
                .coverImg("old.jpg")
                .comment("old comment")
                .build();

        when(plRepository.findById(1L))
                .thenReturn(Optional.of(playlist));

        PLUpdateDto dto = PLUpdateDto.builder()
                .title("new")
                .onOff(OnOff.Private)
                .coverImg("new.jpg")
                .comment("new comment")
                .build();

        // when
        plService.plUpdate(1L, dto);

        // then
        assertThat(playlist.getTitle()).isEqualTo("new");
        assertThat(playlist.getOnOff()).isEqualTo(OnOff.Private);
        assertThat(playlist.getCoverImg()).isEqualTo("new.jpg");
        assertThat(playlist.getComment()).isEqualTo("new comment");
    }

    @Test
    @DisplayName("존재하지 않는 플레이리스트 수정 시 예외가 발생한다")
    void plUpdate_NotFound() {

        // given
        PLUpdateDto dto = PLUpdateDto.builder()
                .title("new title")
                .onOff(OnOff.Public)
                .coverImg("cover")
                .comment("comment")
                .build();


        when(plRepository.findById(1L))
                .thenReturn(Optional.empty());


        // when & then
        assertThatThrownBy(() ->
                plService.plUpdate(1L, dto))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PLAYLIST_NOT_FOUND);


        verify(plRepository).findById(1L);
    }

    @Test
    @DisplayName("삭제된 플레이리스트는 수정할 수 없다")
    void plUpdate_DeletedPlaylist() {

        // given
        PLEntity playlist = PLEntity.builder()
                .memberId("tester")
                .title("playlist")
                .nickname("tester")
                .onOff(OnOff.Public)
                .build();


        playlist.delete();


        when(plRepository.findById(1L))
                .thenReturn(Optional.of(playlist));


        PLUpdateDto dto = PLUpdateDto.builder()
                .title("new")
                .onOff(OnOff.Private)
                .build();


        // when & then
        assertThatThrownBy(() ->
                plService.plUpdate(1L, dto))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PLAYLIST_DELETED);

    }

    @Test
    @DisplayName("플레이리스트를 삭제한다")
    void plDelete() {

        // given
        PLEntity playlist = PLEntity.builder()
                .memberId("tester")
                .title("playlist")
                .nickname("tester")
                .onOff(OnOff.Public)
                .build();


        when(plRepository.findById(1L))
                .thenReturn(Optional.of(playlist));


        // when
        plService.plDelete(1L);


        // then
        assertThat(playlist.isDeleteState())
                .isTrue();

    }

    @Test
    @DisplayName("존재하지 않는 플레이리스트 삭제 시 예외가 발생한다")
    void plDelete_NotFound() {


        // given
        when(plRepository.findById(1L))
                .thenReturn(Optional.empty());


        // when & then
        assertThatThrownBy(() ->
                plService.plDelete(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PLAYLIST_NOT_FOUND);


    }

    @Test
    @DisplayName("이미 삭제된 플레이리스트 삭제 시 예외가 발생한다")
    void plDelete_AlreadyDeleted() {


        // given
        PLEntity playlist = PLEntity.builder()
                .memberId("tester")
                .title("playlist")
                .nickname("tester")
                .onOff(OnOff.Public)
                .build();


        playlist.delete();


        when(plRepository.findById(1L))
                .thenReturn(Optional.of(playlist));


        // when & then
        assertThatThrownBy(() ->
                plService.plDelete(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PLAYLIST_DELETED);

    }

    @Test
    @DisplayName("내 플레이리스트 목록을 조회한다")
    void findMyPlaylists() {


        // given
        ResponsePLDto dto =
                new ResponsePLDto(
                        1L,
                        "playlist",
                        "tester",
                        "cover.jpg",
                        OnOff.Public,
                        "comment",
                        "tester"
                );


        when(plRepository.findMyPlaylists("tester"))
                .thenReturn(List.of(dto));


        // when
        List<ResponsePLDto> result =
                plService.findMyPlaylists("tester");


        // then
        assertThat(result)
                .hasSize(1);

        assertThat(result.get(0).getPlaylistId())
                .isEqualTo(1L);


        verify(plRepository)
                .findMyPlaylists("tester");

    }

    @Test
    @DisplayName("내 플레이리스트 목록을 조회한다")
    void findMyPlaylistsTest() {

        // given
        String memberId = "test-user";

        ResponsePLDto dto = new ResponsePLDto(
                1L,
                "테스트 플레이리스트",
                "tester",
                "cover.jpg",
                OnOff.Public,
                "comment",
                memberId
        );

        List<ResponsePLDto> playlists = List.of(dto);

        when(plRepository.findMyPlaylists(memberId))
                .thenReturn(playlists);


        // when
        List<ResponsePLDto> result =
                plService.findMyPlaylists(memberId);


        // then
        assertThat(result)
                .hasSize(1);

        assertThat(result.get(0).getPlaylistId())
                .isEqualTo(1L);

        assertThat(result.get(0).getPlTitle())
                .isEqualTo("테스트 플레이리스트");


        verify(plRepository)
                .findMyPlaylists(memberId);
    }

    @Test
    @DisplayName("내 플레이리스트 개수를 조회한다")
    void countMyPlaylists() {

        // given
        String memberId = "test-user";

        when(plRepository.countMyPlaylists(memberId))
                .thenReturn(5L);


        // when
        Long result =
                plService.countMyPlaylists(memberId);


        // then
        assertThat(result)
                .isEqualTo(5L);


        verify(plRepository)
                .countMyPlaylists(memberId);
    }

    @Test
    @DisplayName("다른 사용자의 공개 플레이리스트 개수를 조회한다")
    void countOtherPublicPlaylists() {

        // given
        String otherMemberId = "other-user";

        when(plRepository.countOtherPublicPlaylists(otherMemberId))
                .thenReturn(3L);


        // when
        Long result =
                plService.countOtherPublicPlaylists(otherMemberId);


        // then
        assertThat(result)
                .isEqualTo(3L);


        verify(plRepository)
                .countOtherPublicPlaylists(otherMemberId);
    }

    @Test
    @DisplayName("다른 사용자의 공개 플레이리스트 목록을 조회한다")
    void getOtherUserPublicPlaylists() {

        // given
        String otherMemberId = "other-user";

        ResponsePLDto dto = new ResponsePLDto(
                10L,
                "공개 플레이리스트",
                "other",
                "cover.png",
                OnOff.Public,
                "공개",
                otherMemberId
        );


        when(plRepository.findOtherUserPublicPlaylists(otherMemberId))
                .thenReturn(List.of(dto));


        // when
        List<ResponsePLDto> result =
                plService.getOtherUserPublicPlaylists(otherMemberId);


        // then
        assertThat(result)
                .hasSize(1);

        assertThat(result.get(0).getMemberId())
                .isEqualTo(otherMemberId);

        assertThat(result.get(0).getOnOff())
                .isEqualTo(OnOff.Public);


        verify(plRepository)
                .findOtherUserPublicPlaylists(otherMemberId);
    }

    @Test
    @DisplayName("전체 공개 플레이리스트를 조회한다")
    void findPublicPlaylists() {

        // given

        ResponsePLDto dto = new ResponsePLDto(
                20L,
                "전체 공개",
                "tester",
                "cover.jpg",
                OnOff.Public,
                "comment",
                "user"
        );


        when(plRepository.findAllPublicPlaylists())
                .thenReturn(List.of(dto));


        // when

        List<ResponsePLDto> result =
                plService.findPublicPlaylists();


        // then

        assertThat(result)
                .hasSize(1);

        assertThat(result.get(0).getOnOff())
                .isEqualTo(OnOff.Public);


        verify(plRepository)
                .findAllPublicPlaylists();
    }


}
