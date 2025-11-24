package com.cropkeeper.domain.farm.exception;

import com.cropkeeper.global.exception.BaseException;

public class FarmAlreadyDeletedException extends BaseException {

    public FarmAlreadyDeletedException() {
        super(FarmErrorCode.ALREADY_DELETED);
    }

    public FarmAlreadyDeletedException(Long farmId) {
        super(FarmErrorCode.ALREADY_DELETED,
                FarmErrorCode.ALREADY_DELETED.getMessage() + " farmId: " + farmId);
    }
}
