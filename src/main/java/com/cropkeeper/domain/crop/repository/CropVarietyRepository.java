package com.cropkeeper.domain.crop.repository;

import com.cropkeeper.domain.crop.entity.CropVariety;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CropVarietyRepository extends JpaRepository<CropVariety, Long> {

    @Query("SELECT cv FROM CropVariety cv WHERE cv.cropType.typeId = :typeId AND cv.varietyName = :varietyName AND cv.deleted = false")
    Optional<CropVariety> findByCrop_CropIdAndVarietyName(@Param("typeId") Long typeId, @Param("varietyName") String varietyName);

    @Query("SELECT cv FROM CropVariety cv WHERE cv.varietyId = :varietyId AND cv.deleted = false")
    Optional<CropVariety> findById(@Param("varietyId") Long varietyId);

    /**
     * 특정 작물에 품종이 존재하는지 확인 (삭제되지 않은 품종만)
     *
     * @param typeId 작물 ID
     * @return 품종 존재 여부
     */
    @Query("SELECT CASE WHEN COUNT(cv) > 0 THEN true ELSE false END FROM CropVariety cv WHERE cv.cropType.typeId = :typeId AND cv.deleted = false")
    boolean existsByCropTypeTypeIdAndDeletedFalse(@Param("typeId") Long typeId);
}
