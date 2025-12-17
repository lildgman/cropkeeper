package com.cropkeeper.domain.crop.exception;

import com.cropkeeper.global.exception.BaseException;

/**
 * 작물이 연결된 카테고리를 수정 또는 삭제하려 할 때 발생하는 예외
 */
public class CropCategoryHasCropsException extends BaseException {

    public CropCategoryHasCropsException(Long categoryId, String action) {
        super(CropErrorCode.CATEGORY_HAS_CROPS,
                CropErrorCode.CATEGORY_HAS_CROPS.getMessage() +
                " 카테고리 ID: " + categoryId + ", 작업: " + action);
    }
}
