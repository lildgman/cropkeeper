package com.cropkeeper.member.exception;

import com.cropkeeper.common.exception.BaseException;

public class AlreadyDeletedException extends BaseException {

    public AlreadyDeletedException() {
        super(MemberErrorCode.ALREADY_DELETED);
    }

    public AlreadyDeletedException( Long memberId) {
        super(MemberErrorCode.ALREADY_DELETED,
                MemberErrorCode.ALREADY_DELETED.getMessage() + " memberId: " + memberId);
    }
}
