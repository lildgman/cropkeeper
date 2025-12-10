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

    CROP_CATEGORY_NOT_FOUND("C001", "작물 카테고리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CROP_NOT_FOUND("C002", "작물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CROP_VARIETY_NOT_FOUND("C003", "품종을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_CROP_REQUEST("C004", "잘못된 작물 요청입니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
