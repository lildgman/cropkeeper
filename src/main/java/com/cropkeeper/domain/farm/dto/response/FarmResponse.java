package com.cropkeeper.domain.farm.dto.response;

import com.cropkeeper.domain.farm.entity.Farm;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmResponse {

    private Long farmId;
    private String farmName;
    private String zipCode;
    private String street;
    private String detail;
    private Long farmSize;
    private Long memberId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FarmResponse from(Farm farm) {

        return FarmResponse.builder()
                .farmId(farm.getFarmId())
                .farmName(farm.getFarmName())
                .zipCode(farm.getAddress() != null ? farm.getAddress().getZipCode() : null)
                .street(farm.getAddress() != null ? farm.getAddress().getStreet() : null)
                .detail(farm.getAddress() != null ? farm.getAddress().getDetail() : null)
                .farmSize(farm.getFarmSize())
                .memberId(farm.getMember().getMemberId())
                .createdAt(farm.getCreatedAt())
                .updatedAt(farm.getUpdatedAt())
                .build();

    }
}
