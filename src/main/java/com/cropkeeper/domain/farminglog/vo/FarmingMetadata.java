package com.cropkeeper.domain.farminglog.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.LocalDateTime;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FarmingMetadata {

    @Column(name = "log_date", nullable = false)
    private LocalDateTime logDate;

    @Column(name = "weather", nullable = false, length = 50)
    private String weather;

    @Column(name = "temperature")
    private Long temperature;

    @Column(name = "humidity")
    private Long humidity;

    @Column(name = "memo", length = 255)
    private String memo;
}
