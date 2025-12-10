package com.cropkeeper.domain.crop.exception;

import com.cropkeeper.global.exception.BaseException;

/**
 * 잘못된 작물 요청일 때 발생하는 예외
 */
public class InvalidCropRequestException extends BaseException {

    public InvalidCropRequestException(CropErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidCropRequestException(CropErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
