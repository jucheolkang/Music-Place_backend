package org.musicplace.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.musicplace.global.exception.BusinessException;
import org.musicplace.global.exception.ErrorCode;
import org.musicplace.user.domain.UserEntity;
import org.musicplace.user.dto.SignInGetUserDataDto;
import org.musicplace.user.dto.SignInSaveDto;
import org.musicplace.user.dto.SignInUpdateDto;
import org.musicplace.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignInService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void save(SignInSaveDto signInSaveDto) {

        userRepository.save(
                UserEntity.builder()
                        .memberId(signInSaveDto.getMember_id())
                        .pw(passwordEncoder.encode(signInSaveDto.getPw()))
                        .gender(signInSaveDto.getGender())
                        .email(signInSaveDto.getEmail())
                        .nickname(signInSaveDto.getNickname())
                        .name(signInSaveDto.getName())
                        .role("ROLE_USER")
                        .build()
        );
    }

    @Transactional
    public void update(String memberId, SignInUpdateDto signInUpdateDto) {

        UserEntity userEntity = findById(memberId);

        checkSignInDelete(userEntity);

        userEntity.updateProfile(
                signInUpdateDto.getName(),
                signInUpdateDto.getEmail(),
                signInUpdateDto.getNickname(),
                signInUpdateDto.getProfile_img_url()
        );
    }

    @Transactional
    public void delete(String memberId) {

        UserEntity userEntity = findById(memberId);

        checkSignInDelete(userEntity);

        userEntity.deleteAccount();
    }

    public SignInGetUserDataDto getUserData(String memberId) {

        UserEntity userEntity = findById(memberId);

        checkSignInDelete(userEntity);

        return SignInGetUserDataDto.builder()
                .email(userEntity.getEmail())
                .profile_img_url(userEntity.getProfileImgUrl())
                .name(userEntity.getName())
                .nickname(userEntity.getNickname())
                .build();
    }

    public UserEntity findById(String memberId) {

        return userRepository.findById(memberId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.ID_NOT_FOUND));
    }

    public boolean signInCheckSameId(String memberId) {

        return !userRepository.existsByMemberId(memberId);
    }

    public void checkSignInDelete(UserEntity userEntity) {

        if (Boolean.TRUE.equals(userEntity.getDeleteAccount())) {
            throw new BusinessException(ErrorCode.MEMBER_DELETED);
        }
    }

/*    public String forgetPw(String memberId, String email) {

        UserEntity user = userRepository.findById(memberId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.ID_NOT_FOUND));

        if (!user.getEmail().equals(email)) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_FOUND);
        }

        return user.getPw();
    }*/

    public String forgetId(String pw, String email) {

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.EMAIL_NOT_FOUND));

        if (!passwordEncoder.matches(pw, user.getPw())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return user.getMemberId();
    }



    @Transactional
    public UserEntity authenticate(String memberId, String password) {

        UserEntity user = findById(memberId);

        checkSignInDelete(user);

        if (!passwordEncoder.matches(password, user.getPw())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return user;
    }


}
