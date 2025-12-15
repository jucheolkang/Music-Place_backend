package org.musicplace.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.musicplace.global.security.authorizaion.MemberAuthorizationUtil;
import org.musicplace.global.exception.ErrorCode;
import org.musicplace.global.exception.ExceptionHandler;
import org.musicplace.global.security.config.CustomUserDetails;
import org.musicplace.user.domain.UserEntity;
import org.musicplace.user.dto.SignInGetUserDataDto;
import org.musicplace.user.dto.SignInSaveDto;
import org.musicplace.user.dto.SignInUpdateDto;
import org.musicplace.user.repository.SignInRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SignInService {
    private final SignInRepository signInRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void SignInSave(SignInSaveDto signInSaveDto) {
        signInRepository.save(UserEntity.builder()
                .memberId(signInSaveDto.getMember_id())
                .pw(passwordEncoder.encode(signInSaveDto.getPw()))
                .gender(signInSaveDto.getGender())
                .email(signInSaveDto.getEmail())
                .nickname(signInSaveDto.getNickname())
                .name(signInSaveDto.getName())
                .role("ROLE_USER")
                .build());
    }

    @Transactional
    public void SignInUpdate(SignInUpdateDto signInUpdateDto) {
        String member_id = MemberAuthorizationUtil.getLoginMemberId();
        UserEntity userEntity = SignInFindById(member_id);
        CheckSignInDelete(userEntity);
        userEntity.updateProfile(
                signInUpdateDto.getName(),
                signInUpdateDto.getEmail(),
                signInUpdateDto.getNickname(),
                signInUpdateDto.getProfile_img_url());
    }

    @Transactional
    public void SignInDelete() {
        String member_id = MemberAuthorizationUtil.getLoginMemberId();
        UserEntity userEntity = SignInFindById(member_id);
        CheckSignInDelete(userEntity);
        userEntity.deleteAccount();
    }

    public SignInGetUserDataDto SignInGetUserData() {
        String member_id = MemberAuthorizationUtil.getLoginMemberId();
        UserEntity userEntity = SignInFindById(member_id);
        CheckSignInDelete(userEntity);
        return SignInGetUserDataDto.builder()
                .email(userEntity.getEmail())
                .profile_img_url(userEntity.getProfileImgUrl())
                .name(userEntity.getName())
                .nickname(userEntity.getNickname())
                .build();
    }



    public UserEntity SignInFindById(String member_id) {
        return signInRepository.findById(member_id)
                .orElseThrow(() -> new ExceptionHandler(ErrorCode.ID_NOT_FOUND));
    }

    public Boolean SignInCheckSameId(String member_id) {
        List<UserEntity> userEntityList = signInRepository.findAll();
        for (UserEntity getListUser : userEntityList) {
            if(getListUser.getMemberId().equals(member_id)) {
                return false;
            }
        }
        return true;
    }

    public void CheckSignInDelete(UserEntity userEntity) {
        if (userEntity.getDeleteAccount()) {
            throw new ExceptionHandler(ErrorCode.ID_DELETE);
        }
    }

    public String ForgetPw(String member_id, String email) {
        UserEntity userEntity = signInRepository.findById(member_id)
                .orElseThrow(() -> new ExceptionHandler(ErrorCode.ID_NOT_FOUND));
        if(userEntity.getEmail().equals(email)) {
            return userEntity.getPw();
        }
        return ErrorCode.EMAIL_NOT_FOUND.toString();
    }

    public String ForgetId(String pw, String email) {
        List<UserEntity> userEntityList = signInRepository.findAll();
        String result = null;
        for(UserEntity n : userEntityList) {
            if(n.getPw().equals(pw) && n.getEmail().equals(email)) {
                result = n.getMemberId();
            }
        }
        return result;
    }



    public CustomUserDetails authenticate(String id, String password) {
        UserEntity user = signInRepository.findById(id)
                .orElseThrow(() -> new ExceptionHandler(ErrorCode.ID_NOT_FOUND));
        if (user.getDeleteAccount()) {
            throw new ExceptionHandler(ErrorCode.ID_DELETE);
        }
        if (passwordEncoder.matches(password, user.getPw())) {
            return new CustomUserDetails(user);
        }
        throw new ExceptionHandler(ErrorCode.INVALID_CREDENTIALS);
    }


}
