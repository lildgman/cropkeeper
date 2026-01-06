package com.cropkeeper.auth.exception;

import com.cropkeeper.common.exception.BaseException;

public class DeletedMemberLoginException extends BaseException {

    public DeletedMemberLoginException() {
        super(AuthErrorCode.DELETED_MEMBER_LOGIN);
    }

    public DeletedMemberLoginException(String username) {
        super(AuthErrorCode.DELETED_MEMBER_LOGIN,
                AuthErrorCode.DELETED_MEMBER_LOGIN.getMessage() + " (username: " + username + ")");
    }
}
