package com.cropkeeper.crop.repository;

import com.cropkeeper.crop.entity.CropVariety;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CropVarietyRepository extends JpaRepository<CropVariety, Long> {

    @Query("SELECT cv " +
            "FROM CropVariety cv " +
            "WHERE cv.cropType.typeId = :typeId " +
            "AND cv.varietyName = :varietyName " +
            "AND cv.deleted = false")
    Optional<CropVariety> findByCropCropIdAndVarietyName(@Param("typeId") Long typeId, @Param("varietyName") String varietyName);

    @Query("SELECT cv " +
            "FROM CropVariety cv " +
            "WHERE cv.varietyId = :varietyId " +
            "AND cv.deleted = false")
    Optional<CropVariety> findById(@Param("varietyId") Long varietyId);

    @Query("SELECT " +
            "CASE WHEN COUNT(cv) > 0 " +
            "THEN true " +
            "ELSE false " +
            "END " +
            "FROM CropVariety cv " +
            "WHERE cv.cropType.typeId = :typeId " +
            "AND cv.deleted = false")
    boolean existsByCropTypeTypeIdAndDeletedFalse(@Param("typeId") Long typeId);

    @Query("SELECT " +
            "CASE WHEN COUNT(cv) > 0 " +
            "THEN true " +
            "ELSE false " +
            "END " +
            "FROM CropVariety cv " +
            "WHERE cv.varietyName = :varietyName " +
            "AND cv.deleted = false")
    boolean existsByVarietyNameAndDeletedFalse(@Param("varietyName") String varietyName);

    @Query("SELECT cv " +
            "FROM CropVariety cv " +
            "JOIN FETCH cv.cropType ct " +
            "JOIN FETCH ct.category " +
            "WHERE cv.deleted = false")
    List<CropVariety> findAllByDeletedFalse();
}
