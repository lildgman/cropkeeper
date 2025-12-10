package com.cropkeeper.domain.cultivation.exception;

import com.cropkeeper.global.exception.BaseException;

public class CropVarietyNotFoundException extends BaseException {

    public CropVarietyNotFoundException(Long varietyId) {
        super(
                CultivationErrorCode.CROP_VARIETY_NOT_FOUND,
                String.format("작물 품종을 찾을 수 없습니다. (varietyId = %d)", varietyId)
        );
    }
}
