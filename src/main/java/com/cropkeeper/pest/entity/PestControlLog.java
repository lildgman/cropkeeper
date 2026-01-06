package com.cropkeeper.pest.entity;

import com.cropkeeper.farm.entity.Farm;
import com.cropkeeper.farminglog.vo.FarmingMetadata;
import com.cropkeeper.member.entity.Member;
import com.cropkeeper.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pest_control_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PestControlLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pest_control_log_id")
    private Long pestControlLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @Embedded
    private FarmingMetadata metadata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pesticide_id", nullable = false)
    private Pesticide pesticide;

    @Column(name = "used_amount_bottle", nullable = false)
    private Long usedAmountBottle;

    @Column(name = "used_water_liter", nullable = false)
    private Long usedWaterLiter;
}
