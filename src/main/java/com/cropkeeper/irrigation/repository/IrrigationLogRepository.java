package com.cropkeeper.irrigation.repository;

import com.cropkeeper.farm.entity.Farm;
import com.cropkeeper.irrigation.entity.IrrigationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IrrigationLogRepository extends JpaRepository<IrrigationLog, Long> {

    List<IrrigationLog> findByFarm(Farm farm);

    List<IrrigationLog> findByFarmAndMetadata_LogDateBetween(Farm farm, LocalDateTime startDate, LocalDateTime endDate);
}
