package com.cropkeeper.domain.cultivation.dto.response;

import com.cropkeeper.domain.cultivation.entity.CultivationLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CultivationLogResponse {

    private Long cultivationLogId;
    private Long farmId;
    private Long memberId;
    private Long varietyId;
    private String varietyName;
    private Long plantingAmount;

    // FarmingMetadata 필드들
    private LocalDateTime logDate;
    private String weather;
    private Long temperature;
    private Long humidity;
    private String memo;

    // Audit 필드
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity를 Response DTO로 변환하는 정적 팩토리 메서드
     */
    public static CultivationLogResponse from(CultivationLog log) {
        return CultivationLogResponse.builder()
                .cultivationLogId(log.getFarmingLogId())
                .farmId(log.getFarm().getFarmId())
                .memberId(log.getMember().getMemberId())
                .varietyId(log.getVariety().getVarietyId())
                .varietyName(log.getVariety().getVarietyName())
                .plantingAmount(log.getPlantingAmount())
                .logDate(log.getMetadata() != null ? log.getMetadata().getLogDate() : null)
                .weather(log.getMetadata() != null ? log.getMetadata().getWeather() : null)
                .temperature(log.getMetadata() != null ? log.getMetadata().getTemperature() : null)
                .humidity(log.getMetadata() != null ? log.getMetadata().getHumidity() : null)
                .memo(log.getMetadata() != null ? log.getMetadata().getMemo() : null)
                .createdAt(log.getCreatedAt())
                .updatedAt(log.getUpdatedAt())
                .build();
    }
}
