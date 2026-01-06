package com.cropkeeper.farm.exception;


import com.cropkeeper.common.exception.BaseException;

public class FarmNotFoundException extends BaseException {

    public FarmNotFoundException(Long farmId) {
        super(FarmErrorCode.FARM_NOT_FOUND,
                FarmErrorCode.FARM_NOT_FOUND.getMessage()+ " farmId: " + farmId);
    }
}
