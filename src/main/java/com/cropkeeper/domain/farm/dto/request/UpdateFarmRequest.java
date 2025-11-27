package com.cropkeeper.domain.farm.dto.request;

import com.cropkeeper.domain.farm.vo.Address;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateFarmRequest {

    @Size(max = 20, message = "농장 이름은 20자 이하여야 합니다.")
    private String farmName;

    @Size(max = 10, message = "우편번호는 10자 이하여야 합니다.")
    private String zipCode;

    @Size(max = 100, message = "주소는 100자 이하여야 합니다.")
    private String street;

    @Size(max = 100, message = "상세주소는 100자 이하여야 합니다.")
    private String detail;

    @Min(value = 1, message = "농장 크기는 1 이상이어야 합니다.")
    private Long farmSize;

    public boolean hasAtLeastOneField() {
        return (farmName != null && !farmName.isEmpty()) ||
                (zipCode != null && !zipCode.isEmpty()) ||
                (street != null && !street.isEmpty()) ||
                (detail != null && !detail.isEmpty()) ||
                (farmSize != null && farmSize > 0);
    }

    public Address toAddress() {
        if (zipCode == null && street == null && detail == null) {
            return null;
        }
        return Address.builder()
                .zipCode(zipCode)
                .street(street)
                .detail(detail)
                .build();
    }

}
