package com.cropkeeper.domain.crop.repository;

import com.cropkeeper.domain.crop.entity.CropCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CropCategoryRepository extends JpaRepository<CropCategory, Long> {

    @Query("SELECT cc FROM CropCategory cc WHERE cc.categoryName = :categoryName")
    Optional<CropCategory> findByCategoryName(@Param("categoryName") String categoryName);
}
