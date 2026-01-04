package com.cropkeeper.crop.exception;

import com.cropkeeper.global.exception.BaseException;

public class DuplicateCropTypeNameException extends BaseException {

    public DuplicateCropTypeNameException(String typeName) {
        super(CropErrorCode.DUPLICATE_CROP_TYPE_NAME,
                CropErrorCode.DUPLICATE_CROP_TYPE_NAME.getMessage() + " 작물명: " + typeName);
    }
}
