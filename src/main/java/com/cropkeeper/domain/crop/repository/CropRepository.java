package com.cropkeeper.domain.crop.repository;

import com.cropkeeper.domain.crop.entity.Crop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CropRepository extends JpaRepository<Crop, Long> {

    @Query("SELECT c FROM Crop c WHERE c.cropName = :cropName")
    Optional<Crop> findByCropName(@Param("cropName") String cropName);
}
