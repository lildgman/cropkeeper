package com.cropkeeper.global.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API 에러 응답 DTO
 *
 * 모든 에러 응답을 일관된 형식으로 반환
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private LocalDateTime timestamp;  // 에러 발생 시간
    private int status;               // HTTP 상태 코드
    private String errorCode;
    private String message;           // 사용자에게 보여줄 메시지
    private String path;              // 에러가 발생한 API 경로

    /**
     * 에러 응답 생성 편의 메서드
     */
    public static ErrorResponse of(int status, String errorCode, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .build();
    }

    public static ErrorResponse of(int status, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .errorCode(null)
                .message(message)
                .path(path)
                .build();
    }

}
