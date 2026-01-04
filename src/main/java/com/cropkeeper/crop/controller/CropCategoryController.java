package com.cropkeeper.crop.controller;

import com.cropkeeper.crop.dto.request.CreateCropCategoryRequest;
import com.cropkeeper.crop.dto.request.UpdateCropCategoryRequest;
import com.cropkeeper.crop.service.CropCategoryService;
import com.cropkeeper.crop.dto.response.CropCategoryResponse;
import com.cropkeeper.global.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crop-categories")
@RequiredArgsConstructor
public class CropCategoryController {

    private final CropCategoryService cropCategoryService;

    /**
     * 작물 카테고리 생성 API
     *
     * @param request       카테고리 생성 요청
     * @return 201 created, 생성된 카테고리 정보
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CropCategoryResponse> createCategory(
            @Valid @RequestBody CreateCropCategoryRequest request) {

        CropCategoryResponse response = cropCategoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * 전체 작물 카테고리 목록 조회 API
     *
     * @return 200 OK, 카테고리 목록
     */
    @GetMapping
    public ResponseEntity<List<CropCategoryResponse>> getAllCategories() {

        List<CropCategoryResponse> responses = cropCategoryService.getAllCategories();
        return ResponseEntity.ok(responses);
    }

    /**
     * 특정 작물 카테고리 조회 API (categoryId)
     * @param categoryId
     * @return 200 OK, 카테고리 정보
     */
    @GetMapping("/{categoryId}")
    public ResponseEntity<CropCategoryResponse> getCategoryById(@PathVariable Long categoryId) {

        CropCategoryResponse response = cropCategoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 작물 카테고리 조회 API (categoryName)
     *
     * @param categoryName 카테고리명
     * @return 200 OK, 카테고리 정보
     */
    @GetMapping("/name/{categoryName}")
    public ResponseEntity<CropCategoryResponse> getCategoryByName(@PathVariable String categoryName) {

        CropCategoryResponse response = cropCategoryService.getCategoryByName(categoryName);
        return ResponseEntity.ok(response);
    }


    /**
     * 작물 카테고리 수정 API (관리자 전용)
     * @param categoryId 카테고리 ID
     * @param request 카테고리 수정 요청
     * @return 200 ok, 수정된 카테고리 정보
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{categoryId}")
    public ResponseEntity<CropCategoryResponse> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody UpdateCropCategoryRequest request){

        CropCategoryResponse response = cropCategoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 작물 카테고리 삭제 API
     *
     * @param categoryId 카테고리 ID
     * @return
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId) {

        cropCategoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

}
