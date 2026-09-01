package org.musicplace.follow.service;

import lombok.RequiredArgsConstructor;
import org.musicplace.follow.domain.FollowEntity;
import org.musicplace.follow.repository.FollowRepository;
import org.musicplace.user.domain.UserEntity;
import org.musicplace.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowCountTransactionalOps {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public void followWithPessimisticLock(String memberId, String targetId) {
        UserEntity target = userRepository.findByMemberIdForUpdate(targetId)
                .orElseThrow(() -> new IllegalArgumentException("target not found: " + targetId));

        followRepository.save(FollowEntity.builder()
                .memberId(memberId)
                .targetId(targetId)
                .targetNickname(target.getNickname())
                .targetProfileImgUrl(target.getProfileImgUrl())
                .build());

        target.increaseFollowerCount();
    }

    @Transactional
    public void followWithOptimisticLockAttempt(String memberId, String targetId) {
        UserEntity target = userRepository.findByMemberId(targetId)
                .orElseThrow(() -> new IllegalArgumentException("target not found: " + targetId));

        followRepository.save(FollowEntity.builder()
                .memberId(memberId)
                .targetId(targetId)
                .targetNickname(target.getNickname())
                .targetProfileImgUrl(target.getProfileImgUrl())
                .build());

        target.increaseFollowerCount();
    }

    @Transactional
    public void followWithBatchStrategy(String memberId, String targetId) {
        UserEntity target = userRepository.findByMemberId(targetId)
                .orElseThrow(() -> new IllegalArgumentException("target not found: " + targetId));

        followRepository.save(FollowEntity.builder()
                .memberId(memberId)
                .targetId(targetId)
                .targetNickname(target.getNickname())
                .targetProfileImgUrl(target.getProfileImgUrl())
                .build());
        // followerCount는 건드리지 않음 - 나중에 배치가 재계산
    }

    @Transactional
    public void recalculateFollowerCount(String targetId) {
        userRepository.recalculateFollowerCount(targetId);
    }

    @Transactional
    public void resetForTest(String targetId) {
        followRepository.deleteAllByTargetId(targetId);
        userRepository.resetFollowerCount(targetId, 0L);
    }
}
