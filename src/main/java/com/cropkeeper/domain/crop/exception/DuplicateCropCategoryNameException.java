package com.cropkeeper.domain.crop.exception;

import com.cropkeeper.global.exception.BaseException;

/**
 * 중복된 카테고리명으로 생성 또는 수정 시 발생하는 예외
 */
public class DuplicateCropCategoryNameException extends BaseException {

    public DuplicateCropCategoryNameException(String categoryName) {
        super(CropErrorCode.DUPLICATE_CATEGORY_NAME,
                CropErrorCode.DUPLICATE_CATEGORY_NAME.getMessage() + " 카테고리명: " + categoryName);
    }
}
