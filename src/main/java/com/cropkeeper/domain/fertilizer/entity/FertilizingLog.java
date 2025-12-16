package com.cropkeeper.domain.fertilizer.entity;

import com.cropkeeper.domain.farminglog.entity.FarmingLog;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "fertilizing_log")
@DiscriminatorValue("FERTILIZING")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class FertilizingLog extends FarmingLog {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fertilizer_id", nullable = false)
    private Fertilizer fertilizer;

    @Column(name = "used_amount_bag", nullable = false)
    private Long usedAmountBag;

    // 시비기록 전용 편의 메서드
    public void updateFertilizer(Fertilizer fertilizer) {
        this.fertilizer = fertilizer;
    }

    public void updateUsedAmountBag(Long usedAmountBag) {
        this.usedAmountBag = usedAmountBag;
    }
}
