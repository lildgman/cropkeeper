package com.cropkeeper.domain.crop.dto.response;

import com.cropkeeper.domain.crop.entity.CropCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 작물 카테고리 응답 DTO
 *
 * 작물 카테고리 정보를 클라이언트에게 반환합니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropCategoryResponse {

    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdAt;

    /**
     * 엔티티로부터 DTO 생성 (팩토리 메서드)
     *
     * @param category 작물 카테고리 엔티티
     * @return 작물 카테고리 응답 DTO
     */
    public static CropCategoryResponse from(CropCategory category) {
        return CropCategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
