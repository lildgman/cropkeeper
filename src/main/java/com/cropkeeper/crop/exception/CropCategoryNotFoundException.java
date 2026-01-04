package com.cropkeeper.crop.exception;

import com.cropkeeper.global.exception.BaseException;

public class CropCategoryNotFoundException extends BaseException {

    public CropCategoryNotFoundException(Long categoryId) {
        super(CropErrorCode.CROP_CATEGORY_NOT_FOUND,
                 CropErrorCode.CROP_CATEGORY_NOT_FOUND.getMessage() + " 카테고리 ID: " + categoryId);
    }

    public CropCategoryNotFoundException(String categoryName) {
        super(CropErrorCode.CROP_CATEGORY_NOT_FOUND,
                CropErrorCode.CROP_CATEGORY_NOT_FOUND.getMessage()  +" 카테고리 이름: " + categoryName);
    }
}
