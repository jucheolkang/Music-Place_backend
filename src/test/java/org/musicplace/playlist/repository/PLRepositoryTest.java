package org.musicplace.playlist.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.musicplace.playList.domain.OnOff;
import org.musicplace.playList.domain.PLEntity;
import org.musicplace.playList.dto.ResponsePLDto;
import org.musicplace.playList.repository.PLRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PLRepositoryTest {

    @Autowired
    private PLRepository plRepository;

    @Autowired
    private TestEntityManager em;

    private PLEntity persistPlaylist(String memberId, String nickname, String title, OnOff onOff, boolean deleted) {
        PLEntity pl = PLEntity.builder()
                .memberId(memberId)
                .title(title)
                .nickname(nickname)
                .onOff(onOff)
                .coverImg("http://cover")
                .comment("comment")
                .build();

        em.persist(pl);

        if (deleted) {
            pl.delete();
            em.persist(pl);
        }

        return pl;
    }

    @Nested
    @DisplayName("existsByPlaylistIdAndDeleteStateFalse")
    class ExistsActiveTest {

        @Test
        @DisplayName("활성 플레이리스트면 true를 반환한다")
        void active_returnsTrue() {
            PLEntity pl = persistPlaylist("tester01", "nick", "title", OnOff.Public, false);
            em.flush();
            em.clear();

            boolean result = plRepository.existsByPlaylistIdAndDeleteStateFalse(pl.getPlaylistId());

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("삭제된 플레이리스트면 false를 반환한다")
        void deleted_returnsFalse() {
            PLEntity pl = persistPlaylist("tester01", "nick", "title", OnOff.Public, true);
            em.flush();
            em.clear();

            boolean result = plRepository.existsByPlaylistIdAndDeleteStateFalse(pl.getPlaylistId());

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 id면 false를 반환한다")
        void notExists_returnsFalse() {
            boolean result = plRepository.existsByPlaylistIdAndDeleteStateFalse(999L);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("countMyPlaylists (native query)")
    class CountMyPlaylistsTest {

        @Test
        @DisplayName("삭제되지 않은 자신의 플레이리스트 개수만 센다")
        void countsOnlyActiveOwnPlaylists() {
            persistPlaylist("tester01", "nick", "title1", OnOff.Public, false);
            persistPlaylist("tester01", "nick", "title2", OnOff.Private, false);
            persistPlaylist("tester01", "nick", "title3", OnOff.Public, true); // 삭제됨 - 제외
            persistPlaylist("other01", "nick2", "title4", OnOff.Public, false); // 다른 유저 - 제외
            em.flush();
            em.clear();

            Long count = plRepository.countMyPlaylists("tester01");

            assertThat(count).isEqualTo(2L);
        }

        @Test
        @DisplayName("플레이리스트가 없는 회원은 0을 반환한다")
        void noPlaylists_returnsZero() {
            Long count = plRepository.countMyPlaylists("noone");

            assertThat(count).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("countOtherPublicPlaylists (native query)")
    class CountOtherPublicPlaylistsTest {

        @Test
        @DisplayName("공개 + 삭제되지 않은 플레이리스트만 센다")
        void countsOnlyActivePublicPlaylists() {
            persistPlaylist("other01", "nick", "title1", OnOff.Public, false);
            persistPlaylist("other01", "nick", "title2", OnOff.Private, false); // 비공개 - 제외
            persistPlaylist("other01", "nick", "title3", OnOff.Public, true);   // 삭제됨 - 제외
            em.flush();
            em.clear();

            Long count = plRepository.countOtherPublicPlaylists("other01");

            assertThat(count).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("findMyPlaylists (JPQL)")
    class FindMyPlaylistsTest {

        @Test
        @DisplayName("자신의 활성 플레이리스트를 최신순으로 반환한다")
        void returnsOwnActivePlaylistsDescByIdOrder() {
            PLEntity first = persistPlaylist("tester01", "nick", "title1", OnOff.Public, false);
            PLEntity second = persistPlaylist("tester01", "nick", "title2", OnOff.Private, false);
            persistPlaylist("tester01", "nick", "title3", OnOff.Public, true); // 삭제됨 - 제외
            persistPlaylist("other01", "nick2", "title4", OnOff.Public, false); // 다른 유저 - 제외
            em.flush();
            em.clear();

            List<ResponsePLDto> result = plRepository.findMyPlaylists("tester01");

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getPlaylistId()).isEqualTo(second.getPlaylistId());
            assertThat(result.get(1).getPlaylistId()).isEqualTo(first.getPlaylistId());
        }

        @Test
        @DisplayName("플레이리스트가 없으면 빈 리스트를 반환한다")
        void noPlaylists_returnsEmptyList() {
            List<ResponsePLDto> result = plRepository.findMyPlaylists("noone");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findOtherUserPublicPlaylists (JPQL)")
    class FindOtherUserPublicPlaylistsTest {

        @Test
        @DisplayName("해당 유저의 공개 + 활성 플레이리스트만 반환한다")
        void returnsOnlyPublicActivePlaylistsOfGivenUser() {
            PLEntity publicOne = persistPlaylist("other01", "nick", "title1", OnOff.Public, false);
            persistPlaylist("other01", "nick", "title2", OnOff.Private, false); // 비공개 - 제외
            persistPlaylist("other01", "nick", "title3", OnOff.Public, true);   // 삭제됨 - 제외
            persistPlaylist("tester01", "nick2", "title4", OnOff.Public, false); // 다른 유저 - 제외
            em.flush();
            em.clear();

            List<ResponsePLDto> result = plRepository.findOtherUserPublicPlaylists("other01");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPlaylistId()).isEqualTo(publicOne.getPlaylistId());
        }
    }

    @Nested
    @DisplayName("findAllPublicPlaylists (JPQL)")
    class FindAllPublicPlaylistsTest {

        @Test
        @DisplayName("모든 유저의 공개 + 활성 플레이리스트를 최신순으로 반환한다")
        void returnsAllPublicActivePlaylistsDescByIdOrder() {
            PLEntity first = persistPlaylist("tester01", "nick", "title1", OnOff.Public, false);
            persistPlaylist("tester01", "nick", "title2", OnOff.Private, false); // 비공개 - 제외
            PLEntity second = persistPlaylist("other01", "nick2", "title3", OnOff.Public, false);
            persistPlaylist("other01", "nick2", "title4", OnOff.Public, true); // 삭제됨 - 제외
            em.flush();
            em.clear();

            List<ResponsePLDto> result = plRepository.findAllPublicPlaylists();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getPlaylistId()).isEqualTo(second.getPlaylistId());
            assertThat(result.get(1).getPlaylistId()).isEqualTo(first.getPlaylistId());
        }
    }
}
