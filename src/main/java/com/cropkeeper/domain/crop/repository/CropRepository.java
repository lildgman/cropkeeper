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

    /**
     * 특정 카테고리에 작물이 존재하는지 확인
     *
     * @param categoryId 카테고리 ID
     * @return 작물 존재 여부
     */
    boolean existsByCategoryCategoryId(Long categoryId);
}
