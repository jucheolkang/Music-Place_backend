package org.musicplace.user.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.musicplace.common.config.BaseRepositoryTest;
import org.musicplace.common.fixture.UserFixture;
import org.musicplace.user.domain.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("회원 ID로 회원을 조회한다")
    void findByMemberId() {
        // given
        UserEntity user = UserFixture.createUser();
        userRepository.save(user);

        // when
        Optional<UserEntity> result = userRepository.findByMemberId("test-user");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getMemberId()).isEqualTo("test-user");
        assertThat(result.get().getEmail()).isEqualTo("test@test.com");
        assertThat(result.get().getNickname()).isEqualTo("tester");
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID 조회 시 빈 Optional을 반환한다")
    void findByMemberId_ReturnEmptyOptional_WhenMemberDoesNotExist() {
        // when
        Optional<UserEntity> result = userRepository.findByMemberId("not-found");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("기본 PK로 회원을 조회한다")
    void findById() {
        // given
        UserEntity user = UserFixture.createUser();
        userRepository.save(user);

        // when
        Optional<UserEntity> result = userRepository.findById("test-user");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getMemberId()).isEqualTo("test-user");
    }

    @Test
    @DisplayName("동일한 memberId를 가진 회원은 하나만 조회된다")
    void findByMemberId_ReturnSingleUser() {
        // given
        UserEntity user = UserFixture.createUser();
        userRepository.save(user);

        // when
        Optional<UserEntity> result = userRepository.findByMemberId("test-user");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getMemberId()).isEqualTo("test-user");
    }
}
