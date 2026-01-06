package com.cropkeeper.member.exception;

import com.cropkeeper.common.exception.BaseException;

public class InvalidAspectConfigurationException extends BaseException {

    public InvalidAspectConfigurationException(MemberErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidAspectConfigurationException(MemberErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }
}
