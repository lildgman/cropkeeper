package com.cropkeeper.domain.cultivation.repository;

import com.cropkeeper.domain.cultivation.entity.CultivationLog;
import com.cropkeeper.domain.farm.entity.Farm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CultivationLogRepository extends JpaRepository<CultivationLog, Long> {

    List<CultivationLog> findByFarm(Farm farm);

    List<CultivationLog> findByFarmAndMetadata_LogDateBetween(Farm farm, LocalDateTime startDate, LocalDateTime endDate);
}
