package org.musicplace.follow.repository;

import org.musicplace.follow.domain.FollowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FollowRepository extends JpaRepository<FollowEntity, Long> {

    boolean existsByMemberIdAndTargetId(String memberId, String targetId);

    List<FollowEntity> findAllByMemberId(String memberId);

    long countByMemberId(String memberId);

    long countByTargetId(String targetId);

    // follow 실험 정리용 메서드
    @Modifying
    @Query("delete from FollowEntity f where f.targetId = :targetId")
    void deleteAllByTargetId(@Param("targetId") String targetId);
}
