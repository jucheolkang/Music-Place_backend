package org.musicplace.playList.repository;

import org.musicplace.playList.domain.PLEntity;
import org.musicplace.playList.dto.ResponsePLDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PLRepository extends JpaRepository<PLEntity, Long> {

    /* ================== 기본 검증 ================== */

    boolean existsByPlaylistIdAndPLDeleteFalse(Long playlistId);

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
    @Query(
            value = """
        SELECT
            playlist_id   AS playlistId,
            title         AS PLTitle,
            nickname,
            cover_img     AS cover_img,
            comment,
            onoff
        FROM PLAYLIST
        WHERE member_id = :memberId
          AND delete_state = false
        ORDER BY playlist_id DESC
        """,
            nativeQuery = true
    )
    List<ResponsePLDto> findMyPlaylists(@Param("memberId") String memberId);

    /** 다른 유저 공개 플레이리스트 */
    @Query(
            value = """
        SELECT
            playlist_id   AS playlistId,
            title         AS PLTitle,
            nickname,
            cover_img     AS cover_img,
            comment,
            onoff
        FROM PLAYLIST
        WHERE member_id = :memberId
          AND onoff = 'Public'
          AND delete_state = false
        ORDER BY playlist_id DESC
        """,
            nativeQuery = true
    )
    List<ResponsePLDto> findOtherUserPublicPlaylists(
            @Param("memberId") String memberId
    );

    /** 전체 공개 플레이리스트 */
    @Query(
            value = """
        SELECT
            playlist_id   AS playlistId,
            title         AS PLTitle,
            nickname,
            cover_img     AS cover_img,
            comment,
            onoff
        FROM PLAYLIST
        WHERE onoff = 'Public'
          AND delete_state = false
        ORDER BY playlist_id DESC
        """,
            nativeQuery = true
    )
    List<ResponsePLDto> findAllPublicPlaylists();
}
