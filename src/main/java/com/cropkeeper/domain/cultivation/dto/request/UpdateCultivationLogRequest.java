package com.cropkeeper.domain.cultivation.dto.request;

import com.cropkeeper.domain.farminglog.vo.FarmingMetadata;
import jakarta.validation.constraints.Min;
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
public class UpdateCultivationLogRequest {

    private Long varietyId;

    @Min(value = 1, message = "재배량은 1 이상이어야 합니다.")
    private Long plantingAmount;

    // FarmingMetadata 필드들
    private LocalDateTime logDate;

    @Size(max = 50, message = "날씨 정보는 50자 이하여야 합니다.")
    private String weather;

    private Long temperature;

    private Long humidity;

    @Size(max = 255, message = "메모는 255자 이하여야 합니다.")
    private String memo;

    /**
     * 최소한 하나의 수정할 필드가 있는지 확인
     */
    public boolean hasAtLeastOneField() {
        return varietyId != null
                || plantingAmount != null
                || logDate != null
                || (weather != null && !weather.isEmpty())
                || temperature != null
                || humidity != null
                || memo != null;
    }

    /**
     * 메타데이터 필드가 하나라도 있는지 확인
     */
    public boolean hasMetadataFields() {
        return logDate != null
                || (weather != null && !weather.isEmpty())
                || temperature != null
                || humidity != null
                || memo != null;
    }

    /**
     * 기존 메타데이터를 기반으로 업데이트된 FarmingMetadata 생성
     */
    public FarmingMetadata toUpdatedMetadata(FarmingMetadata existing) {
        return FarmingMetadata.builder()
                .logDate(logDate != null ? logDate : existing.getLogDate())
                .weather((weather != null && !weather.isEmpty()) ? weather : existing.getWeather())
                .temperature(temperature != null ? temperature : existing.getTemperature())
                .humidity(humidity != null ? humidity : existing.getHumidity())
                .memo(memo != null ? memo : existing.getMemo())
                .build();
    }
}
