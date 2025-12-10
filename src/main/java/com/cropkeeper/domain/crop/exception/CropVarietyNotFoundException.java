package com.cropkeeper.domain.crop.exception;

import com.cropkeeper.global.exception.BaseException;

/**
 * 품종을 찾을 수 없을 때 발생하는 예외
 */
public class CropVarietyNotFoundException extends BaseException {

    public CropVarietyNotFoundException(Long varietyId) {
        super(CropErrorCode.CROP_VARIETY_NOT_FOUND,
                "품종 ID: " + varietyId);
    }

    public CropVarietyNotFoundException(String varietyName) {
        super(CropErrorCode.CROP_VARIETY_NOT_FOUND,
                "품종명: " + varietyName);
    }

    public CropVarietyNotFoundException(Long cropId, String varietyName) {
        super(CropErrorCode.CROP_VARIETY_NOT_FOUND,
                String.format("작물 ID: %d, 품종명: %s", cropId, varietyName));
    }
}
