package com.cropkeeper.crop.exception;

import com.cropkeeper.common.exception.BaseException;

public class DuplicateCropVarietyNameException extends BaseException {

    public DuplicateCropVarietyNameException(String varietyName) {
        super(CropErrorCode.DUPLICATE_CROP_VARIETY_NAME,
                CropErrorCode.DUPLICATE_CROP_VARIETY_NAME.getMessage()+ " 품종명: " + varietyName);
    }
}
