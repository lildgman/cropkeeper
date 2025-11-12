package com.cropkeeper.domain.irrigation.entity;

import com.cropkeeper.domain.farminglog.entity.FarmingLog;
import com.cropkeeper.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "irrigation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Irrigation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "irrigation_id")
    private Long irrigationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_id", nullable = false)
    private FarmingLog farmingLog;

    @Column(name = "water_amount_liter", nullable = false)
    private Long waterAmountLiter;
}
