package com.cropkeeper.domain.farm.exception;


import com.cropkeeper.global.exception.BaseException;

public class FarmNotFoundException extends BaseException {

    public FarmNotFoundException(Long farmId) {
        super(FarmErrorCode.FARM_NOT_FOUND,
                FarmErrorCode.FARM_NOT_FOUND.getMessage()+ " farmId: " + farmId);
    }
}
