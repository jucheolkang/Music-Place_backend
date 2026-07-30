package org.musicplace.user.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.musicplace.common.config.BaseControllerTest;
import org.musicplace.global.exception.BusinessException;
import org.musicplace.global.exception.ErrorCode;
import org.musicplace.global.logging.LoggingMdcFilter;
import org.musicplace.global.security.jwt.JwtAuthenticationFilter;
import org.musicplace.user.service.SignInService;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.mockito.ArgumentCaptor;
import org.musicplace.user.domain.Gender;
import org.musicplace.user.dto.SignInSaveDto;
import org.musicplace.user.dto.SignInGetUserDataDto;


import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.ArgumentMatchers.any;

import org.musicplace.user.dto.SignInUpdateDto;
import org.springframework.http.MediaType;

@WebMvcTest(SignInController.class)
@AutoConfigureMockMvc(addFilters = false)
class SignInControllerTest extends BaseControllerTest {

    @MockBean
    private SignInService signInService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private LoggingMdcFilter loggingMdcFilter;


    @Test
    @DisplayName("회원가입 요청을 처리한다")
    void save() throws Exception {

        // given
        SignInSaveDto dto = SignInSaveDto.builder()
                .member_id("test-user")
                .pw("1234")
                .name("tester")
                .nickname("tester")
                .email("test@test.com")
                .gender(Gender.male)
                .build();

        // when
        mockMvc.perform(post("/sign_in/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        // then
        ArgumentCaptor<SignInSaveDto> captor =
                ArgumentCaptor.forClass(SignInSaveDto.class);

        verify(signInService).save(captor.capture());

        SignInSaveDto saved = captor.getValue();

        assertThat(saved.getMember_id()).isEqualTo("test-user");
        assertThat(saved.getPw()).isEqualTo("1234");
        assertThat(saved.getName()).isEqualTo("tester");
        assertThat(saved.getEmail()).isEqualTo("test@test.com");
        assertThat(saved.getNickname()).isEqualTo("tester");
        assertThat(saved.getGender()).isEqualTo(Gender.male);
    }

    @Test
    @DisplayName("회원 ID 중복 여부를 조회한다")
    void checkSameId() throws Exception {

        // given
        when(signInService.SignInCheckSameId("test-user"))
                .thenReturn(true);

        // when & then
        mockMvc.perform(get("/sign_in/test-user/sameid"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(signInService).SignInCheckSameId("test-user");
    }

    @Test
    @DisplayName("비밀번호 찾기를 요청한다")
    void forgetPw() throws Exception {

        // given
        when(signInService.ForgetPw("test-user", "test@test.com"))
                .thenReturn("encoded-password");

        // when & then
        mockMvc.perform(get("/sign_in/test-user/test@test.com/pw"))
                .andExpect(status().isOk())
                .andExpect(content().string("encoded-password"));

        verify(signInService)
                .ForgetPw("test-user", "test@test.com");
    }

    @Test
    @DisplayName("아이디 찾기를 요청한다")
    void forgetId() throws Exception {

        // given
        when(signInService.ForgetId("1234", "test@test.com"))
                .thenReturn("test-user");

        // when & then
        mockMvc.perform(get("/sign_in/1234/test@test.com/id"))
                .andExpect(status().isOk())
                .andExpect(content().string("test-user"));

        verify(signInService)
                .ForgetId("1234", "test@test.com");
    }

    @Test
    @DisplayName("회원 정보를 수정한다")
    void update() throws Exception {

        // given
        SignInUpdateDto dto = SignInUpdateDto.builder()
                .name("new-name")
                .email("new@test.com")
                .nickname("new-nickname")
                .profile_img_url("profile.png")
                .build();

        // when
        mockMvc.perform(patch("/sign_in/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        // then
        ArgumentCaptor<SignInUpdateDto> captor =
                ArgumentCaptor.forClass(SignInUpdateDto.class);

        verify(signInService).update(captor.capture());

        SignInUpdateDto request = captor.getValue();

        assertThat(request.getName()).isEqualTo("new-name");
        assertThat(request.getEmail()).isEqualTo("new@test.com");
        assertThat(request.getNickname()).isEqualTo("new-nickname");
        assertThat(request.getProfile_img_url()).isEqualTo("profile.png");
    }

    @Test
    @DisplayName("회원 탈퇴를 요청한다")
    void deleteUser() throws Exception {

        mockMvc.perform(delete("/sign_in/delete"))
                .andExpect(status().isOk());

        verify(signInService).delete();
    }

    @Test
    @DisplayName("회원 정보를 조회한다")
    void getUserData() throws Exception {

        // given
        SignInGetUserDataDto dto =
                SignInGetUserDataDto.builder()
                        .name("tester")
                        .email("test@test.com")
                        .nickname("tester")
                        .profile_img_url("profile.png")
                        .build();

        when(signInService.SignInGetUserData())
                .thenReturn(dto);

        // when & then
        mockMvc.perform(get("/sign_in/getuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("tester"))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.nickname").value("tester"))
                .andExpect(jsonPath("$.profile_img_url").value("profile.png"));

        verify(signInService).SignInGetUserData();
    }

    @Test
    @DisplayName("회원가입 중 BusinessException 발생 시 ErrorResponse를 반환한다")
    void save_BusinessException() throws Exception {

        // given
        SignInSaveDto dto = SignInSaveDto.builder()
                .member_id("test-user")
                .pw("1234")
                .name("tester")
                .email("test@test.com")
                .nickname("tester")
                .gender(Gender.male)
                .build();

        doThrow(new BusinessException(ErrorCode.ID_NOT_FOUND))
                .when(signInService)
                .save(any(SignInSaveDto.class));

        // when & then
        mockMvc.perform(post("/sign_in/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("ID_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("해당 사용자를 찾을 수 없습니다."))
                .andExpect(jsonPath("$.path").value("/sign_in/save"));
    }

    @Test
    @DisplayName("비밀번호 찾기 실패 시 ErrorResponse를 반환한다")
    void forgetPw_BusinessException() throws Exception {

        // given
        when(signInService.ForgetPw("user", "test@test.com"))
                .thenThrow(new BusinessException(ErrorCode.EMAIL_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/sign_in/user/test@test.com/pw"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("EMAIL_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("해당 이메일을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("아이디 찾기 실패 시 ErrorResponse를 반환한다")
    void forgetId_BusinessException() throws Exception {

        // given
        when(signInService.ForgetId("1234", "test@test.com"))
                .thenThrow(new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // when & then
        mockMvc.perform(get("/sign_in/1234/test@test.com/id"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    @DisplayName("회원 정보 수정 실패 시 ErrorResponse를 반환한다")
    void update_BusinessException() throws Exception {

        // given
        SignInUpdateDto dto = SignInUpdateDto.builder()
                .name("tester")
                .email("test@test.com")
                .nickname("tester")
                .profile_img_url("profile.png")
                .build();

        doThrow(new BusinessException(ErrorCode.MEMBER_DELETED))
                .when(signInService)
                .update(any(SignInUpdateDto.class));

        // when & then
        mockMvc.perform(patch("/sign_in/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MEMBER_DELETED"))
                .andExpect(jsonPath("$.message").value("탈퇴한 사용자입니다."));
    }

    @Test
    @DisplayName("회원 탈퇴 실패 시 ErrorResponse를 반환한다")
    void delete_BusinessException() throws Exception {

        // given
        doThrow(new BusinessException(ErrorCode.MEMBER_DELETED))
                .when(signInService)
                .delete();

        // when & then
        mockMvc.perform(delete("/sign_in/delete"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MEMBER_DELETED"))
                .andExpect(jsonPath("$.message").value("탈퇴한 사용자입니다."));
    }

    @Test
    @DisplayName("회원 정보 조회 실패 시 ErrorResponse를 반환한다")
    void getUserData_BusinessException() throws Exception {

        // given
        when(signInService.SignInGetUserData())
                .thenThrow(new BusinessException(ErrorCode.MEMBER_DELETED));

        // when & then
        mockMvc.perform(get("/sign_in/getuser"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MEMBER_DELETED"))
                .andExpect(jsonPath("$.message").value("탈퇴한 사용자입니다."));
    }


}
