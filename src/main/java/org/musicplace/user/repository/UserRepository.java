package org.musicplace.user.repository;

import jakarta.persistence.LockModeType;
import org.musicplace.user.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {

    Optional<UserEntity> findByMemberId(String memberId);

    boolean existsByMemberId(String memberId);

    Optional<UserEntity> findByEmail(String email);


    // 비관적 락: 다른 트랜잭션이 이 row를 건드리지 못하게 SELECT ... FOR UPDATE
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserEntity u where u.memberId = :memberId")
    Optional<UserEntity> findByMemberIdForUpdate(@Param("memberId") String memberId);

    // 배치 방식: 특정 유저의 팔로워 수를 실제 follow 테이블 기준으로 재계산
    @Modifying
    @Query("update UserEntity u set u.followerCount = " +
            "(select count(f) from FollowEntity f where f.targetId = u.memberId) " +
            "where u.memberId = :memberId")
    void recalculateFollowerCount(@Param("memberId") String memberId);

    // 배치 방식: 전체 유저 대상 일괄 재계산 (스케줄러용)
    @Modifying
    @Query("update UserEntity u set u.followerCount = " +
            "(select count(f) from FollowEntity f where f.targetId = u.memberId)")
    void recalculateAllFollowerCounts();

    // follow 실험 정리용 메서드
    @Modifying
    @Query("update UserEntity u set u.followerCount = :count where u.memberId = :memberId")
    void resetFollowerCount(@Param("memberId") String memberId, @Param("count") Long count);
}
