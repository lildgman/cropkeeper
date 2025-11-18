package com.cropkeeper.domain.auth.exception;

import com.cropkeeper.global.exception.BaseException;

public class InvalidCredentialsException extends BaseException {

    public InvalidCredentialsException() {
        super(AuthErrorCode.INVALID_CREDENTIALS);
    }
}
