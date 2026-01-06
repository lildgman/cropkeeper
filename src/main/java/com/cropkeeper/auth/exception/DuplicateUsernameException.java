package com.cropkeeper.auth.exception;

import com.cropkeeper.member.exception.MemberErrorCode;
import com.cropkeeper.common.exception.BaseException;

public class DuplicateUsernameException extends BaseException {

    public DuplicateUsernameException() {
        super(MemberErrorCode.DUPLICATE_USERNAME);
    }

    public DuplicateUsernameException(String username) {
        super(MemberErrorCode.DUPLICATE_USERNAME, AuthErrorCode.DUPLICATE_USERNAME.getMessage() + ": " + username);
    }
}
