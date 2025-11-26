package com.cropkeeper.domain.irrigation.repository;

import com.cropkeeper.domain.farm.entity.Farm;
import com.cropkeeper.domain.irrigation.entity.IrrigationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IrrigationLogRepository extends JpaRepository<IrrigationLog, Long> {

    List<IrrigationLog> findByFarm(Farm farm);

    List<IrrigationLog> findByFarmAndMetadata_LogDateBetween(Farm farm, LocalDateTime startDate, LocalDateTime endDate);
}
