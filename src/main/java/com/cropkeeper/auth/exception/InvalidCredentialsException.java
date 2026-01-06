package com.cropkeeper.auth.exception;

import com.cropkeeper.common.exception.BaseException;

public class InvalidCredentialsException extends BaseException {

    public InvalidCredentialsException() {
        super(AuthErrorCode.INVALID_CREDENTIALS);
    }
}
