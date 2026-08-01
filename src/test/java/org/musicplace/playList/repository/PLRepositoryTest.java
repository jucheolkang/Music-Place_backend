package org.musicplace.playList.repository;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.musicplace.playList.domain.OnOff;
import org.musicplace.playList.domain.PLEntity;
import org.musicplace.playList.dto.ResponsePLDto;
import org.musicplace.common.config.BaseRepositoryTest;
import org.musicplace.user.domain.UserEntity;
import org.musicplace.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.musicplace.common.fixture.UserFixture.createUser;


class PLRepositoryTest extends BaseRepositoryTest {


    @Autowired
    private PLRepository plRepository;

    @Autowired
    private UserRepository userRepository;

    private PLEntity createPlaylist(
            String memberId,
            String title,
            OnOff onOff
    ) {

        return PLEntity.builder()
                .memberId(memberId)
                .title(title)
                .nickname("tester")
                .onOff(onOff)
                .coverImg("cover.jpg")
                .comment("comment")
                .build();
    }

    @Test
    @DisplayName("회원의 플레이리스트 목록을 조회한다")
    void findMyPlaylistsTest() {


        // given

        PLEntity playlist1 =
                createPlaylist(
                        "user1",
                        "playlist1",
                        OnOff.Public
                );


        PLEntity playlist2 =
                createPlaylist(
                        "user1",
                        "playlist2",
                        OnOff.Private
                );


        PLEntity other =
                createPlaylist(
                        "user2",
                        "other playlist",
                        OnOff.Public
                );


        plRepository.saveAll(
                List.of(
                        playlist1,
                        playlist2,
                        other
                )
        );


        // when

        List<ResponsePLDto> result =
                plRepository.findMyPlaylists("user1");


        // then

        assertThat(result)
                .hasSize(2);


        assertThat(result.get(0).getMemberId())
                .isEqualTo("user1");


        assertThat(result.get(0).getPlTitle())
                .isEqualTo("playlist2");
    }

    @Test
    @DisplayName("삭제된 플레이리스트는 조회되지 않는다")
    void findMyPlaylists_ignoreDeletedPlaylist() {


        // given

        PLEntity playlist =
                createPlaylist(
                        "user1",
                        "deleted playlist",
                        OnOff.Public
                );


        playlist.delete();


        plRepository.save(playlist);


        // when

        List<ResponsePLDto> result =
                plRepository.findMyPlaylists("user1");


        // then

        assertThat(result)
                .isEmpty();

    }

    @Test
    @DisplayName("내 플레이리스트 개수를 조회한다")
    void countMyPlaylistsTest() {


        // given

        plRepository.save(
                createPlaylist(
                        "user1",
                        "playlist1",
                        OnOff.Public
                )
        );


        plRepository.save(
                createPlaylist(
                        "user1",
                        "playlist2",
                        OnOff.Private
                )
        );


        // when

        Long count =
                plRepository.countMyPlaylists("user1");


        // then

        assertThat(count)
                .isEqualTo(2L);

    }

    @Test
    @DisplayName("다른 사용자의 공개 플레이리스트 개수를 조회한다")
    void countOtherPublicPlaylistsTest() {


        // given

        plRepository.save(
                createPlaylist(
                        "user1",
                        "public",
                        OnOff.Public
                )
        );


        plRepository.save(
                createPlaylist(
                        "user1",
                        "private",
                        OnOff.Private
                )
        );


        // when

        Long count =
                plRepository.countOtherPublicPlaylists("user1");


        // then

        assertThat(count)
                .isEqualTo(1L);

    }

    @Test
    @DisplayName("다른 사용자의 공개 플레이리스트를 조회한다")
    void findOtherUserPublicPlaylistsTest() {


        // given

        plRepository.save(
                createPlaylist(
                        "user2",
                        "public playlist",
                        OnOff.Public
                )
        );


        plRepository.save(
                createPlaylist(
                        "user2",
                        "private playlist",
                        OnOff.Private
                )
        );


        // when

        List<ResponsePLDto> result =
                plRepository.findOtherUserPublicPlaylists("user2");


        // then

        assertThat(result)
                .hasSize(1);


        assertThat(result.get(0).getOnOff())
                .isEqualTo(OnOff.Public);

    }

    @Test
    @DisplayName("전체 공개 플레이리스트를 조회한다")
    void findAllPublicPlaylistsTest() {


        // given

        plRepository.save(
                createPlaylist(
                        "user1",
                        "public1",
                        OnOff.Public
                )
        );


        plRepository.save(
                createPlaylist(
                        "user2",
                        "private1",
                        OnOff.Private
                )
        );


        // when

        List<ResponsePLDto> result =
                plRepository.findAllPublicPlaylists();


        // then

        assertThat(result)
                .hasSize(1);


        assertThat(result.get(0).getOnOff())
                .isEqualTo(OnOff.Public);

    }

    @Test
    @DisplayName("내 플레이리스트 개수를 조회한다")
    void countMyPlaylists() {

        // given
        UserEntity user = createUser("tester");
        userRepository.save(user);

        plRepository.save(createPlaylist("tester", "A", OnOff.Public));
        plRepository.save(createPlaylist("tester", "B", OnOff.Private));

        PLEntity deleted = createPlaylist("tester", "C", OnOff.Public);
        deleted.delete();
        plRepository.save(deleted);

        // when
        Long count = plRepository.countMyPlaylists("tester");

        // then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("다른 사용자의 공개 플레이리스트 개수를 조회한다")
    void countOtherPublicPlaylists() {

        // given
        UserEntity user = createUser("other");
        userRepository.save(user);

        plRepository.save(createPlaylist("other", "A", OnOff.Public));
        plRepository.save(createPlaylist("other", "B", OnOff.Public));
        plRepository.save(createPlaylist("other", "C", OnOff.Private));

        PLEntity deleted = createPlaylist("other", "D", OnOff.Public);
        deleted.delete();
        plRepository.save(deleted);

        // when
        Long count = plRepository.countOtherPublicPlaylists("other");

        // then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("내 플레이리스트를 최신순으로 조회한다")
    void findMyPlaylists() {

        // given
        UserEntity user = createUser("tester");
        userRepository.save(user);

        PLEntity first =
                plRepository.save(createPlaylist("tester", "first", OnOff.Public));

        PLEntity second =
                plRepository.save(createPlaylist("tester", "second", OnOff.Private));

        // when
        List<ResponsePLDto> result =
                plRepository.findMyPlaylists("tester");

        // then
        assertThat(result).hasSize(2);

        assertThat(result.get(0).getPlaylistId())
                .isEqualTo(second.getPlaylistId());

        assertThat(result.get(1).getPlaylistId())
                .isEqualTo(first.getPlaylistId());
    }

    @Test
    @DisplayName("다른 사용자의 공개 플레이리스트만 조회한다")
    void findOtherUserPublicPlaylists() {

        // given
        UserEntity user = createUser("other");
        userRepository.save(user);

        plRepository.save(createPlaylist("other", "public1", OnOff.Public));
        plRepository.save(createPlaylist("other", "public2", OnOff.Public));
        plRepository.save(createPlaylist("other", "private", OnOff.Private));

        // when
        List<ResponsePLDto> result =
                plRepository.findOtherUserPublicPlaylists("other");

        // then
        assertThat(result).hasSize(2);

        assertThat(result)
                .extracting(ResponsePLDto::getPlTitle)
                .containsExactly("public2", "public1");
    }

    @Test
    @DisplayName("전체 공개 플레이리스트를 조회한다")
    void findAllPublicPlaylists() {

        // given
        userRepository.save(createUser("user1"));
        userRepository.save(createUser("user2"));

        plRepository.save(createPlaylist("user1", "A", OnOff.Public));
        plRepository.save(createPlaylist("user2", "B", OnOff.Public));
        plRepository.save(createPlaylist("user1", "C", OnOff.Private));

        // when
        List<ResponsePLDto> result =
                plRepository.findAllPublicPlaylists();

        // then
        assertThat(result).hasSize(2);

        assertThat(result)
                .extracting(ResponsePLDto::getPlTitle)
                .containsExactly("B", "A");
    }

    @Test
    @DisplayName("삭제되지 않은 플레이리스트는 true를 반환한다")
    void existsByPlaylistIdAndDeleteStateFalse() {

        // given
        userRepository.save(createUser("tester"));

        PLEntity playlist =
                plRepository.save(createPlaylist("tester", "playlist", OnOff.Public));

        // when
        boolean exists =
                plRepository.existsByPlaylistIdAndDeleteStateFalse(
                        playlist.getPlaylistId());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("삭제된 플레이리스트는 false를 반환한다")
    void existsByPlaylistIdAndDeleteStateFalse_deleted() {

        // given
        userRepository.save(createUser("tester"));

        PLEntity playlist =
                createPlaylist("tester", "playlist", OnOff.Public);

        playlist.delete();

        playlist = plRepository.save(playlist);

        // when
        boolean exists =
                plRepository.existsByPlaylistIdAndDeleteStateFalse(
                        playlist.getPlaylistId());

        // then
        assertThat(exists).isFalse();
    }
}
