package com.cropkeeper.domain.member.exception;

import com.cropkeeper.global.exception.BaseException;

public class InvalidAspectConfigurationException extends BaseException {

    public InvalidAspectConfigurationException(MemberErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidAspectConfigurationException(MemberErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }
}
