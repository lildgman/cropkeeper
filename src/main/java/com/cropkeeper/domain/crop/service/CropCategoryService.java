package com.cropkeeper.domain.crop.service;

import com.cropkeeper.domain.crop.dto.request.CreateCropCategoryRequest;
import com.cropkeeper.domain.crop.dto.request.UpdateCropCategoryRequest;
import com.cropkeeper.domain.crop.dto.response.CropCategoryResponse;
import com.cropkeeper.domain.crop.entity.CropCategory;
import com.cropkeeper.domain.crop.exception.CropCategoryHasCropsException;
import com.cropkeeper.domain.crop.exception.CropCategoryNotFoundException;
import com.cropkeeper.domain.crop.exception.DuplicateCropCategoryNameException;
import com.cropkeeper.domain.crop.repository.CropCategoryRepository;
import com.cropkeeper.domain.crop.repository.CropTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CropCategoryService {

    private final CropCategoryRepository categoryRepository;
    private final CropTypeRepository cropTypeRepository;

    /**
     * 작물 카테고리 생성
     * @param request 카테고리 생성 요청 dto
     * @return 생성된 카테고리 응답
     * @throws DuplicateCropCategoryNameException 중복된 카테고리명인 경우
     */
    @Transactional
    public CropCategoryResponse createCategory(CreateCropCategoryRequest request) {

        String categoryName = request.getCategoryName();

        validateCategoryNameNotDuplicate(categoryName);

        CropCategory category = CropCategory.builder()
                .categoryName(categoryName)
                .build();

        CropCategory savedCategory = categoryRepository.save(category);
        log.info("작물 카테고리 생성 완료: categoryId = {}, categoryName = {}",
                savedCategory.getCategoryId(), savedCategory.getCategoryName());

        return CropCategoryResponse.from(savedCategory);
    }

    /**
     * 전체 작물 카테고리 목록 조회
     *
     * @return 카테고리 목록
     */
    public List<CropCategoryResponse> getAllCategories() {

        List<CropCategory> categories = categoryRepository.findAll();

        return categories.stream()
                .map(CropCategoryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * ID로 작물 카테고리 조회
     * @param categoryId 카테고리 ID
     * @return 카테고리 응답
     * @throws CropCategoryNotFoundException 카테고리를 찾을 수 없는 경우
     */
    public CropCategoryResponse getCategoryById(Long categoryId) {
        CropCategory category = findById(categoryId);
        return CropCategoryResponse.from(category);
    }

    /**
     * 카테고리명으로 작물 카테고리 조회
     *
     * @param categoryName 카테고리명
     * @return 카테고리 응답
     * @throws CropCategoryNotFoundException 카테고리를 찾을 수 없는 경우
     */
    public CropCategoryResponse getCategoryByName(String categoryName) {
        CropCategory category = categoryRepository.findByCategoryName(categoryName)
                .orElseThrow(() -> new CropCategoryNotFoundException(categoryName));

        return CropCategoryResponse.from(category);
    }


    /**
     * 작물 카테고리 수정
     * @param categoryId 카테고리 Id
     * @param request 수정 요청
     * @return 수정된 카테고리 응답
     * @throws CropCategoryNotFoundException 카테고리를 찾을 수 없는 경우
     * @throws CropCategoryHasCropsException 카테고리에 작물이 존재하는 경우
     * @throws DuplicateCropCategoryNameException 중복된 카테고리명인 경우
     */
    @Transactional
    public CropCategoryResponse updateCategory(Long categoryId, UpdateCropCategoryRequest request) {

        CropCategory category = findById(categoryId);
        validateNoCrops(categoryId, "수정");

        String newCategoryName = request.getCategoryName();

        if (!category.getCategoryName().equals(newCategoryName)) {

            validateCategoryNameNotDuplicate(newCategoryName);
            category.updateCategoryName(newCategoryName);
            log.info("작물 카테고리 수정 완료: categoryId = {}, 새 이름 = {}",
                    categoryId, newCategoryName);
        }

        return CropCategoryResponse.from(category);

    }


    /**
     * 작물 카테고리 삭제
     *
     * @param categoryId 삭제할 카테고리 id
     */
    @Transactional
    public void deleteCategory(Long categoryId) {

        CropCategory category = findById(categoryId);
        validateNoCrops(categoryId, "삭제");

        category.delete();
        log.info("작물 카테고리 삭제 완료: categoryID = {}, categoryName = {}",
                categoryId, category.getCategoryName());

    }

    private void validateCategoryNameNotDuplicate(String categoryName) {

        if (categoryRepository.findByCategoryName(categoryName).isPresent()) {
            log.warn("카테고리명이 존재합니다. categoryName = {}", categoryName);
            throw new DuplicateCropCategoryNameException(categoryName);
        }

    }

    private CropCategory findById(Long categoryId) {

        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CropCategoryNotFoundException(categoryId));
    }

    private void validateNoCrops(Long categoryId, String action) {

        if (cropTypeRepository.existsByCategoryCategoryId(categoryId)) {
            log.warn("작물이 연결된 카테고리 {} 시도: categoryId = {}", action, categoryId);
            throw new CropCategoryHasCropsException(categoryId, action);
        }
    }


}
