package com.cropkeeper.domain.cultivation.exception;

import com.cropkeeper.global.exception.BaseException;
import com.cropkeeper.global.exception.ErrorCode;

public class InvalidCultivationRequestException extends BaseException {

    public InvalidCultivationRequestException(ErrorCode errorCode) {
        super(errorCode);
    }
}
