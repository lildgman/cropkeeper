package com.cropkeeper.crop.exception;

import com.cropkeeper.global.exception.BaseException;

public class DuplicateCropCategoryNameException extends BaseException {

    public DuplicateCropCategoryNameException(String categoryName) {
        super(CropErrorCode.DUPLICATE_CATEGORY_NAME,
                CropErrorCode.DUPLICATE_CATEGORY_NAME.getMessage() + " 카테고리명: " + categoryName);
    }
}
