package com.cropkeeper.domain.member.exception;

import com.cropkeeper.global.exception.BaseException;
import lombok.Getter;

@Getter
public class PasswordMismatchException extends BaseException {

    public PasswordMismatchException(MemberErrorCode errorCode) {
        super(errorCode);
    }
}
