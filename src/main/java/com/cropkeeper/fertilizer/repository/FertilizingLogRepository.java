package com.cropkeeper.fertilizer.repository;

import com.cropkeeper.farm.entity.Farm;
import com.cropkeeper.fertilizer.entity.FertilizingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FertilizingLogRepository extends JpaRepository<FertilizingLog, Long> {

    List<FertilizingLog> findByFarm(Farm farm);

    List<FertilizingLog> findByFarmAndMetadata_LogDateBetween(Farm farm, LocalDateTime startDate, LocalDateTime endDate);
}
