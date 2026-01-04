package com.cropkeeper.crop.controller;

import com.cropkeeper.crop.dto.request.CreateCropVarietyRequest;
import com.cropkeeper.crop.dto.response.CropVarietyResponse;
import com.cropkeeper.crop.service.CropVarietyService;
import com.cropkeeper.global.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/crop-varieties")
@RequiredArgsConstructor
public class CropVarietyController {

    private final CropVarietyService cropVarietyService;

    @PostMapping
    public ResponseEntity<CropVarietyResponse> createCropVariety(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateCropVarietyRequest request) {

        CropVarietyResponse response = cropVarietyService.createCropVariety(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);

    }
}
