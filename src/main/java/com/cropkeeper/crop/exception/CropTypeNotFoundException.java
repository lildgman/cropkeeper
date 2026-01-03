package com.cropkeeper.crop.exception;

import com.cropkeeper.global.exception.BaseException;

/**
 * 작물을 찾을 수 없을 때 발생하는 예외
 */
public class CropTypeNotFoundException extends BaseException {

    public CropTypeNotFoundException(Long typeId) {
        super(CropErrorCode.CROP_TYPE_NOT_FOUND,
                "작물을 찾을 수 없습니다. 작물 ID: " + typeId);
    }

    public CropTypeNotFoundException(String typeName) {
        super(CropErrorCode.CROP_TYPE_NOT_FOUND,
                "작물을 찾을 수 없습니다. 작물명: " + typeName);
    }
}
