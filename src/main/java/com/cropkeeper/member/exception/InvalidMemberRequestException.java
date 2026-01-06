package com.cropkeeper.member.exception;

import com.cropkeeper.common.exception.BaseException;

public class InvalidMemberRequestException extends BaseException {

    public InvalidMemberRequestException(MemberErrorCode errorCode) {
        super(errorCode);
    }
}
