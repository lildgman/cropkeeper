package com.cropkeeper.global.exception;

/**
 * 에러 코드 인터페이스
 * 모든 도메인의 에러 코드는 이 인터페이스를 구현해야 함
 */
public interface ErrorCode {

    /**
     * 에러 코드 반환
     * @return 에러 코드 (예: M001, F001 등)
     */
    String getCode();

    /**
     * 에러 메시지 반환
     * @return 에러 메시지
     */
    String getMessage();
}
