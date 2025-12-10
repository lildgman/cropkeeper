package com.cropkeeper.domain.crop.repository;

import com.cropkeeper.domain.crop.entity.Crop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CropRepository extends JpaRepository<Crop, Long> {

    Optional<Crop> findByCropName(String cropName);
}
