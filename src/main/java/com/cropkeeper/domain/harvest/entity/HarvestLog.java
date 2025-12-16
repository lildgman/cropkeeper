package com.cropkeeper.domain.harvest.entity;

import com.cropkeeper.domain.crop.entity.CropVariety;
import com.cropkeeper.domain.farminglog.entity.FarmingLog;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "harvest_log")
@DiscriminatorValue("HARVEST")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class HarvestLog extends FarmingLog {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variety_id", nullable = false)
    private CropVariety variety;

    @Column(name = "quantity_box", nullable = false)
    private Long quantityBox;

    // 수확기록 전용 편의 메서드
    public void updateVariety(CropVariety variety) {
        this.variety = variety;
    }

    public void updateQuantityBox(Long quantityBox) {
        this.quantityBox = quantityBox;
    }
}
