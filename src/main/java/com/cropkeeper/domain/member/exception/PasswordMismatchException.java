package com.cropkeeper.domain.member.exception;

import com.cropkeeper.global.exception.BaseException;

public class PasswordMismatchException extends BaseException {

    public PasswordMismatchException(MemberErrorCode errorCode) {
        super(errorCode);
    }
}
