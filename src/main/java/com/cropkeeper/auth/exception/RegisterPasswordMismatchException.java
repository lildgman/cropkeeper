package com.cropkeeper.auth.exception;

import com.cropkeeper.common.exception.BaseException;

public class RegisterPasswordMismatchException extends BaseException {

    public RegisterPasswordMismatchException(AuthErrorCode authErrorCode) {
        super(authErrorCode);
    }
}
