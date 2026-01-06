package com.cropkeeper.member.exception;

import com.cropkeeper.common.exception.BaseException;

public class PasswordMismatchException extends BaseException {

    public PasswordMismatchException(MemberErrorCode errorCode) {
        super(errorCode);
    }
}
