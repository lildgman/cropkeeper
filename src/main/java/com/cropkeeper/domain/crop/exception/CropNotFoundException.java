package com.cropkeeper.domain.crop.exception;

import com.cropkeeper.global.exception.BaseException;

/**
 * 작물을 찾을 수 없을 때 발생하는 예외
 */
public class CropNotFoundException extends BaseException {

    public CropNotFoundException(Long cropId) {
        super(CropErrorCode.CROP_NOT_FOUND,
                "작물 ID: " + cropId);
    }

    public CropNotFoundException(String cropName) {
        super(CropErrorCode.CROP_NOT_FOUND,
                "작물명: " + cropName);
    }
}
