package com.cropkeeper.domain.crop.repository;

import com.cropkeeper.domain.crop.entity.CropCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CropCategoryRepository extends JpaRepository<CropCategory, Long> {

    @Query("SELECT cc FROM CropCategory cc WHERE cc.categoryName = :categoryName AND cc.deleted = false")
    Optional<CropCategory> findByCategoryName(@Param("categoryName") String categoryName);

    @Query("SELECT cc FROM CropCategory cc WHERE cc.categoryId = :categoryId AND cc.deleted = false")
    Optional<CropCategory> findById(@Param("categoryId") Long categoryId);

    @Query("SELECT CASE WHEN COUNT(cc) > 0 THEN true ELSE false END FROM CropCategory cc WHERE cc.categoryName = :categoryName AND cc.deleted = false")
    boolean existsByCategoryNameAndDeletedFalse(@Param("categoryName") String categoryName);
}
