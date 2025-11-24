package com.cropkeeper.domain.farm.exception;

import com.cropkeeper.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FarmErrorCode implements ErrorCode {

    FARM_NOT_FOUND("F001", "농장을 찾을 수 없습니다."),
    NO_FIELD_TO_UPDATE("F002", "수정할 정보가 없습니다. 농장이름 또는 주소 또는 크기를 입력해주세요."),
    FORBIDDEN_FARM_ACCESS("F003", "해당 농장에 대한 접근 권한이 없습니다."),
    ALREADY_DELETED("F004", "이미 삭제된 농장입니다.");

    private final String code;
    private final String message;
}
