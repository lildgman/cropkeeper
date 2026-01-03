package com.cropkeeper.crop.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 작물(중분류) 수정 요청 DTO
 *
 * 부분 업데이트를 지원하며, 이름과 카테고리를 독립적으로 변경 가능합니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCropTypeRequest {

    @Size(max = 20, message = "작물명은 20자 이하여야 합니다.")
    private String typeName;

    private Long categoryId;
}
