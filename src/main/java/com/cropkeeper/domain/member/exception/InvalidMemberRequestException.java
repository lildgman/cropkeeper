package com.cropkeeper.domain.member.exception;

import com.cropkeeper.global.exception.BaseException;
import lombok.Getter;

@Getter
public class InvalidMemberRequestException extends BaseException {

    public InvalidMemberRequestException(MemberErrorCode errorCode) {
        super(errorCode);
    }
}
