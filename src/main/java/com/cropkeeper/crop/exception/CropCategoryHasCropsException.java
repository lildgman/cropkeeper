package com.cropkeeper.crop.exception;

import com.cropkeeper.global.exception.BaseException;

public class CropCategoryHasCropsException extends BaseException {

    public CropCategoryHasCropsException(Long categoryId, String action) {
        super(CropErrorCode.CATEGORY_HAS_CROPS,
                CropErrorCode.CATEGORY_HAS_CROPS.getMessage() +
                " 카테고리 ID: " + categoryId + ", 작업: " + action);
    }
}
