package com.cropkeeper.domain.member.exception;

import com.cropkeeper.global.exception.BaseException;
import lombok.Getter;

@Getter
public class ForbiddenAccessException extends BaseException {

    public ForbiddenAccessException(MemberErrorCode errorCode) {
        super(errorCode);
    }
}
