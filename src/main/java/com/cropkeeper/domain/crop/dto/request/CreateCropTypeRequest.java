package com.cropkeeper.domain.crop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCropTypeRequest {

    @NotBlank(message = "작물명은 필수입니다.")
    @Size(max = 20, message = "작물명은 20자 이하여야 합니다.")
    private String typeName;

    @NotNull(message = "카테고리 ID는 필수입니다.")
    private Long categoryId;
}
