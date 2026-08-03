package org.musicplace.user.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.musicplace.user.domain.Gender;
import org.musicplace.user.domain.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager em;

    private UserEntity persistUser(String memberId, String email) {
        UserEntity user = UserEntity.builder()
                .memberId(memberId)
                .pw("encodedPw")
                .gender(Gender.male)
                .email(email)
                .nickname("nick_" + memberId)
                .name("홍길동")
                .role("ROLE_USER")
                .build();

        em.persist(user);
        return user;
    }

    @Nested
    @DisplayName("findByMemberId")
    class FindByMemberIdTest {

        @Test
        @DisplayName("존재하는 memberId면 UserEntity를 반환한다")
        void found() {
            persistUser("tester01", "tester01@test.com");
            em.flush();
            em.clear();

            Optional<UserEntity> result = userRepository.findByMemberId("tester01");

            assertThat(result).isPresent();
            assertThat(result.get().getMemberId()).isEqualTo("tester01");
            assertThat(result.get().getEmail()).isEqualTo("tester01@test.com");
        }

        @Test
        @DisplayName("존재하지 않는 memberId면 빈 Optional을 반환한다")
        void notFound() {
            Optional<UserEntity> result = userRepository.findByMemberId("noone");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByMemberId")
    class ExistsByMemberIdTest {

        @Test
        @DisplayName("존재하는 memberId면 true를 반환한다")
        void exists() {
            persistUser("tester01", "tester01@test.com");
            em.flush();
            em.clear();

            boolean result = userRepository.existsByMemberId("tester01");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 memberId면 false를 반환한다")
        void notExists() {
            boolean result = userRepository.existsByMemberId("noone");

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("findByEmail")
    class FindByEmailTest {

        @Test
        @DisplayName("존재하는 email이면 UserEntity를 반환한다")
        void found() {
            persistUser("tester01", "tester01@test.com");
            em.flush();
            em.clear();

            Optional<UserEntity> result = userRepository.findByEmail("tester01@test.com");

            assertThat(result).isPresent();
            assertThat(result.get().getMemberId()).isEqualTo("tester01");
        }

        @Test
        @DisplayName("존재하지 않는 email이면 빈 Optional을 반환한다")
        void notFound() {
            Optional<UserEntity> result = userRepository.findByEmail("none@test.com");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("대소문자가 다른 email로는 조회되지 않는다")
        void caseSensitive() {
            persistUser("tester01", "tester01@test.com");
            em.flush();
            em.clear();

            Optional<UserEntity> result = userRepository.findByEmail("TESTER01@TEST.COM");

            assertThat(result).isEmpty();
        }
    }
}
