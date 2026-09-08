package org.musicplace.playList.repository;

import org.musicplace.playList.domain.PLEntity;
import org.musicplace.playList.dto.PlaylistSearchProjection;
import org.musicplace.playList.dto.ResponsePLDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    Page<ResponsePLDto> findAllPublicPlaylists(Pageable pageable);

    @Query(
            value = """
        SELECT
            p.playlist_id AS playlistId,
            p.title       AS title,
            p.nickname    AS nickname,
            p.cover_img   AS coverImg,
            p.onoff       AS onoff,
            p.comment     AS comment,
            p.member_id   AS memberId,
            MATCH(p.title, p.comment, p.search_text) AGAINST (:keyword IN NATURAL LANGUAGE MODE) AS relevanceScore
        FROM PLAYLIST p
        WHERE p.delete_state = false
          AND p.onoff = 'Public'
          AND MATCH(p.title, p.comment, p.search_text) AGAINST (:keyword IN NATURAL LANGUAGE MODE) > 0
        ORDER BY relevanceScore DESC, p.playlist_id DESC
        """,
            countQuery = """
        SELECT COUNT(*) FROM PLAYLIST p
        WHERE p.delete_state = false
          AND p.onoff = 'Public'
          AND MATCH(p.title, p.comment, p.search_text) AGAINST (:keyword IN NATURAL LANGUAGE MODE) > 0
        """,
            nativeQuery = true
    )
    Page<PlaylistSearchProjection> searchByRelevance(@Param("keyword") String keyword, Pageable pageable);

    @Query(
            value = """
        SELECT
            p.playlist_id AS playlistId,
            p.title       AS title,
            p.nickname    AS nickname,
            p.cover_img   AS coverImg,
            p.onoff       AS onoff,
            p.comment     AS comment,
            p.member_id   AS memberId,
            MATCH(p.title, p.comment, p.search_text) AGAINST (:keyword IN NATURAL LANGUAGE MODE) AS relevanceScore
        FROM PLAYLIST p
        WHERE p.delete_state = false
          AND p.onoff = 'Public'
          AND MATCH(p.title, p.comment, p.search_text) AGAINST (:keyword IN NATURAL LANGUAGE MODE) > 0
        ORDER BY p.register_date DESC, p.playlist_id DESC
        """,
            countQuery = """
        SELECT COUNT(*) FROM PLAYLIST p
        WHERE p.delete_state = false
          AND p.onoff = 'Public'
          AND MATCH(p.title, p.comment, p.search_text) AGAINST (:keyword IN NATURAL LANGUAGE MODE) > 0
        """,
            nativeQuery = true
    )
    Page<PlaylistSearchProjection> searchByLatest(@Param("keyword") String keyword, Pageable pageable);

    // 페이지네이션 없이 전체를 가져오는 메서드
    @Query("""
    select p from PLEntity p
    where p.deleteState = false
    and p.onOff = org.musicplace.playList.domain.OnOff.Public
""")
    List<PLEntity> findAllActivePublicPlaylists();

    // 전체 재생목록 ID 조회
    @Query("select p.playlistId from PLEntity p where p.deleteState = false")
    List<Long> findAllActivePlaylistIds();
}
