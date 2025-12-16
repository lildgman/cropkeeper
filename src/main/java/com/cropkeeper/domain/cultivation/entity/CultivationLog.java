package com.cropkeeper.domain.cultivation.entity;

import com.cropkeeper.domain.crop.entity.CropVariety;
import com.cropkeeper.domain.farminglog.entity.FarmingLog;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "cultivation_log")
@DiscriminatorValue("CULTIVATION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class CultivationLog extends FarmingLog {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variety_id", nullable = false)
    private CropVariety variety;

    @Column(name = "planting_amount")
    private Long plantingAmount;

    // 재배기록 전용 편의 메서드
    public void updatePlantingAmount(Long plantingAmount) {
        this.plantingAmount = plantingAmount;
    }

    public void updateVariety(CropVariety variety) {
        this.variety = variety;
    }
}
