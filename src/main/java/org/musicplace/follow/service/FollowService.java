package org.musicplace.follow.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.musicplace.follow.domain.FollowEntity;
import org.musicplace.follow.dto.FollowResponseDto;
import org.musicplace.follow.dto.FollowSaveDto;
import org.musicplace.follow.kafka.event.FollowChangedEvent;
import org.musicplace.follow.repository.FollowRepository;
import org.musicplace.global.exception.BusinessException;
import org.musicplace.global.exception.ErrorCode;
import org.musicplace.user.domain.UserEntity;
import org.musicplace.user.service.SignInService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final SignInService signInService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public Long followSave(String memberId, FollowSaveDto dto) {

        UserEntity user = signInService.findById(memberId);
        signInService.checkSignInDelete(user);

        if (memberId.equals(dto.getTarget_id())) {
            throw new BusinessException(ErrorCode.CANNOT_FOLLOW_SELF);
        }

        if (followRepository.existsByMemberIdAndTargetId(
                memberId,
                dto.getTarget_id())) {

            throw new BusinessException(ErrorCode.FOLLOW_ALREADY_EXISTS);
        }

        FollowEntity follow = FollowEntity.builder()
                .memberId(memberId)
                .targetId(dto.getTarget_id())
                .targetNickname(dto.getNickname())
                .targetProfileImgUrl(dto.getProfile_img_url())
                .build();

        try {
            followRepository.save(follow);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.FOLLOW_ALREADY_EXISTS);
        }

        applicationEventPublisher.publishEvent(
                new FollowChangedEvent(dto.getTarget_id(), memberId));

        return follow.getFollowId();
    }

    @Transactional
    public void followDelete(String memberId, Long followId) {

        FollowEntity follow = followRepository.findById(followId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.FOLLOW_NOT_FOUND));

        if (!follow.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FOLLOW_NOT_FOUND);
        }

        followRepository.delete(follow);

        applicationEventPublisher.publishEvent(
                new FollowChangedEvent(follow.getTargetId(), memberId));
    }

    public List<FollowResponseDto> followFindAll(String memberId) {

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

    public long followCount(String memberId) {
        return followRepository.countByMemberId(memberId);
    }

    public long otherFollowCount(String otherMemberId) {
        return followRepository.countByTargetId(otherMemberId);
    }
}
