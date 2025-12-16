package com.cropkeeper.domain.irrigation.entity;

import com.cropkeeper.domain.farminglog.entity.FarmingLog;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "irrigation_log")
@DiscriminatorValue("IRRIGATION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class IrrigationLog extends FarmingLog {

    @Column(name = "water_amount_liter", nullable = false)
    private Long waterAmountLiter;

    // 관수기록 전용 편의 메서드
    public void updateWaterAmountLiter(Long waterAmountLiter) {
        this.waterAmountLiter = waterAmountLiter;
    }
}
