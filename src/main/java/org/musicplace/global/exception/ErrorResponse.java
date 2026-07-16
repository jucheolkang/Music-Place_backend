package org.musicplace.global.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {

    private final int status;

    private final String error;

    private final String message;

    private final String path;

    private final LocalDateTime timestamp;
}
