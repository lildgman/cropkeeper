package com.cropkeeper.crop.exception;

import com.cropkeeper.global.exception.BaseException;

public class InvalidCropRequestException extends BaseException {

    public InvalidCropRequestException(CropErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidCropRequestException(CropErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
