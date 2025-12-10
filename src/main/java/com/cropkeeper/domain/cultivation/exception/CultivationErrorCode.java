package com.cropkeeper.domain.cultivation.exception;

import com.cropkeeper.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CultivationErrorCode implements ErrorCode {

    CULTIVATION_LOG_NOT_FOUND("C001", "재배기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NO_FIELD_TO_UPDATE("C002", "수정할 정보가 없습니다. 재배량, 메타데이터, 또는 품종을 입력해주세요.", HttpStatus.BAD_REQUEST),
    ALREADY_DELETED("C003", "이미 삭제된 재배기록입니다.", HttpStatus.CONFLICT),
    CROP_VARIETY_NOT_FOUND("C004", "작물 품종을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
