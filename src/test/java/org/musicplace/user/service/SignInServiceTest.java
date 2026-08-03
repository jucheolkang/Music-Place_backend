package org.musicplace.user.service;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.musicplace.global.exception.BusinessException;
import org.musicplace.global.exception.ErrorCode;
import org.musicplace.user.domain.Gender;
import org.musicplace.user.domain.UserEntity;
import org.musicplace.user.dto.SignInGetUserDataDto;
import org.musicplace.user.dto.SignInSaveDto;
import org.musicplace.user.dto.SignInUpdateDto;
import org.musicplace.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignInServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SignInService signInService;

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

    private void assertBusinessException(Runnable action, ErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(expected);
    }

    @Nested
    @DisplayName("save")
    class SaveTest {

        @Test
        @DisplayName("비밀번호를 인코딩하여 저장한다")
        void save_encodesPasswordAndSaves() {
            SignInSaveDto dto = SignInSaveDto.builder()
                    .member_id("tester01")
                    .pw("rawPw")
                    .name("홍길동")
                    .email("tester01@test.com")
                    .nickname("nick")
                    .gender(Gender.male)
                    .build();

            when(passwordEncoder.encode("rawPw")).thenReturn("encodedPw");

            signInService.save(dto);

            verify(passwordEncoder).encode("rawPw");

            org.mockito.ArgumentCaptor<UserEntity> captor =
                    org.mockito.ArgumentCaptor.forClass(UserEntity.class);
            verify(userRepository).save(captor.capture());

            UserEntity saved = captor.getValue();
            assertThat(saved.getMemberId()).isEqualTo("tester01");
            assertThat(saved.getPw()).isEqualTo("encodedPw");
            assertThat(saved.getRole()).isEqualTo("ROLE_USER");
        }
    }

    @Nested
    @DisplayName("update")
    class UpdateTest {

        @Test
        @DisplayName("정상 회원은 프로필이 갱신된다")
        void update_activeUser_updatesProfile() {
            UserEntity user = activeUser();
            when(userRepository.findById("tester01")).thenReturn(Optional.of(user));

            SignInUpdateDto dto = SignInUpdateDto.builder()
                    .name("새이름")
                    .email("new@test.com")
                    .nickname("newNick")
                    .profile_img_url("http://img")
                    .build();

            signInService.update("tester01", dto);

            assertThat(user.getName()).isEqualTo("새이름");
            assertThat(user.getEmail()).isEqualTo("new@test.com");
            assertThat(user.getNickname()).isEqualTo("newNick");
            assertThat(user.getProfileImgUrl()).isEqualTo("http://img");
        }

        @Test
        @DisplayName("존재하지 않는 회원이면 ID_NOT_FOUND 예외가 발생한다")
        void update_userNotFound_throws() {
            when(userRepository.findById("noone")).thenReturn(Optional.empty());

            SignInUpdateDto dto = SignInUpdateDto.builder().build();

            assertBusinessException(
                    () -> signInService.update("noone", dto),
                    ErrorCode.ID_NOT_FOUND
            );
        }

        @Test
        @DisplayName("탈퇴한 회원이면 MEMBER_DELETED 예외가 발생한다")
        void update_deletedUser_throws() {
            UserEntity user = deletedUser();
            when(userRepository.findById("tester01")).thenReturn(Optional.of(user));

            SignInUpdateDto dto = SignInUpdateDto.builder().build();

            assertBusinessException(
                    () -> signInService.update("tester01", dto),
                    ErrorCode.MEMBER_DELETED
            );
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTest {

        @Test
        @DisplayName("정상 회원은 탈퇴 처리된다")
        void delete_activeUser_marksDeleted() {
            UserEntity user = activeUser();
            when(userRepository.findById("tester01")).thenReturn(Optional.of(user));

            signInService.delete("tester01");

            assertThat(user.getDeleteAccount()).isTrue();
        }

        @Test
        @DisplayName("이미 탈퇴한 회원이면 MEMBER_DELETED 예외가 발생한다")
        void delete_alreadyDeleted_throws() {
            UserEntity user = deletedUser();
            when(userRepository.findById("tester01")).thenReturn(Optional.of(user));

            assertBusinessException(
                    () -> signInService.delete("tester01"),
                    ErrorCode.MEMBER_DELETED
            );
        }
    }

    @Nested
    @DisplayName("getUserData")
    class GetUserDataTest {

        @Test
        @DisplayName("정상 회원의 데이터를 dto로 반환한다")
        void getUserData_returnsDto() {
            UserEntity user = activeUser();
            when(userRepository.findById("tester01")).thenReturn(Optional.of(user));

            SignInGetUserDataDto result = signInService.getUserData("tester01");

            assertThat(result.getName()).isEqualTo(user.getName());
            assertThat(result.getEmail()).isEqualTo(user.getEmail());
            assertThat(result.getNickname()).isEqualTo(user.getNickname());
        }

        @Test
        @DisplayName("탈퇴한 회원이면 MEMBER_DELETED 예외가 발생한다")
        void getUserData_deletedUser_throws() {
            UserEntity user = deletedUser();
            when(userRepository.findById("tester01")).thenReturn(Optional.of(user));

            assertBusinessException(
                    () -> signInService.getUserData("tester01"),
                    ErrorCode.MEMBER_DELETED
            );
        }
    }

    @Nested
    @DisplayName("signInCheckSameId")
    class CheckSameIdTest {

        @Test
        @DisplayName("이미 존재하는 아이디면 false를 반환한다")
        void existingId_returnsFalse() {
            when(userRepository.existsByMemberId("tester01")).thenReturn(true);

            assertThat(signInService.signInCheckSameId("tester01")).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 아이디면 true를 반환한다")
        void newId_returnsTrue() {
            when(userRepository.existsByMemberId("newId")).thenReturn(false);

            assertThat(signInService.signInCheckSameId("newId")).isTrue();
        }
    }

    @Nested
    @DisplayName("forgetId")
    class ForgetIdTest {

        @Test
        @DisplayName("비밀번호가 일치하면 memberId를 반환한다")
        void pwMatches_returnsMemberId() {
            UserEntity user = activeUser();
            when(userRepository.findByEmail("tester01@test.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("rawPw", user.getPw())).thenReturn(true);

            String result = signInService.forgetId("rawPw", "tester01@test.com");

            assertThat(result).isEqualTo("tester01");
        }

        @Test
        @DisplayName("비밀번호가 일치하지 않으면 INVALID_CREDENTIALS 예외가 발생한다")
        void pwMismatch_throws() {
            UserEntity user = activeUser();
            when(userRepository.findByEmail("tester01@test.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongPw", user.getPw())).thenReturn(false);

            assertBusinessException(
                    () -> signInService.forgetId("wrongPw", "tester01@test.com"),
                    ErrorCode.INVALID_CREDENTIALS
            );
        }

        @Test
        @DisplayName("이메일이 존재하지 않으면 EMAIL_NOT_FOUND 예외가 발생한다")
        void emailNotFound_throws() {
            when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

            assertBusinessException(
                    () -> signInService.forgetId("anyPw", "none@test.com"),
                    ErrorCode.EMAIL_NOT_FOUND
            );
        }
    }

    @Nested
    @DisplayName("authenticate")
    class AuthenticateTest {

        @Test
        @DisplayName("비밀번호가 일치하면 유저를 반환한다")
        void pwMatches_returnsUser() {
            UserEntity user = activeUser();
            when(userRepository.findById("tester01")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("rawPw", user.getPw())).thenReturn(true);

            UserEntity result = signInService.authenticate("tester01", "rawPw");

            assertThat(result).isEqualTo(user);
        }

        @Test
        @DisplayName("비밀번호가 일치하지 않으면 INVALID_CREDENTIALS 예외가 발생한다")
        void pwMismatch_throws() {
            UserEntity user = activeUser();
            when(userRepository.findById("tester01")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongPw", user.getPw())).thenReturn(false);

            assertBusinessException(
                    () -> signInService.authenticate("tester01", "wrongPw"),
                    ErrorCode.INVALID_CREDENTIALS
            );
        }

        @Test
        @DisplayName("탈퇴한 회원이면 MEMBER_DELETED 예외가 발생한다")
        void deletedUser_throws() {
            UserEntity user = deletedUser();
            when(userRepository.findById("tester01")).thenReturn(Optional.of(user));

            assertBusinessException(
                    () -> signInService.authenticate("tester01", "rawPw"),
                    ErrorCode.MEMBER_DELETED
            );
        }
    }
}
