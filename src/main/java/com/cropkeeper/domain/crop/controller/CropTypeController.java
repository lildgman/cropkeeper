package com.cropkeeper.domain.crop.controller;

import com.cropkeeper.domain.crop.dto.request.CreateCropTypeRequest;
import com.cropkeeper.domain.crop.dto.request.UpdateCropTypeRequest;
import com.cropkeeper.domain.crop.dto.response.CropTypeResponse;
import com.cropkeeper.domain.crop.service.CropTypeService;
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
@RequestMapping("/api/crop-types")
@RequiredArgsConstructor
public class CropTypeController {

    private final CropTypeService cropTypeService;

    /**
     * 작물 생성 API
     * @param userPrincipal
     * @param request
     * @return
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CropTypeResponse> createCropType(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateCropTypeRequest request) {

        CropTypeResponse response = cropTypeService.createCropType(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    /**
     * 작물 리스트 조회
     * @param categoryId
     * @return
     */
    @GetMapping
    public ResponseEntity<List<CropTypeResponse>> getAllCropTypes(
            @RequestParam(required = false) Long categoryId) {

        List<CropTypeResponse> responses;

        if (categoryId != null) {
            responses = cropTypeService.getCropTypesByCategoryId(categoryId);
        } else {
            responses = cropTypeService.getAllCropTypes();
        }

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{typeId}")
    public ResponseEntity<CropTypeResponse> getCropTypeById(
            @PathVariable Long typeId) {

        CropTypeResponse response = cropTypeService.getCropTypeById(typeId);
        return ResponseEntity.ok(response);
    }

    /**
     * 작물 수정 API
     *
     * @param userPrincipal 인증된 사용자 (관리자)
     * @param typeId 작물 ID
     * @param request 수정 요청
     * @return 수정된 작물 응답
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{typeId}")
    public ResponseEntity<CropTypeResponse> updateCropType(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long typeId,
            @Valid @RequestBody UpdateCropTypeRequest request) {

        CropTypeResponse response = cropTypeService.updateCropType(typeId, request);
        return ResponseEntity.ok(response);
    }
}
