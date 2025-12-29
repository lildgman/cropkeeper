package com.cropkeeper.domain.crop.repository;

import com.cropkeeper.domain.crop.entity.CropType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CropTypeRepository extends JpaRepository<CropType, Long> {

    @Query("SELECT ct FROM CropType ct WHERE ct.typeName = :typeName AND ct.deleted = false")
    Optional<CropType> findByCropTypeName(@Param("typeName") String typeName);

    @Query("SELECT ct FROM CropType ct WHERE ct.typeId = :typeId AND ct.deleted = false")
    Optional<CropType> findById(@Param("typeId") Long typeId);

    /**
     * 특정 카테고리에 작물이 존재하는지 확인
     *
     * @param categoryId 카테고리 ID
     * @return 작물 존재 여부
     */
    @Query("SELECT CASE WHEN COUNT(ct) > 0 THEN true ELSE false END FROM CropType ct WHERE ct.category.categoryId = :categoryId AND ct.deleted = false")
    boolean existsByCategoryCategoryId(Long categoryId);

    @Query("SELECT ct FROM CropType ct WHERE ct.deleted = false")
    List<CropType> findAllByDeletedFalse();

    /**
     * 특정 카테고리에 속한 작물 목록 조회
     * @param categoryId 카테고리 ID
     * @return 작물 목록
     */
    @Query("SELECT ct FROM CropType ct WHERE ct.category.categoryId = :categoryId AND ct.deleted = false")
    List<CropType> findByCategoryCategoryIdAndDeletedFalse(Long categoryId);
}
