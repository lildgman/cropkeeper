package com.cropkeeper.member.exception;

import com.cropkeeper.common.exception.BaseException;

public class MemberNotFoundException extends BaseException {

    public MemberNotFoundException() {
        super(MemberErrorCode.MEMBER_NOT_FOUND);
    }

    public MemberNotFoundException(Long memberId) {
        super(MemberErrorCode.MEMBER_NOT_FOUND, MemberErrorCode.MEMBER_NOT_FOUND.getMessage() + " memberId: " + memberId);
    }
}
