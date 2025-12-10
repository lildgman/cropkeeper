package com.cropkeeper.domain.cultivation.repository;

import com.cropkeeper.domain.cultivation.entity.CultivationLog;
import com.cropkeeper.domain.farm.entity.Farm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CultivationLogRepository extends JpaRepository<CultivationLog, Long> {

    /**
     * 특정 농장의 모든 재배기록 조회
     */
    List<CultivationLog> findByFarm_FarmId(Long farmId);

    /**
     * 특정 농장의 기간별 재배기록 조회
     */
    List<CultivationLog> findByFarm_FarmIdAndMetadata_LogDateBetween(
            Long farmId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * 특정 농장의 품종별 재배기록 조회
     */
    List<CultivationLog> findByFarm_FarmIdAndVariety_VarietyId(Long farmId, Long varietyId);
}
