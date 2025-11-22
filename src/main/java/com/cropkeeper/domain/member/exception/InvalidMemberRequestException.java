package com.cropkeeper.domain.member.exception;

import com.cropkeeper.global.exception.BaseException;

public class InvalidMemberRequestException extends BaseException {

    public InvalidMemberRequestException(MemberErrorCode errorCode) {
        super(errorCode);
    }
}
