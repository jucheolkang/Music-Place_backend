package org.musicplace.follow.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.musicplace.follow.domain.FollowEntity;
import org.musicplace.follow.dto.FollowSaveDto;
import org.musicplace.follow.dto.FollowResponseDto;
import org.musicplace.follow.repository.FollowRepository;
import org.musicplace.global.security.authorizaion.MemberAuthorizationUtil;
import org.musicplace.global.exception.ErrorCode;
import org.musicplace.global.exception.ExceptionHandler;
import org.musicplace.user.domain.UserEntity;
import org.musicplace.user.service.SignInService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final SignInService signInService;

    @Transactional
    public Long FollowSave(FollowSaveDto followSaveDto) {
        String member_id = MemberAuthorizationUtil.getLoginMemberId();
        UserEntity userEntity = signInService.SignInFindById(member_id);
        signInService.CheckSignInDelete(userEntity);
        FollowCheck(followSaveDto.getTarget_id(), userEntity);
        FollowEntity followEntity = FollowEntity.builder()
                .target_id(followSaveDto.getTarget_id())
                .nickname(followSaveDto.getNickname())
                .profile_img_url(followSaveDto.getProfile_img_url())
                .build();
        userEntity.getFollowEntities().add(followEntity);
        followEntity.SignInEntity(userEntity);
        followRepository.save(followEntity);
        return followEntity.getFollow_id();
    }

    @Transactional
    public void followDelete(Long follow_id) {
        String member_id = MemberAuthorizationUtil.getLoginMemberId();
        UserEntity userEntity = signInService.SignInFindById(member_id);
        FollowEntity followEntity = followFindById(follow_id);
        if (userEntity.getFollowEntities().contains(followEntity)) {
            userEntity.getFollowEntities().remove(followEntity);
            followRepository.delete(followEntity);
        } else {
            throw new ExceptionHandler(ErrorCode.FOLLOW_NO_ID);
        }
    }

    public List<FollowResponseDto> followFindAll() {
        String member_id = MemberAuthorizationUtil.getLoginMemberId();
        UserEntity userEntity = signInService.SignInFindById(member_id);
        List<FollowEntity> followEntities = userEntity.getFollowEntities();
        List<FollowResponseDto> followResponseDtos = followEntities.stream()
                .map(followEntity -> FollowResponseDto.builder()
                        .follow_id(followEntity.getFollow_id())
                        .target_id(followEntity.getTarget_id())
                        .nickname(followEntity.getNickname())
                        .profile_img_url(followEntity.getProfile_img_url())
                        .build())
                .collect(Collectors.toList());
        return followResponseDtos;
    }

    public Long followCount() {
        String member_id = MemberAuthorizationUtil.getLoginMemberId();
        UserEntity userEntity = signInService.SignInFindById(member_id);
        return userEntity.getFollowEntities().stream().count();
    }

    public Long otherFollowCount(String otherMemberId) {
        UserEntity userEntity = signInService.SignInFindById(otherMemberId);
        return userEntity.getFollowEntities().stream().count();
    }

    public FollowEntity followFindById(Long target_id) {
        FollowEntity followEntity = followRepository.findById(target_id)
                .orElseThrow(() -> new ExceptionHandler(ErrorCode.FOLLOW_NOT_FOUND));
        return followEntity;
    }

    public void FollowCheck(String targetId, UserEntity userEntity) {
        List<FollowEntity> followEntities = userEntity.getFollowEntities();

        for (FollowEntity target : followEntities) {
            if (target.getTarget_id().equals(targetId)) {
                throw new ExceptionHandler(ErrorCode.FOLLOW_SAME_ID);
            }
        }

        if (userEntity.getMemberId().equals(targetId)) {
            throw new ExceptionHandler(ErrorCode.NOT_FOLLOW_SELF);
        }
    }


}
