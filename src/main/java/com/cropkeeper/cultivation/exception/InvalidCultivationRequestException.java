package com.cropkeeper.cultivation.exception;

import com.cropkeeper.common.exception.BaseException;
import com.cropkeeper.common.exception.ErrorCode;

public class InvalidCultivationRequestException extends BaseException {

    public InvalidCultivationRequestException(ErrorCode errorCode) {
        super(errorCode);
    }
}
