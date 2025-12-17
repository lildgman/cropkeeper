package com.cropkeeper.domain.crop.repository;

import com.cropkeeper.domain.crop.entity.CropVariety;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CropVarietyRepository extends JpaRepository<CropVariety, Long> {

    /**
     * 특정 작물의 품종명으로 품종 조회
     *
     * 같은 작물 내에서 품종명은 유니크합니다.
     * 예: "토마토" 작물 내에 "방울토마토" 품종은 하나만 존재
     *
     * @param cropId 작물 ID
     * @param varietyName 품종명
     * @return 품종 (Optional)
     */
    @Query("SELECT cv FROM CropVariety cv WHERE cv.crop.cropId = :cropId AND cv.varietyName = :varietyName")
    Optional<CropVariety> findByCrop_CropIdAndVarietyName(@Param("cropId") Long cropId, @Param("varietyName") String varietyName);
}
