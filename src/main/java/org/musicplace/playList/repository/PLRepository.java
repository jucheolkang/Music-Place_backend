package org.musicplace.playList.repository;

import org.musicplace.playList.domain.PLEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PLRepository extends JpaRepository<PLEntity,Long> {
    Page<PLEntity> findByDeleteStateFalse(Pageable pageable);
}
