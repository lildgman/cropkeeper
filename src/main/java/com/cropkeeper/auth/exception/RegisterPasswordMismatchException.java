package com.cropkeeper.auth.exception;

import com.cropkeeper.global.exception.BaseException;

public class RegisterPasswordMismatchException extends BaseException {

    public RegisterPasswordMismatchException(AuthErrorCode authErrorCode) {
        super(authErrorCode);
    }
}
