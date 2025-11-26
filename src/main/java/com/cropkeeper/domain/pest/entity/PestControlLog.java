package com.cropkeeper.domain.pest.entity;

import com.cropkeeper.domain.farm.entity.Farm;
import com.cropkeeper.domain.farminglog.vo.FarmingMetadata;
import com.cropkeeper.domain.member.entity.Member;
import com.cropkeeper.global.common.BaseTimeEntity;
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
