package com.cropkeeper.domain.farm.exception;

import com.cropkeeper.global.exception.BaseException;

public class ForbiddenFarmAccessException extends BaseException {

    public ForbiddenFarmAccessException() {
        super(FarmErrorCode.FORBIDDEN_FARM_ACCESS);
    }
}
