package com.cropkeeper.domain.crop.exception;

import com.cropkeeper.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 작물 도메인 에러 코드
 *
 * 작물 카테고리, 작물, 품종 관련 에러 코드를 정의합니다.
 */
@Getter
@RequiredArgsConstructor
public enum CropErrorCode implements ErrorCode {

    // 작물 카테고리 관련 에러
    CROP_CATEGORY_NOT_FOUND("C001", "작물 카테고리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DUPLICATE_CATEGORY_NAME("C002", "이미 존재하는 카테고리명입니다.", HttpStatus.CONFLICT),
    CATEGORY_HAS_CROPS("C003", "해당 카테고리에 연결된 작물이 있어 작업할 수 없습니다.", HttpStatus.CONFLICT),

    // 작물 관련 에러
    CROP_NOT_FOUND("C101", "작물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_CROP_REQUEST("C102", "잘못된 작물 요청입니다.", HttpStatus.BAD_REQUEST),

    // 품종 관련 에러
    CROP_VARIETY_NOT_FOUND("C201", "품종을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
