package org.musicplace.follow.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.musicplace.follow.domain.FollowEntity;
import org.musicplace.follow.dto.FollowSaveDto;
import org.musicplace.follow.dto.FollowResponseDto;
import org.musicplace.follow.repository.FollowRepository;
import org.musicplace.global.exception.ErrorCode;
import org.musicplace.global.exception.ExceptionHandler;
import org.musicplace.global.security.authorizaion.MemberAuthorizationUtil;
import org.musicplace.user.domain.UserEntity;
import org.musicplace.user.service.SignInService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final SignInService signInService;

    @Transactional
    public Long followSave(FollowSaveDto dto) {
        String memberId = MemberAuthorizationUtil.getLoginMemberId();

        UserEntity user = signInService.SignInFindById(memberId);
        signInService.CheckSignInDelete(user);

        if (memberId.equals(dto.getTarget_id())) {
            throw new ExceptionHandler(ErrorCode.NOT_FOLLOW_SELF);
        }

        // 🔥 중복 팔로우 방지 (DB + 조회)
        if (followRepository.existsByMemberIdAndTargetId(memberId, dto.getTarget_id())) {
            throw new ExceptionHandler(ErrorCode.FOLLOW_SAME_ID);
        }

        FollowEntity follow = FollowEntity.builder()
                .memberId(memberId)
                .targetId(dto.getTarget_id())
                .targetNickname(dto.getNickname())
                .targetProfileImgUrl(dto.getProfile_img_url())
                .build();

        followRepository.save(follow);
        return follow.getFollowId();
    }

    @Transactional
    public void followDelete(Long followId) {
        String memberId = MemberAuthorizationUtil.getLoginMemberId();

        FollowEntity follow = followRepository.findById(followId)
                .orElseThrow(() -> new ExceptionHandler(ErrorCode.FOLLOW_NOT_FOUND));

        if (!follow.getMemberId().equals(memberId)) {
            throw new ExceptionHandler(ErrorCode.FOLLOW_NO_ID);
        }

        followRepository.delete(follow);
    }

    public List<FollowResponseDto> followFindAll() {
        String memberId = MemberAuthorizationUtil.getLoginMemberId();

        return followRepository.findAllByMemberId(memberId)
                .stream()
                .map(f -> FollowResponseDto.builder()
                        .follow_id(f.getFollowId())
                        .target_id(f.getTargetId())
                        .nickname(f.getTargetNickname())
                        .profile_img_url(f.getTargetProfileImgUrl())
                        .build())
                .toList();
    }

    public long followCount() {
        String memberId = MemberAuthorizationUtil.getLoginMemberId();
        return followRepository.countByMemberId(memberId);
    }

    public long otherFollowCount(String otherMemberId) {
        return followRepository.countByTargetId(otherMemberId);
    }
}
