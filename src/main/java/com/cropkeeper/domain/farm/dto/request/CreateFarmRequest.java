package com.cropkeeper.domain.farm.dto.request;

import com.cropkeeper.domain.farm.vo.Address;
import jakarta.validation.constraints.Min;
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
public class CreateFarmRequest {

    @NotBlank(message = "농장 이름은 필수입니다.")
    @Size(max = 100, message = "농장 이름은 100자 이하여야 합니다.")
    private String farmName;

    @Size(max = 10, message = "우편번호는 10자 이하여야 합니다.")
    private String zipCode;

    @NotBlank(message = "주소는 필수입니다.")
    @Size(max = 200, message = "주소는 200자 이하여야 합니다.")
    private String street;

    @Size(max = 100, message = "상세주소는 100자 이하여야 합니다.")
    private String detail;

    @NotNull(message = "농장 크기는 필수입니다.")
    @Min(value = 1, message = "농장 크기는 1 이상이어야 합니다.")
    private Long farmSize;

    public Address toAddress() {
        return Address.builder()
                .zipCode(zipCode)
                .street(street)
                .detail(detail)
                .build();
    }
}
