package com.cropkeeper.domain.auth.exception;

import com.cropkeeper.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    DUPLICATE_USERNAME("A001", "이미 사용 중인 사용자 이름입니다."),
    REGISTER_PASSWORD_MISMATCH("A002", "비밀번호가 일치하지 않습니다."),

    DELETED_MEMBER_LOGIN("A003", "탈퇴한 회원입니다. 로그인할 수 없습니다."),
    INVALID_CREDENTIALS("A004", "아이디 또는 비밀번호가 올바르지 않습니다.");

    private final String code;
    private final String message;
}
