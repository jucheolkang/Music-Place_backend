package org.musicplace.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.musicplace.global.security.authorizaion.MemberAuthorizationUtil;
import org.musicplace.global.exception.ErrorCode;
import org.musicplace.global.exception.BusinessException;
import org.musicplace.global.security.config.CustomUserDetails;
import org.musicplace.user.domain.UserEntity;
import org.musicplace.user.dto.SignInGetUserDataDto;
import org.musicplace.user.dto.SignInSaveDto;
import org.musicplace.user.dto.SignInUpdateDto;
import org.musicplace.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SignInService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void save(SignInSaveDto signInSaveDto) {
        userRepository.save(UserEntity.builder()
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
    public void update(SignInUpdateDto signInUpdateDto) {
        String member_id = MemberAuthorizationUtil.getLoginMemberId();
        UserEntity userEntity = findById(member_id);
        CheckSignInDelete(userEntity);
        userEntity.updateProfile(
                signInUpdateDto.getName(),
                signInUpdateDto.getEmail(),
                signInUpdateDto.getNickname(),
                signInUpdateDto.getProfile_img_url());
    }

    @Transactional
    public void delete() {
        String member_id = MemberAuthorizationUtil.getLoginMemberId();
        UserEntity userEntity = findById(member_id);
        CheckSignInDelete(userEntity);
        userEntity.deleteAccount();
    }

    public SignInGetUserDataDto SignInGetUserData() {
        String member_id = MemberAuthorizationUtil.getLoginMemberId();
        UserEntity userEntity = findById(member_id);
        CheckSignInDelete(userEntity);
        return SignInGetUserDataDto.builder()
                .email(userEntity.getEmail())
                .profile_img_url(userEntity.getProfileImgUrl())
                .name(userEntity.getName())
                .nickname(userEntity.getNickname())
                .build();
    }



    public UserEntity findById(String member_id) {
        return userRepository.findById(member_id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ID_NOT_FOUND));
    }

    public boolean SignInCheckSameId(String memberId) {
        return !userRepository.existsByMemberId(memberId);
    }

    public void CheckSignInDelete(UserEntity userEntity) {
        if (userEntity.getDeleteAccount()) {
            throw new BusinessException(ErrorCode.MEMBER_DELETED);
        }
    }

    public String ForgetPw(String memberId, String email) {

        UserEntity user = userRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ID_NOT_FOUND));

        if (!user.getEmail().equals(email)) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_FOUND);
        }

        return user.getPw();
    }

    public String ForgetId(String pw, String email) {

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_NOT_FOUND));

        if (!passwordEncoder.matches(pw, user.getPw())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return user.getMemberId();
    }



    public CustomUserDetails authenticate(String memberId, String password) {

        UserEntity user = userRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ID_NOT_FOUND));

        CheckSignInDelete(user);

        if (!passwordEncoder.matches(password, user.getPw())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return new CustomUserDetails(user);
    }


}
