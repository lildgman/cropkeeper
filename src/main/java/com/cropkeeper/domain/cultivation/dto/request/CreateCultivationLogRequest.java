package com.cropkeeper.domain.cultivation.dto.request;

import com.cropkeeper.domain.farminglog.vo.FarmingMetadata;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCultivationLogRequest {

    @NotNull(message = "작물 품종 ID는 필수입니다.")
    private Long varietyId;

    @NotNull(message = "재배량은 필수입니다.")
    @Min(value = 1, message = "재배량은 1 이상이어야 합니다.")
    private Long plantingAmount;

    // FarmingMetadata 필드들
    @NotNull(message = "기록 날짜는 필수입니다.")
    private LocalDateTime logDate;

    @NotBlank(message = "날씨 정보는 필수입니다.")
    @Size(max = 50, message = "날씨 정보는 50자 이하여야 합니다.")
    private String weather;

    private Long temperature;

    private Long humidity;

    @Size(max = 255, message = "메모는 255자 이하여야 합니다.")
    private String memo;

    /**
     * Request DTO를 FarmingMetadata VO로 변환
     */
    public FarmingMetadata toMetadata() {
        return FarmingMetadata.builder()
                .logDate(logDate)
                .weather(weather)
                .temperature(temperature)
                .humidity(humidity)
                .memo(memo)
                .build();
    }
}
