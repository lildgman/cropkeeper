package com.cropkeeper.farm.exception;

import com.cropkeeper.common.exception.BaseException;

public class ForbiddenFarmAccessException extends BaseException {

    public ForbiddenFarmAccessException() {
        super(FarmErrorCode.FORBIDDEN_FARM_ACCESS);
    }
}
