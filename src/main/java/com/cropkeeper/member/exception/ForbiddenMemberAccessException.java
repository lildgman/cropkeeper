package com.cropkeeper.member.exception;

import com.cropkeeper.common.exception.BaseException;

public class ForbiddenMemberAccessException extends BaseException {

    public ForbiddenMemberAccessException(MemberErrorCode errorCode) {
        super(errorCode);
    }
}
