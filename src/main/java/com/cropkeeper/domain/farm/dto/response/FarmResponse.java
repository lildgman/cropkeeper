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
    private String address;
    private Long farmSize;
    private Long memberId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FarmResponse from(Farm farm) {

        return FarmResponse.builder()
                .farmId(farm.getFarmId())
                .farmName(farm.getFarmName())
                .farmSize(farm.getFarmSize())
                .memberId(farm.getMember().getMemberId())
                .createdAt(farm.getCreatedAt())
                .updatedAt(farm.getUpdatedAt())
                .build();

    }
}
