package com.cropkeeper.domain.crop.controller;

import com.cropkeeper.domain.crop.dto.request.CreateCropCategoryRequest;
import com.cropkeeper.domain.crop.dto.request.UpdateCropCategoryRequest;
import com.cropkeeper.domain.crop.dto.response.CropCategoryResponse;
import com.cropkeeper.domain.crop.service.CropCategoryService;
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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CropCategoryController {

    private final CropCategoryService cropCategoryService;

    /**
     * 작물 카테고리 생성 API
     *
     * @param userPrincipal 현재 로그인한 사용자 정보
     * @param request       카테고리 생성 요청
     * @return 201 created, 생성된 카테고리 정보
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CropCategoryResponse> createCategory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateCropCategoryRequest request) {

        CropCategoryResponse response = cropCategoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * 전체 작물 카테고리 목록 조회 API
     *
     * @param userPrincipal 현재 로그인한 사용자 정보
     * @return 200 OK, 카테고리 목록
     */
    @GetMapping
    public ResponseEntity<List<CropCategoryResponse>> getAllCategories(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<CropCategoryResponse> responses = cropCategoryService.getAllCategories();
        return ResponseEntity.ok(responses);
    }

    /**
     * 특정 작물 카테고리 조회 API (categoryId)
     * @param categoryId
     * @param userPrincipal
     * @return 200 OK, 카테고리 정보
     */
    @GetMapping("/{categoryId}")
    public ResponseEntity<CropCategoryResponse> getCategoryById(
            @PathVariable Long categoryId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        CropCategoryResponse response = cropCategoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 작물 카테고리 조회 API (categoryName)
     *
     * @param categoryName 카테고리명
     * @param userPrincipal 현재 로그인한 사용자 정보
     * @return 200 OK, 카테고리 정보
     */
    @GetMapping("/name/{categoryName}")
    public ResponseEntity<CropCategoryResponse> getCategoryByName(
            @PathVariable String categoryName,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        CropCategoryResponse response = cropCategoryService.getCategoryByName(categoryName);
        return ResponseEntity.ok(response);
    }


    /**
     * 작물 카테고리 수정 API (관리자 전용)
     * @param categoryId 카테고리 ID
     * @param userPrincipal 현재 로그인한 사용자 정보
     * @param request 카테고리 수정 요청
     * @return 200 ok, 수정된 카테고리 정보
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{categoryId}")
    public ResponseEntity<CropCategoryResponse> updateCategory(
            @PathVariable Long categoryId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateCropCategoryRequest request){

        CropCategoryResponse response = cropCategoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 작물 카테고리 삭제 API
     *
     * @param categoryId 카테고리 ID
     * @param userPrincipal 현재 로그인한 사용자 정보
     * @return
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long categoryId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        cropCategoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

}
