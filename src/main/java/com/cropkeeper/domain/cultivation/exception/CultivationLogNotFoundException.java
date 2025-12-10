package com.cropkeeper.domain.cultivation.exception;

import com.cropkeeper.global.exception.BaseException;

public class CultivationLogNotFoundException extends BaseException {

    public CultivationLogNotFoundException(Long cultivationLogId) {
        super(
                CultivationErrorCode.CULTIVATION_LOG_NOT_FOUND,
                String.format("재배기록을 찾을 수 없습니다. (cultivationLogId = %d)", cultivationLogId)
        );
    }
}
