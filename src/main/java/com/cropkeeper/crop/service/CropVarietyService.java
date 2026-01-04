package com.cropkeeper.crop.service;

import com.cropkeeper.crop.dto.request.CreateCropVarietyRequest;
import com.cropkeeper.crop.dto.response.CropVarietyResponse;
import com.cropkeeper.crop.entity.CropType;
import com.cropkeeper.crop.entity.CropVariety;
import com.cropkeeper.crop.exception.CropTypeNotFoundException;
import com.cropkeeper.crop.exception.DuplicateCropVarietyNameException;
import com.cropkeeper.crop.repository.CropTypeRepository;
import com.cropkeeper.crop.repository.CropVarietyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CropVarietyService {

    @Autowired
    private final CropTypeRepository cropTypeRepository;

    @Autowired
    private final CropVarietyRepository cropVarietyRepository;

    @Transactional
    public CropVarietyResponse createCropVariety(CreateCropVarietyRequest request) {

        Long typeId = request.getTypeId();
        String varietyName = request.getVarietyName();

        CropType cropType = findCropTypeById(typeId);
        validationVarietyNameNotDuplicate(varietyName);

        CropVariety cropVariety = CropVariety.builder()
                .varietyName(varietyName)
                .cropType(cropType)
                .build();

        CropVariety savedCropVariety = cropVarietyRepository.save(cropVariety);

        log.info("품종 생성 완료: varietyId = {}, varietyName = {}, cropTypeId = {}, cropTypeName = {} ",
                savedCropVariety.getVarietyId(), savedCropVariety.getVarietyName(), savedCropVariety.getCropType().getTypeId(), savedCropVariety.getCropType().getTypeName());

        return CropVarietyResponse.from(savedCropVariety);
    }

    private void validationVarietyNameNotDuplicate(String varietyName) {

        if (cropVarietyRepository.existsByVarietyNameAndDeletedFalse(varietyName)) {
            log.warn("품종명이 이미 존재합니다. varietyName = {}", varietyName);

            throw new DuplicateCropVarietyNameException(varietyName);
        }
    }

    private CropType findCropTypeById(Long typeId) {

        return cropTypeRepository.findById(typeId)
                .orElseThrow(() -> new CropTypeNotFoundException(typeId));
    }
}
