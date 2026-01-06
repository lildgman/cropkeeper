package com.cropkeeper.crop.service;

import com.cropkeeper.crop.dto.request.CreateCropTypeRequest;
import com.cropkeeper.crop.dto.request.UpdateCropTypeRequest;
import com.cropkeeper.crop.exception.CropTypeHasVarietiesException;
import com.cropkeeper.crop.exception.InvalidCropRequestException;
import com.cropkeeper.crop.repository.CropCategoryRepository;
import com.cropkeeper.crop.repository.CropTypeRepository;
import com.cropkeeper.crop.repository.CropVarietyRepository;
import com.cropkeeper.crop.dto.response.CropTypeResponse;
import com.cropkeeper.crop.entity.CropCategory;
import com.cropkeeper.crop.entity.CropType;
import com.cropkeeper.crop.exception.CropCategoryNotFoundException;
import com.cropkeeper.crop.exception.CropErrorCode;
import com.cropkeeper.crop.exception.CropTypeNotFoundException;
import com.cropkeeper.crop.exception.DuplicateCropTypeNameException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CropTypeService {

    private final CropTypeRepository cropTypeRepository;
    private final CropCategoryRepository cropCategoryRepository;
    private final CropVarietyRepository cropVarietyRepository;

    /**
     * 작물 생성
     *
     * @param request 작물 생성 요청 정보 (작물명, 카테고리 ID)
     * @return 생성된 작물 응답 정보
     * @throws DuplicateCropTypeNameException 작물명이 중복된 경우
     * @throws CropCategoryNotFoundException 카테고리를 찾을 수 없는 경우
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
     * 전체 작물 조회 (삭제되지 않은 작물만)
     *
     * @return 작물 목록 응답
     */
    public List<CropTypeResponse> getAllCropTypes() {

        List<CropType> cropTypes = cropTypeRepository.findAllByDeletedFalse();

        return cropTypes.stream()
                .map(CropTypeResponse::from)
                .toList();

    }

    /**
     * 작물 ID로 조회
     *
     * @param typeId 작물 ID
     * @return 작물 응답 정보
     * @throws CropTypeNotFoundException 작물을 찾을 수 없는 경우
     */
    public CropTypeResponse getCropTypeById(Long typeId) {

        return CropTypeResponse.from(findById(typeId));

    }

    /**
     * 카테고리별 작물 조회 (삭제되지 않은 작물만)
     *
     * @param categoryId 카테고리 ID
     * @return 해당 카테고리에 속한 작물 목록 응답
     */
    public List<CropTypeResponse> getCropTypesByCategoryId(Long categoryId) {
        validateCropCategoryExists(categoryId);
        List<CropType> cropTypes = cropTypeRepository.findByCategoryCategoryIdAndDeletedFalse(categoryId);

        return cropTypes.stream()
                .map(CropTypeResponse::from)
                .toList();
    }

    private void validateCropCategoryExists(Long categoryId) {
        if (!cropCategoryRepository.existsByCategoryIdAndDeletedFalse(categoryId)) {
            throw new CropCategoryNotFoundException(categoryId);
        }
    }

    /**
     * 작물 수정
     *
     * @param typeId 작물 ID
     * @param request 수정 요청
     * @return 수정된 작물 응답
     * @throws CropTypeNotFoundException 작물을 찾을 수 없는 경우
     * @throws InvalidCropRequestException 수정할 필드가 없는 경우
     * @throws DuplicateCropTypeNameException 중복된 작물명인 경우
     * @throws CropCategoryNotFoundException 카테고리를 찾을 수 없는 경우
     */
    @Transactional
    public CropTypeResponse updateCropType(Long typeId, UpdateCropTypeRequest request) {

        // 1. 빈 요청 검증
        if (request.getTypeName() == null && request.getCategoryId() == null) {
            log.warn("작물 수정 실패: 수정할 필드 없음 - typeId = {}", typeId);
            throw new InvalidCropRequestException(CropErrorCode.INVALID_CROP_REQUEST);
        }

        // 2. 작물 조회
        CropType cropType = findById(typeId);

        // 3. 작물명 수정 (제공된 경우)
        if (request.getTypeName() != null && !request.getTypeName().isBlank()) {
            String newTypeName = request.getTypeName();

            // 기존 이름과 다른 경우만 중복 검증 및 수정
            if (!cropType.getTypeName().equals(newTypeName)) {
                validateTypeNameNotDuplicate(newTypeName);
                cropType.updateCropName(newTypeName);
                log.info("작물명 수정 완료: typeId = {}, 새 이름 = {}", typeId, newTypeName);
            }
        }

        // 4. 카테고리 수정 (제공된 경우)
        if (request.getCategoryId() != null) {
            Long newCategoryId = request.getCategoryId();

            // 기존 카테고리와 다른 경우만 수정
            if (!cropType.getCategory().getCategoryId().equals(newCategoryId)) {
                CropCategory newCategory = findCategoryById(newCategoryId);
                cropType.updateCategory(newCategory);
                log.info("작물 카테고리 수정 완료: typeId = {}, 새 카테고리 = {}",
                        typeId, newCategory.getCategoryName());
            }
        }

        return CropTypeResponse.from(cropType);
    }

    /**
     * 작물 삭제 (Soft Delete)
     *
     * @param typeId 삭제할 작물 ID
     * @throws CropTypeNotFoundException 작물을 찾을 수 없는 경우
     * @throws CropTypeHasVarietiesException 연결된 품종이 있는 경우
     */
    @Transactional
    public void deleteCropType(Long typeId) {

        CropType cropType = findById(typeId);
        validateNoVarieties(typeId);

        cropType.delete();
        log.info("작물 삭제 완료: typeId = {}, typeName = {}",
                typeId, cropType.getTypeName());
    }

    /**
     * 품종 존재 여부 검증
     *
     * @param typeId 작물 ID
     * @throws CropTypeHasVarietiesException 품종이 존재하는 경우
     */
    private void validateNoVarieties(Long typeId) {

        if (cropVarietyRepository.existsByCropTypeTypeIdAndDeletedFalse(typeId)) {
            log.warn("품종이 연결된 작물 삭제 시도: typeId = {}", typeId);
            throw new CropTypeHasVarietiesException(typeId);
        }
    }


    /**
     * 작물명 중복 검증
     *
     * @param typeName 검증할 작물명
     * @throws DuplicateCropTypeNameException 작물명이 이미 존재하는 경우
     */
    private void validateTypeNameNotDuplicate(String typeName) {

        if (cropTypeRepository.existsByTypeNameAndDeletedFalse(typeName)) {
            log.warn("작물명이 이미 존재합니다. typeName = {}", typeName);

            throw new DuplicateCropTypeNameException(typeName);
        }

    }

    /**
     * 카테고리 조회
     *
     * @param categoryId 카테고리 ID
     * @return 카테고리 엔티티
     * @throws CropCategoryNotFoundException 카테고리를 찾을 수 없는 경우
     */
    private CropCategory findCategoryById(Long categoryId) {

        return cropCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new CropCategoryNotFoundException(categoryId));

    }

    /**
     * 작물 조회
     *
     * @param typeId 작물 ID
     * @return 작물 엔티티
     * @throws CropTypeNotFoundException 작물을 찾을 수 없는 경우
     */
    private CropType findById(Long typeId) {

        return cropTypeRepository.findById(typeId)
                .orElseThrow(() -> new CropTypeNotFoundException(typeId));
    }



}
