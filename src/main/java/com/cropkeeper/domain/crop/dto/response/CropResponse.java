package com.cropkeeper.domain.crop.dto.response;

import com.cropkeeper.domain.crop.entity.CropType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 작물 응답 DTO
 *
 * 작물 정보를 클라이언트에게 반환합니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropResponse {

    private Long cropId;
    private String cropName;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdAt;

    /**
     * 엔티티로부터 DTO 생성 (팩토리 메서드)
     *
     * @param cropType 작물 엔티티
     * @return 작물 응답 DTO
     */
    public static CropResponse from(CropType cropType) {
        return CropResponse.builder()
                .cropId(cropType.getTypeId())
                .cropName(cropType.getTypeName())
                .categoryId(cropType.getCategory().getCategoryId())
                .categoryName(cropType.getCategory().getCategoryName())
                .createdAt(cropType.getCreatedAt())
                .build();
    }
}
