package org.musicplace.user.repository;

import org.musicplace.user.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignInRepository extends JpaRepository<UserEntity,String> {
    UserEntity findByMemberId(String memberId);
}
