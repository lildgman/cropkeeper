package com.cropkeeper.domain.harvest.entity;

import com.cropkeeper.domain.crop.entity.CropVariety;
import com.cropkeeper.domain.farm.entity.Farm;
import com.cropkeeper.domain.farminglog.vo.FarmingMetadata;
import com.cropkeeper.domain.member.entity.Member;
import com.cropkeeper.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "harvest_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class HarvestLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "harvest_log_id")
    private Long harvestLogId;

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

    @Column(name = "quantity_box", nullable = false)
    private Long quantityBox;
}
