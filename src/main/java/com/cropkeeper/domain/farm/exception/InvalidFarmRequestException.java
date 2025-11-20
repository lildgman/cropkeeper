package com.cropkeeper.domain.farm.exception;

import com.cropkeeper.global.exception.BaseException;

public class InvalidFarmRequestException extends BaseException {

    public InvalidFarmRequestException(FarmErrorCode errorCode) {
        super(errorCode);
    }



}
