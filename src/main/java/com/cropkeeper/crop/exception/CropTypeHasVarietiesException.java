package com.cropkeeper.crop.exception;

import com.cropkeeper.global.exception.BaseException;

public class CropTypeHasVarietiesException extends BaseException {

    public CropTypeHasVarietiesException(Long typeId) {
        super(CropErrorCode.CROP_TYPE_HAS_VARIETIES,
                CropErrorCode.CROP_TYPE_HAS_VARIETIES.getMessage() +
                " 작물 ID: " + typeId);
    }
}
