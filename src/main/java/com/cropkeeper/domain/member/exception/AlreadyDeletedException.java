package com.cropkeeper.domain.member.exception;

import com.cropkeeper.global.exception.BaseException;

public class AlreadyDeletedException extends BaseException {

    public AlreadyDeletedException() {
        super(MemberErrorCode.ALREADY_DELETED);
    }

    public AlreadyDeletedException( Long memberId) {
        super(MemberErrorCode.ALREADY_DELETED,
                MemberErrorCode.ALREADY_DELETED.getMessage() + " memberId: " + memberId);
    }
}
