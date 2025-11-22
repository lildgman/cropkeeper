package com.cropkeeper.domain.auth.exception;

import com.cropkeeper.domain.member.exception.MemberErrorCode;
import com.cropkeeper.global.exception.BaseException;

public class DuplicateUsernameException extends BaseException {

    public DuplicateUsernameException() {
        super(MemberErrorCode.DUPLICATE_USERNAME);
    }

    public DuplicateUsernameException(String username) {
        super(MemberErrorCode.DUPLICATE_USERNAME, MemberErrorCode.DUPLICATE_USERNAME.getMessage() + ": " + username);
    }
}
