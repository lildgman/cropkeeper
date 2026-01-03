package com.cropkeeper.crop.exception;

import com.cropkeeper.global.exception.BaseException;

/**
 * 품종이 연결된 작물을 삭제하려 할 때 발생하는 예외
 */
public class CropTypeHasVarietiesException extends BaseException {

    public CropTypeHasVarietiesException(Long typeId) {
        super(CropErrorCode.CROP_TYPE_HAS_VARIETIES,
                CropErrorCode.CROP_TYPE_HAS_VARIETIES.getMessage() +
                " 작물 ID: " + typeId);
    }
}
