package com.cropkeeper.domain.pest.repository;

import com.cropkeeper.domain.farm.entity.Farm;
import com.cropkeeper.domain.pest.entity.PestControlLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PestControlLogRepository extends JpaRepository<PestControlLog, Long> {

    List<PestControlLog> findByFarm(Farm farm);

    List<PestControlLog> findByFarmAndMetadata_LogDateBetween(Farm farm, LocalDateTime startDate, LocalDateTime endDate);
}
