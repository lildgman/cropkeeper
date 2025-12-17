package com.cropkeeper.domain.crop.repository;

import com.cropkeeper.domain.crop.entity.CropVariety;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CropVarietyRepository extends JpaRepository<CropVariety, Long> {

    @Query("SELECT cv FROM CropVariety cv WHERE cv.crop.cropId = :cropId AND cv.varietyName = :varietyName")
    Optional<CropVariety> findByCrop_CropIdAndVarietyName(@Param("cropId") Long cropId, @Param("varietyName") String varietyName);
}
