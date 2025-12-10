package com.cropkeeper.domain.crop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 품종 생성 요청 DTO
 *
 * 새로운 품종을 생성하기 위한 정보를 담습니다.
 * 카테고리 → 작물 → 품종 순서로 계층 구조를 가집니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCropVarietyRequest {

    @NotNull(message = "카테고리 ID는 필수입니다.")
    private Long categoryId;

    @NotBlank(message = "작물명은 필수입니다.")
    @Size(max = 50, message = "작물명은 50자 이하여야 합니다.")
    private String cropName;

    @NotBlank(message = "품종명은 필수입니다.")
    @Size(max = 50, message = "품종명은 50자 이하여야 합니다.")
    private String varietyName;
}
