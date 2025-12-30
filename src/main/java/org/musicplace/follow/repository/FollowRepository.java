package org.musicplace.follow.repository;

import org.musicplace.follow.domain.FollowEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowRepository extends JpaRepository<FollowEntity, Long> {

    boolean existsByMemberIdAndTargetId(String memberId, String targetId);

    List<FollowEntity> findAllByMemberId(String memberId);

    long countByMemberId(String memberId);

    long countByTargetId(String targetId);
}
