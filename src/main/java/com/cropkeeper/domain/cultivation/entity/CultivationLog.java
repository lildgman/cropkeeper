package com.cropkeeper.domain.cultivation.entity;

import com.cropkeeper.domain.crop.entity.CropVariety;
import com.cropkeeper.domain.farm.entity.Farm;
import com.cropkeeper.domain.farminglog.vo.FarmingMetadata;
import com.cropkeeper.domain.member.entity.Member;
import com.cropkeeper.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cultivation_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CultivationLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cultivation_log_id")
    private Long cultivationLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @Embedded
    private FarmingMetadata metadata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variety_id", nullable = false)
    private CropVariety variety;

    @Column(name = "planting_amount")
    private Long plantingAmount;
}
