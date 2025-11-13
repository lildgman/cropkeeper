package com.cropkeeper.domain.pest.entity;

import com.cropkeeper.domain.farminglog.entity.FarmingLog;
import com.cropkeeper.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pest_control")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PestControl extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pest_ctrl_id")
    private Long pestCtrlId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_id", nullable = false)
    private FarmingLog farmingLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pesticide_id", nullable = false)
    private Pesticide pesticide;

    @Column(name = "used_amount_bottle", nullable = false)
    private Long usedAmountBottle;

    @Column(name = "used_water_liter", nullable = false)
    private Long usedWaterLiter;
}
