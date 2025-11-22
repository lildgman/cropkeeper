package com.cropkeeper.domain.member.exception;

import com.cropkeeper.global.exception.BaseException;

public class ForbiddenMemberAccessException extends BaseException {

    public ForbiddenMemberAccessException(MemberErrorCode errorCode) {
        super(errorCode);
    }
}
