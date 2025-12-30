package org.musicplace.global.exception;

import lombok.Getter;

public enum ErrorCode {
    ID_NOT_FOUND("해당 ID를 찾을 수 없습니다."),

    ID_DELETE("삭제된 ID입니다."),

    NOT_FOUND_RESULT("결과를 찾을 수 없습니다."),


    EMAIL_NOT_FOUND("해당 이메일을 찾을 수 없습니다."),

    FOLLOW_SAME_ID("동일한 팔로우 ID가 있습니다"),
    FOLLOW_NO_ID("사용자에게 해당 팔로워가 없습니다."),
    FOLLOW_NOT_FOUND("해당 팔로워가 존재하지 않습니다."),

    INVALID_CREDENTIALS("잘못된 자격 증명입니다."),

    SAME_MUSIC("동일한 비디오ID가 있습니다"),
    NOT_FOLLOW_SELF("스스로를 팔로우 할 수 없습니다"),
    MEMBER_DELETED("탈퇴한 사용자입니다.");


    @Getter
    private String message;

    ErrorCode(String message) {
        this.message = message;
    }
}
