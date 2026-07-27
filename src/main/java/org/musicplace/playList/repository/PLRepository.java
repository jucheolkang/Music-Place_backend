package org.musicplace.playList.repository;

import org.musicplace.playList.domain.PLEntity;
import org.musicplace.playList.dto.ResponsePLDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PLRepository extends JpaRepository<PLEntity, Long> {

    /* ================== 기본 검증 ================== */

    boolean existsByPlaylistIdAndDeleteStateFalse(Long playlistId);

    /* ================== COUNT ================== */

    @Query(
            value = """
        SELECT COUNT(*)
        FROM PLAYLIST
        WHERE member_id = :memberId
          AND delete_state = false
        """,
            nativeQuery = true
    )
    Long countMyPlaylists(@Param("memberId") String memberId);

    @Query(
            value = """
        SELECT COUNT(*)
        FROM PLAYLIST
        WHERE member_id = :memberId
          AND onoff = 'Public'
          AND delete_state = false
        """,
            nativeQuery = true
    )
    Long countOtherPublicPlaylists(@Param("memberId") String memberId);

    /* ================== 목록 조회 ================== */

    /** 내 플레이리스트 (삭제 제외) */
    @Query("""
        select new org.musicplace.playList.dto.ResponsePLDto(
            p.playlistId,
            p.title,
            p.nickname,
            p.coverImg,
            p.onOff,
            p.comment,
            p.memberId
        )
        from PLEntity p
        where p.memberId = :memberId
        and p.deleteState = false
        order by p.playlistId desc
    """)
    List<ResponsePLDto> findMyPlaylists(
            @Param("memberId") String memberId
    );

    /** 다른 유저 공개 플레이리스트 */
    @Query("""
        select new org.musicplace.playList.dto.ResponsePLDto(
            p.playlistId,
            p.title,
            p.nickname,
            p.coverImg,
            p.onOff,
            p.comment,
            p.memberId
        )
        from PLEntity p
        where p.memberId = :memberId
        and p.onOff = org.musicplace.playList.domain.OnOff.Public
        and p.deleteState = false
        order by p.playlistId desc
    """)
    List<ResponsePLDto> findOtherUserPublicPlaylists(
            @Param("memberId") String memberId
    );

    /** 전체 공개 플레이리스트 */
    @Query("""
        select new org.musicplace.playList.dto.ResponsePLDto(
            p.playlistId,
            p.title,
            p.nickname,
            p.coverImg,
            p.onOff,
            p.comment,
            p.memberId
        )
        from PLEntity p
        where p.onOff = org.musicplace.playList.domain.OnOff.Public
        and p.deleteState = false
        order by p.playlistId desc
    """)
    List<ResponsePLDto> findAllPublicPlaylists();
}
