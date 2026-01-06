package com.cropkeeper.crop.controller;

import com.cropkeeper.crop.dto.request.CreateCropVarietyRequest;
import com.cropkeeper.crop.dto.response.CropVarietyResponse;
import com.cropkeeper.crop.service.CropVarietyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/crop-varieties")
@RequiredArgsConstructor
public class CropVarietyController {

    private final CropVarietyService cropVarietyService;

    /**
     * 품종 생성 API
     *
     * @param request 품종 생성 요청
     * @return 201 created, 생성된 품종 정보
     */
    @PostMapping
    public ResponseEntity<CropVarietyResponse> createCropVariety(
            @Valid @RequestBody CreateCropVarietyRequest request) {

        CropVarietyResponse response = cropVarietyService.createCropVariety(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);

    }

    @GetMapping
    public ResponseEntity<List<CropVarietyResponse>> getAllVarieties(
            @RequestParam(required = false) Long typeId) {

        List<CropVarietyResponse> responses;

        if (typeId != null) {
            responses = cropVarietyService.getCropVarietiesByTypeId(typeId);
        } else {
            responses = cropVarietyService.getAllCropVarieties();
        }

        return ResponseEntity.ok(responses);
    }
}
