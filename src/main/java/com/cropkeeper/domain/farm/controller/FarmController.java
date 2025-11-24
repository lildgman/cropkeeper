package com.cropkeeper.domain.farm.controller;

import com.cropkeeper.domain.farm.annotation.ValidateFarmAccess;
import com.cropkeeper.domain.farm.dto.request.CreateFarmRequest;
import com.cropkeeper.domain.farm.dto.request.UpdateFarmRequest;
import com.cropkeeper.domain.farm.dto.response.FarmResponse;
import com.cropkeeper.domain.farm.service.FarmService;
import com.cropkeeper.global.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farms")
@RequiredArgsConstructor
public class FarmController {

    private final FarmService farmService;

    /**
     * 농장 생성 API
     *
     * @param userPrincipal 현재 로그인한 사용자 정보
     * @param request       농장 생성 요청
     * @return 201 Created + 생성된 농장 정보
     */
    @PostMapping
    public ResponseEntity<FarmResponse> createFarm(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateFarmRequest request) {

        Long memberId = userPrincipal.getId();
        FarmResponse response = farmService.createFarm(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 내 농장 목록 조회 API
     *
     * @param userPrincipal 현재 로그인한 사용자 정보
     * @return 200 OK + 농장 목록
     */
    @GetMapping
    public ResponseEntity<List<FarmResponse>> getMyFarms(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long memberId = userPrincipal.getId();
        List<FarmResponse> response = farmService.findAllByMemberId(memberId);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 농장 조회 API
     *
     * @param farmId        농장 ID
     * @param userPrincipal 현재 로그인한 사용자 정보
     * @return 200 OK + 농장 정보
     */
    @ValidateFarmAccess(action = "농장 조회")
    @GetMapping("/{farmId}")
    public ResponseEntity<FarmResponse> getFarm(
            @PathVariable Long farmId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        FarmResponse response = farmService.getFarmInfo(farmId);
        return ResponseEntity.ok(response);
    }

    /**
     * 농장 정보 수정 API
     *
     * @param farmId        농장 ID
     * @param userPrincipal 현재 로그인한 사용자 정보
     * @param request       수정할 농장 정보
     * @return 200 OK + 수정된 농장 정보
     */
    @ValidateFarmAccess(action = "농장 수정")
    @PutMapping("/{farmId}")
    public ResponseEntity<FarmResponse> updateFarm(
            @PathVariable Long farmId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateFarmRequest request) {

        FarmResponse response = farmService.updateFarm(farmId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 농장 삭제 API
     *
     * @param farmId        농장 ID
     * @param userPrincipal 현재 로그인한 사용자 정보
     * @return 204 No Content
     */
    @ValidateFarmAccess(action = "농장 삭제")
    @DeleteMapping("/{farmId}")
    public ResponseEntity<Void> deleteFarm(
            @PathVariable Long farmId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        farmService.deleteFarm(farmId);
        return ResponseEntity.noContent().build();
    }
}
