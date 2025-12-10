package com.cropkeeper.domain.cultivation.entity;

import com.cropkeeper.domain.crop.entity.CropVariety;
import com.cropkeeper.domain.farm.entity.Farm;
import com.cropkeeper.domain.farminglog.vo.FarmingMetadata;
import com.cropkeeper.domain.member.entity.Member;
import com.cropkeeper.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 편의 메서드: 재배기록 정보 수정
    public void updatePlantingAmount(Long plantingAmount) {
        this.plantingAmount = plantingAmount;
    }

    public void updateMetadata(FarmingMetadata metadata) {
        this.metadata = metadata;
    }

    public void updateVariety(CropVariety variety) {
        this.variety = variety;
    }

    // 재배기록 삭제 처리 (Soft Delete)
    public void delete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    // 삭제 여부 확인
    public boolean isDeleted() {
        return this.deleted;
    }
}
