package com.cropkeeper.domain.crop.service;

import com.cropkeeper.domain.crop.dto.request.CreateCropTypeRequest;
import com.cropkeeper.domain.crop.dto.response.CropTypeResponse;
import com.cropkeeper.domain.crop.entity.CropCategory;
import com.cropkeeper.domain.crop.entity.CropType;
import com.cropkeeper.domain.crop.exception.CropCategoryNotFoundException;
import com.cropkeeper.domain.crop.exception.DuplicateCropTypeNameException;
import com.cropkeeper.domain.crop.repository.CropCategoryRepository;
import com.cropkeeper.domain.crop.repository.CropTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CropTypeService {

    private final CropTypeRepository cropTypeRepository;
    private final CropCategoryRepository cropCategoryRepository;

    /**
     * 작물 생성
     *
     * @param request
     * @return
     */
    @Transactional
    public CropTypeResponse createCropType(CreateCropTypeRequest request) {

        String typeName = request.getTypeName();
        Long categoryId = request.getCategoryId();

        validateTypeNameNotDuplicate(typeName);
        CropCategory category = findCategoryById(categoryId);

        CropType cropType = CropType.builder()
                .typeName(typeName)
                .category(category)
                .build();

        CropType savedCropType = cropTypeRepository.save(cropType);

        log.info("작물 생성 완료: typeId = {}, typeName = {}, categoryId = {}",
                savedCropType.getTypeId(), savedCropType.getTypeName(), savedCropType.getCategory().getCategoryId());

        return CropTypeResponse.from(savedCropType);
    }



    /**
     * 작물명 중복 검증
     * @param typeName
     */
    private void validateTypeNameNotDuplicate(String typeName) {

        Optional<CropType> existingCropType = cropTypeRepository.findByCropTypeName(typeName);
        if (existingCropType.isPresent()) {
            log.warn("작물명이 이미 존재합니다. typeName = {}", typeName);

            throw new DuplicateCropTypeNameException(typeName);
        }

    }

    /**
     * 카테고리 조회
     * @param categoryId
     * @return
     */
    private CropCategory findCategoryById(Long categoryId) {

        return cropCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new CropCategoryNotFoundException(categoryId));

    }
}
