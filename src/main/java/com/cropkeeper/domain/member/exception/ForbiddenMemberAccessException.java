package com.cropkeeper.domain.member.exception;

import com.cropkeeper.global.exception.BaseException;
import lombok.Getter;

@Getter
public class ForbiddenMemberAccessException extends BaseException {

    public ForbiddenMemberAccessException(MemberErrorCode errorCode) {
        super(errorCode);
    }
}
