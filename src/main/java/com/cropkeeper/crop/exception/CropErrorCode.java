package com.cropkeeper.crop.exception;

import com.cropkeeper.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CropErrorCode implements ErrorCode {

    // 작물 카테고리 관련 에러
    CROP_CATEGORY_NOT_FOUND("C001", "존재하지 않는 작물 카테고리입니다.", HttpStatus.NOT_FOUND),
    DUPLICATE_CATEGORY_NAME("C002", "이미 존재하는 카테고리명입니다.", HttpStatus.CONFLICT),
    CATEGORY_HAS_CROPS("C003", "해당 카테고리에 연결된 작물이 있어 작업할 수 없습니다.", HttpStatus.CONFLICT),

    // 작물 관련 에러
    CROP_TYPE_NOT_FOUND("C101", "존재하지 않는 작물입니다.", HttpStatus.NOT_FOUND),
    INVALID_CROP_REQUEST("C102", "잘못된 작물 요청입니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_CROP_TYPE_NAME("C103", "이미 존재하는 작물명입니다.", HttpStatus.CONFLICT),
    CROP_TYPE_HAS_VARIETIES("C104", "해당 작물에 연결된 품종이 있어 삭제할 수 없습니다.", HttpStatus.CONFLICT),

    // 품종 관련 에러
    CROP_VARIETY_NOT_FOUND("C201", "존재하지 않는 품종입니다.", HttpStatus.NOT_FOUND),
    DUPLICATE_CROP_VARIETY_NAME("C202", "이미 존재하는 품종명입니다.", HttpStatus.CONFLICT)
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
