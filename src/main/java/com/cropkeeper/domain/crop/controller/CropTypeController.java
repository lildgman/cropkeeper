package com.cropkeeper.domain.crop.controller;

import com.cropkeeper.domain.crop.dto.request.CreateCropTypeRequest;
import com.cropkeeper.domain.crop.dto.response.CropTypeResponse;
import com.cropkeeper.domain.crop.service.CropTypeService;
import com.cropkeeper.global.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crop-types")
@RequiredArgsConstructor
public class CropTypeController {

    private final CropTypeService cropTypeService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CropTypeResponse> createCropType(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateCropTypeRequest request) {

        CropTypeResponse response = cropTypeService.createCropType(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }
}
