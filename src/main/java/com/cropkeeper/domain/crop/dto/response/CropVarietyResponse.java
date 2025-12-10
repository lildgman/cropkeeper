package com.cropkeeper.domain.crop.dto.response;

import com.cropkeeper.domain.crop.entity.CropVariety;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 품종 응답 DTO
 *
 * 품종 정보를 클라이언트에게 반환합니다.
 * 작물 및 카테고리 정보도 함께 포함합니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropVarietyResponse {

    private Long varietyId;
    private String varietyName;
    private Long cropId;
    private String cropName;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdAt;

    /**
     * 엔티티로부터 DTO 생성 (팩토리 메서드)
     *
     * @param variety 품종 엔티티
     * @return 품종 응답 DTO
     */
    public static CropVarietyResponse from(CropVariety variety) {
        return CropVarietyResponse.builder()
                .varietyId(variety.getVarietyId())
                .varietyName(variety.getVarietyName())
                .cropId(variety.getCrop().getCropId())
                .cropName(variety.getCrop().getCropName())
                .categoryId(variety.getCrop().getCategory().getCategoryId())
                .categoryName(variety.getCrop().getCategory().getCategoryName())
                .createdAt(variety.getCreatedAt())
                .build();
    }
}
