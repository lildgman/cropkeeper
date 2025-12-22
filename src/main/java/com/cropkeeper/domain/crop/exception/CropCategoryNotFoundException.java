package com.cropkeeper.domain.crop.exception;

import com.cropkeeper.global.exception.BaseException;

/**
 * 작물 카테고리를 찾을 수 없을 때 발생하는 예외
 */
public class CropCategoryNotFoundException extends BaseException {

    public CropCategoryNotFoundException(Long categoryId) {
        super(CropErrorCode.CROP_CATEGORY_NOT_FOUND,
                "작물 카테고리를 찾을 수 없습니다. 카테고리 ID: " + categoryId);
    }

    public CropCategoryNotFoundException(String categoryName) {
        super(CropErrorCode.CROP_CATEGORY_NOT_FOUND,
                "작물 카테고리를 찾을 수 없습니다. 카테고리 이름: " + categoryName);
    }
}
