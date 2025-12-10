package com.cropkeeper.domain.cultivation.exception;

import com.cropkeeper.global.exception.BaseException;

public class CultivationAlreadyDeletedException extends BaseException {

    public CultivationAlreadyDeletedException(Long cultivationLogId) {
        super(
                CultivationErrorCode.ALREADY_DELETED,
                String.format("이미 삭제된 재배기록입니다. (cultivationLogId = %d)", cultivationLogId)
        );
    }
}
