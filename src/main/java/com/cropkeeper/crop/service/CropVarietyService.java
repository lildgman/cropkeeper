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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CropVarietyService {

    private final CropTypeRepository cropTypeRepository;
    private final CropVarietyRepository cropVarietyRepository;

    /**
     * 품종 생성
     *
     * @param request 품종 생성 요청 정보
     * @return 생성된 품종 응답 정보
     * @throws CropTypeNotFoundException 작물이 존재하지 않을 경우
     * @throws DuplicateCropVarietyNameException 품종명이 중복된 경우
     */
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

    /**
     * 품종 전체 조회
     *
     * @return 조회한 품종 응답 목록
     */
    public List<CropVarietyResponse> getAllCropVarieties() {
        List<CropVariety> cropVarieties = cropVarietyRepository.findAllByDeletedFalse();

        return cropVarieties.stream()
                .map(CropVarietyResponse::from)
                .toList();
    }


    /**
     * 품종명 존재 여부 검증
     * @param varietyName 품종 이름
     * @throws DuplicateCropVarietyNameException 품종명이 이미 존재하는 경우
     */
    private void validationVarietyNameNotDuplicate(String varietyName) {

        if (cropVarietyRepository.existsByVarietyNameAndDeletedFalse(varietyName)) {
            log.warn("품종명이 이미 존재합니다. varietyName = {}", varietyName);

            throw new DuplicateCropVarietyNameException(varietyName);
        }
    }

    /**
     * 작물 ID 조회
     * @param typeId 조회할 작물 ID
     * @return 조회한 작물 ID
     * @throws CropTypeNotFoundException 조회할 작물이 존재하지 않는 경우
     */
    private CropType findCropTypeById(Long typeId) {

        return cropTypeRepository.findById(typeId)
                .orElseThrow(() -> new CropTypeNotFoundException(typeId));
    }
}
