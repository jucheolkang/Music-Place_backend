package org.musicplace.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    /**
     * ===========================
     * Member
     * ===========================
     */

    ID_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "MEMBER_001",
            "해당 사용자를 찾을 수 없습니다."
    ),

    MEMBER_DELETED(
            HttpStatus.GONE,
            "MEMBER_002",
            "탈퇴한 사용자입니다."
    ),

    EMAIL_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "MEMBER_003",
            "해당 이메일을 찾을 수 없습니다."
    ),

    INVALID_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "AUTH_001",
            "아이디 또는 비밀번호가 올바르지 않습니다."
    ),

    /**
     * ===========================
     * Follow
     * ===========================
     */

    FOLLOW_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "FOLLOW_001",
            "이미 팔로우한 사용자입니다."
    ),

    FOLLOW_NOT_EXISTS(
            HttpStatus.NOT_FOUND,
            "FOLLOW_002",
            "팔로우 정보가 존재하지 않습니다."
    ),

    FOLLOW_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "FOLLOW_003",
            "해당 팔로워를 찾을 수 없습니다."
    ),

    CANNOT_FOLLOW_SELF(
            HttpStatus.BAD_REQUEST,
            "FOLLOW_004",
            "자기 자신은 팔로우할 수 없습니다."
    ),

    /**
     * ===========================
     * Music
     * ===========================
     */

    MUSIC_ALREADY_EXISTS(
            HttpStatus.CONFLICT,
            "MUSIC_001",
            "이미 등록된 음악입니다."
    ),

    /**
     * ===========================
     * Common
     * ===========================
     */

    RESULT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "COMMON_001",
            "조회 결과가 존재하지 않습니다."
    );

    /**
     * HTTP Status
     */
    private final HttpStatus status;

    /**
     * Error Code
     */
    private final String code;

    /**
     * Client Message
     */
    private final String message;
}
