package com.cropkeeper.crop.exception;

import com.cropkeeper.common.exception.BaseException;

public class CropTypeNotFoundException extends BaseException {

    public CropTypeNotFoundException(Long typeId) {
        super(CropErrorCode.CROP_TYPE_NOT_FOUND,
                CropErrorCode.CROP_TYPE_NOT_FOUND.getMessage() + " 작물 ID: " + typeId);
    }

    public CropTypeNotFoundException(String typeName) {
        super(CropErrorCode.CROP_TYPE_NOT_FOUND,
                CropErrorCode.CROP_TYPE_NOT_FOUND.getMessage() + " 작물명: " + typeName);
    }
}
