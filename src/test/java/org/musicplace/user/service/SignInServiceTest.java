package org.musicplace.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.musicplace.common.config.BaseServiceTest;
import org.musicplace.global.exception.BusinessException;
import org.musicplace.global.exception.ErrorCode;
import org.musicplace.global.security.authorizaion.MemberAuthorizationUtil;
import org.musicplace.global.security.config.CustomUserDetails;
import org.musicplace.user.domain.Gender;
import org.musicplace.user.domain.UserEntity;
import org.musicplace.user.dto.SignInGetUserDataDto;
import org.musicplace.user.dto.SignInSaveDto;
import org.musicplace.user.dto.SignInUpdateDto;
import org.musicplace.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.MockedStatic;

import static org.mockito.Mockito.mockStatic;

public class SignInServiceTest extends BaseServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SignInService signInService;

    @Test
    @DisplayName("회원 ID로 회원을 조회한다")
    void findById() {
        // given
        UserEntity user = UserEntity.builder()
                .memberId("test-user")
                .pw("encoded-password")
                .name("tester")
                .nickname("tester")
                .email("test@test.com")
                .gender(Gender.male)
                .role("ROLE_USER")
                .build();

        when(userRepository.findById("test-user"))
                .thenReturn(Optional.of(user));

        // when
        UserEntity result = signInService.findById("test-user");

        // then
        assertThat(result).isEqualTo(user);

        verify(userRepository).findById("test-user");
    }

    @Test
    @DisplayName("존재하지 않는 회원 조회 시 예외가 발생한다")
    void findById_ThrowsException_WhenUserDoesNotExist() {

        // given
        when(userRepository.findById("unknown"))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> signInService.findById("unknown"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ID_NOT_FOUND);

        verify(userRepository).findById("unknown");
    }

    @Test
    @DisplayName("탈퇴하지 않은 회원은 예외가 발생하지 않는다")
    void checkSignInDelete() {

        // given
        UserEntity user = UserEntity.builder()
                .memberId("user")
                .pw("pw")
                .name("tester")
                .nickname("tester")
                .email("test@test.com")
                .gender(Gender.male)
                .role("ROLE_USER")
                .build();

        // when & then
        assertThatCode(() -> signInService.CheckSignInDelete(user))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("탈퇴한 회원이면 예외가 발생한다")
    void checkSignInDelete_ThrowsException_WhenDeletedUser() {

        // given
        UserEntity user = UserEntity.builder()
                .memberId("user")
                .pw("pw")
                .name("tester")
                .nickname("tester")
                .email("test@test.com")
                .gender(Gender.male)
                .role("ROLE_USER")
                .build();

        user.deleteAccount();

        // when & then
        assertThatThrownBy(() -> signInService.CheckSignInDelete(user))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MEMBER_DELETED);
    }

    @Test
    @DisplayName("회원가입 시 암호화된 비밀번호로 저장한다")
    void save() {

        // given
        SignInSaveDto dto = SignInSaveDto.builder()
                .member_id("test-user")
                .pw("1234")
                .name("tester")
                .nickname("tester")
                .email("test@test.com")
                .gender(Gender.male)
                .build();

        when(passwordEncoder.encode("1234"))
                .thenReturn("encoded-password");

        // when
        signInService.save(dto);

        // then
        ArgumentCaptor<UserEntity> captor =
                ArgumentCaptor.forClass(UserEntity.class);

        verify(userRepository).save(captor.capture());

        UserEntity savedUser = captor.getValue();

        assertThat(savedUser.getMemberId()).isEqualTo("test-user");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getName()).isEqualTo("tester");
        assertThat(savedUser.getEmail()).isEqualTo("test@test.com");
        assertThat(savedUser.getNickname()).isEqualTo("tester");
        assertThat(savedUser.getRole()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("사용 가능한 회원 ID이면 true를 반환한다")
    void SignInCheckSameId_ReturnTrue_WhenMemberIdDoesNotExist() {

        // given
        when(userRepository.existsByMemberId("new-user"))
                .thenReturn(false);

        // when
        boolean result = signInService.SignInCheckSameId("new-user");

        // then
        assertThat(result).isTrue();

        verify(userRepository).existsByMemberId("new-user");
    }

    @Test
    @DisplayName("이미 존재하는 회원 ID이면 false를 반환한다")
    void SignInCheckSameId_ReturnFalse_WhenMemberIdExists() {

        // given
        when(userRepository.existsByMemberId("test-user"))
                .thenReturn(true);

        // when
        boolean result = signInService.SignInCheckSameId("test-user");

        // then
        assertThat(result).isFalse();

        verify(userRepository).existsByMemberId("test-user");
    }

    @Test
    @DisplayName("회원 ID와 이메일이 일치하면 비밀번호를 반환한다")
    void ForgetPw() {

        // given
        UserEntity user = UserEntity.builder()
                .memberId("test-user")
                .pw("encoded-password")
                .email("test@test.com")
                .name("tester")
                .nickname("tester")
                .gender(Gender.male)
                .role("ROLE_USER")
                .build();

        when(userRepository.findById("test-user"))
                .thenReturn(Optional.of(user));

        // when
        String result = signInService.ForgetPw(
                "test-user",
                "test@test.com"
        );

        // then
        assertThat(result).isEqualTo("encoded-password");

        verify(userRepository).findById("test-user");
    }

    @Test
    @DisplayName("회원이 존재하지 않으면 예외가 발생한다")
    void ForgetPw_ThrowsException_WhenMemberDoesNotExist() {

        // given
        when(userRepository.findById("unknown"))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                signInService.ForgetPw("unknown", "test@test.com"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ID_NOT_FOUND);
    }

    @Test
    @DisplayName("이메일이 일치하지 않으면 예외가 발생한다")
    void ForgetPw_ThrowsException_WhenEmailDoesNotMatch() {

        // given
        UserEntity user = UserEntity.builder()
                .memberId("test-user")
                .pw("encoded")
                .email("test@test.com")
                .name("tester")
                .nickname("tester")
                .gender(Gender.male)
                .role("ROLE_USER")
                .build();

        when(userRepository.findById("test-user"))
                .thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() ->
                signInService.ForgetPw("test-user", "wrong@test.com"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_NOT_FOUND);
    }

    @Test
    @DisplayName("비밀번호와 이메일이 일치하면 회원 ID를 반환한다")
    void ForgetId() {

        // given
        UserEntity user = UserEntity.builder()
                .memberId("test-user")
                .pw("encoded-password")
                .email("test@test.com")
                .name("tester")
                .nickname("tester")
                .gender(Gender.male)
                .role("ROLE_USER")
                .build();

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("1234", "encoded-password"))
                .thenReturn(true);

        // when
        String result =
                signInService.ForgetId("1234", "test@test.com");

        // then
        assertThat(result).isEqualTo("test-user");

        verify(userRepository).findByEmail("test@test.com");
    }

    @Test
    @DisplayName("이메일이 존재하지 않으면 예외가 발생한다")
    void ForgetId_ThrowsException_WhenEmailDoesNotExist() {

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                signInService.ForgetId("1234", "test@test.com"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_NOT_FOUND);
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 예외가 발생한다")
    void ForgetId_ThrowsException_WhenPasswordDoesNotMatch() {

        UserEntity user = UserEntity.builder()
                .memberId("test-user")
                .pw("encoded-password")
                .email("test@test.com")
                .name("tester")
                .nickname("tester")
                .gender(Gender.male)
                .role("ROLE_USER")
                .build();

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("1234", "encoded-password"))
                .thenReturn(false);

        assertThatThrownBy(() ->
                signInService.ForgetId("1234", "test@test.com"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("로그인에 성공하면 CustomUserDetails를 반환한다")
    void authenticate() {

        UserEntity user = UserEntity.builder()
                .memberId("test-user")
                .pw("encoded-password")
                .email("test@test.com")
                .name("tester")
                .nickname("tester")
                .gender(Gender.male)
                .role("ROLE_USER")
                .build();

        when(userRepository.findById("test-user"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("1234", "encoded-password"))
                .thenReturn(true);

        CustomUserDetails result =
                signInService.authenticate("test-user", "1234");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("test-user");
    }

    @Test
    @DisplayName("존재하지 않는 회원이면 예외가 발생한다")
    void authenticate_ThrowsException_WhenMemberDoesNotExist() {

        when(userRepository.findById("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                signInService.authenticate("unknown", "1234"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ID_NOT_FOUND);
    }

    @Test
    @DisplayName("탈퇴한 회원은 로그인할 수 없다")
    void authenticate_ThrowsException_WhenDeletedMember() {

        UserEntity user = UserEntity.builder()
                .memberId("test-user")
                .pw("encoded-password")
                .email("test@test.com")
                .name("tester")
                .nickname("tester")
                .gender(Gender.male)
                .role("ROLE_USER")
                .build();

        user.deleteAccount();

        when(userRepository.findById("test-user"))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                signInService.authenticate("test-user", "1234"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MEMBER_DELETED);
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 로그인에 실패한다")
    void authenticate_ThrowsException_WhenPasswordDoesNotMatch() {

        UserEntity user = UserEntity.builder()
                .memberId("test-user")
                .pw("encoded-password")
                .email("test@test.com")
                .name("tester")
                .nickname("tester")
                .gender(Gender.male)
                .role("ROLE_USER")
                .build();

        when(userRepository.findById("test-user"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("1234", "encoded-password"))
                .thenReturn(false);

        assertThatThrownBy(() ->
                signInService.authenticate("test-user", "1234"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("회원 정보를 수정한다")
    void update() {

        // given
        SignInUpdateDto dto = SignInUpdateDto.builder()
                .name("new-name")
                .email("new@test.com")
                .nickname("new-nickname")
                .profile_img_url("profile.png")
                .build();

        UserEntity user = UserEntity.builder()
                .memberId("test-user")
                .pw("encoded-password")
                .name("tester")
                .nickname("tester")
                .email("test@test.com")
                .gender(Gender.male)
                .role("ROLE_USER")
                .build();

        try (MockedStatic<MemberAuthorizationUtil> mocked =
                     mockStatic(MemberAuthorizationUtil.class)) {

            mocked.when(MemberAuthorizationUtil::getLoginMemberId)
                    .thenReturn("test-user");

            when(userRepository.findById("test-user"))
                    .thenReturn(Optional.of(user));

            // when
            signInService.update(dto);

            // then
            assertThat(user.getName()).isEqualTo("new-name");
            assertThat(user.getEmail()).isEqualTo("new@test.com");
            assertThat(user.getNickname()).isEqualTo("new-nickname");
            assertThat(user.getProfileImgUrl()).isEqualTo("profile.png");

            verify(userRepository).findById("test-user");
        }
    }

    @Test
    @DisplayName("수정 대상 회원이 존재하지 않으면 예외가 발생한다")
    void update_ThrowsException_WhenMemberDoesNotExist() {

        SignInUpdateDto dto = SignInUpdateDto.builder().build();

        try (MockedStatic<MemberAuthorizationUtil> mocked =
                     mockStatic(MemberAuthorizationUtil.class)) {

            mocked.when(MemberAuthorizationUtil::getLoginMemberId)
                    .thenReturn("unknown");

            when(userRepository.findById("unknown"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> signInService.update(dto))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.ID_NOT_FOUND);
        }
    }

    @Test
    @DisplayName("탈퇴한 회원은 정보를 수정할 수 없다")
    void update_ThrowsException_WhenDeletedUser() {

        UserEntity user = UserEntity.builder()
                .memberId("test-user")
                .pw("pw")
                .name("tester")
                .nickname("tester")
                .email("test@test.com")
                .gender(Gender.male)
                .role("ROLE_USER")
                .build();

        user.deleteAccount();

        try (MockedStatic<MemberAuthorizationUtil> mocked =
                     mockStatic(MemberAuthorizationUtil.class)) {

            mocked.when(MemberAuthorizationUtil::getLoginMemberId)
                    .thenReturn("test-user");

            when(userRepository.findById("test-user"))
                    .thenReturn(Optional.of(user));

            assertThatThrownBy(() ->
                    signInService.update(SignInUpdateDto.builder().build()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.MEMBER_DELETED);
        }
    }

    @Test
    @DisplayName("회원을 탈퇴 처리한다")
    void delete() {

        UserEntity user = UserEntity.builder()
                .memberId("test-user")
                .pw("pw")
                .name("tester")
                .nickname("tester")
                .email("test@test.com")
                .gender(Gender.male)
                .role("ROLE_USER")
                .build();

        try (MockedStatic<MemberAuthorizationUtil> mocked =
                     mockStatic(MemberAuthorizationUtil.class)) {

            mocked.when(MemberAuthorizationUtil::getLoginMemberId)
                    .thenReturn("test-user");

            when(userRepository.findById("test-user"))
                    .thenReturn(Optional.of(user));

            // when
            signInService.delete();

            // then
            assertThat(user.getDeleteAccount()).isTrue();
        }
    }

    @Test
    @DisplayName("탈퇴 대상 회원이 존재하지 않으면 예외가 발생한다")
    void delete_ThrowsException_WhenMemberDoesNotExist() {

        try (MockedStatic<MemberAuthorizationUtil> mocked =
                     mockStatic(MemberAuthorizationUtil.class)) {

            mocked.when(MemberAuthorizationUtil::getLoginMemberId)
                    .thenReturn("unknown");

            when(userRepository.findById("unknown"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> signInService.delete())
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.ID_NOT_FOUND);
        }
    }

    @Test
    @DisplayName("이미 탈퇴한 회원은 다시 탈퇴할 수 없다")
    void delete_ThrowsException_WhenDeletedUser() {

        UserEntity user = UserEntity.builder()
                .memberId("test-user")
                .pw("pw")
                .name("tester")
                .nickname("tester")
                .email("test@test.com")
                .gender(Gender.male)
                .role("ROLE_USER")
                .build();

        user.deleteAccount();

        try (MockedStatic<MemberAuthorizationUtil> mocked =
                     mockStatic(MemberAuthorizationUtil.class)) {

            mocked.when(MemberAuthorizationUtil::getLoginMemberId)
                    .thenReturn("test-user");

            when(userRepository.findById("test-user"))
                    .thenReturn(Optional.of(user));

            assertThatThrownBy(() -> signInService.delete())
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.MEMBER_DELETED);
        }
    }

    @Test
    @DisplayName("회원 정보를 조회한다")
    void SignInGetUserData() {

        UserEntity user = UserEntity.builder()
                .memberId("test-user")
                .pw("pw")
                .name("tester")
                .nickname("nickname")
                .email("test@test.com")
                .gender(Gender.male)
                .role("ROLE_USER")
                .build();

        user.updateProfile(
                "tester",
                "test@test.com",
                "nickname",
                "profile.png"
        );

        try (MockedStatic<MemberAuthorizationUtil> mocked =
                     mockStatic(MemberAuthorizationUtil.class)) {

            mocked.when(MemberAuthorizationUtil::getLoginMemberId)
                    .thenReturn("test-user");

            when(userRepository.findById("test-user"))
                    .thenReturn(Optional.of(user));

            // when
            SignInGetUserDataDto result =
                    signInService.SignInGetUserData();

            // then
            assertThat(result.getName()).isEqualTo("tester");
            assertThat(result.getEmail()).isEqualTo("test@test.com");
            assertThat(result.getNickname()).isEqualTo("nickname");
            assertThat(result.getProfile_img_url()).isEqualTo("profile.png");
        }
    }

    @Test
    @DisplayName("조회 대상 회원이 존재하지 않으면 예외가 발생한다")
    void SignInGetUserData_ThrowsException_WhenMemberDoesNotExist() {

        try (MockedStatic<MemberAuthorizationUtil> mocked =
                     mockStatic(MemberAuthorizationUtil.class)) {

            mocked.when(MemberAuthorizationUtil::getLoginMemberId)
                    .thenReturn("unknown");

            when(userRepository.findById("unknown"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    signInService.SignInGetUserData())
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.ID_NOT_FOUND);
        }
    }

    @Test
    @DisplayName("탈퇴한 회원은 정보를 조회할 수 없다")
    void SignInGetUserData_ThrowsException_WhenDeletedUser() {

        UserEntity user = UserEntity.builder()
                .memberId("test-user")
                .pw("pw")
                .name("tester")
                .nickname("tester")
                .email("test@test.com")
                .gender(Gender.male)
                .role("ROLE_USER")
                .build();

        user.deleteAccount();

        try (MockedStatic<MemberAuthorizationUtil> mocked =
                     mockStatic(MemberAuthorizationUtil.class)) {

            mocked.when(MemberAuthorizationUtil::getLoginMemberId)
                    .thenReturn("test-user");

            when(userRepository.findById("test-user"))
                    .thenReturn(Optional.of(user));

            assertThatThrownBy(() ->
                    signInService.SignInGetUserData())
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.MEMBER_DELETED);
        }
    }


}
