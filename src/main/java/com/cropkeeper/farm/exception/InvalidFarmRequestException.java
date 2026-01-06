package com.cropkeeper.farm.exception;

import com.cropkeeper.common.exception.BaseException;

public class InvalidFarmRequestException extends BaseException {

    public InvalidFarmRequestException(FarmErrorCode errorCode) {
        super(errorCode);
    }



}
