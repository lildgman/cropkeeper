package com.cropkeeper.crop.exception;

import com.cropkeeper.global.exception.BaseException;

/**
 * 중복된 작물명으로 생성 또는 수정 시 발생하는 예외
 */
public class DuplicateCropTypeNameException extends BaseException {

    public DuplicateCropTypeNameException(String typeName) {
        super(CropErrorCode.DUPLICATE_CROP_TYPE_NAME,
                CropErrorCode.DUPLICATE_CROP_TYPE_NAME.getMessage() + " 작물명: " + typeName);
    }
}
