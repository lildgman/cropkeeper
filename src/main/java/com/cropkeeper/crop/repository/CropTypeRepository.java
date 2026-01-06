package com.cropkeeper.crop.repository;

import com.cropkeeper.crop.entity.CropType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CropTypeRepository extends JpaRepository<CropType, Long> {

    @Query("SELECT ct " +
            "FROM CropType ct " +
            "WHERE ct.typeName = :typeName " +
            "AND ct.deleted = false")
    Optional<CropType> findByCropTypeName(@Param("typeName") String typeName);

    @Query("SELECT ct " +
            "FROM CropType ct " +
            "JOIN FETCH ct.category " +
            "WHERE ct.typeId = :typeId " +
            "AND ct.deleted = false")
    Optional<CropType> findById(@Param("typeId") Long typeId);

    @Query("SELECT " +
            "CASE WHEN COUNT(ct) > 0 " +
            "THEN true ELSE false " +
            "END " +
            "FROM CropType ct " +
            "WHERE ct.category.categoryId = :categoryId " +
            "AND ct.deleted = false")
    boolean existsByCategoryCategoryId(Long categoryId);

    @Query("SELECT " +
            "CASE WHEN COUNT(ct) > 0 " +
            "THEN true ELSE false END " +
            "FROM CropType ct " +
            "WHERE ct.typeName = :typeName " +
            "AND ct.deleted = false")
    boolean existsByTypeNameAndDeletedFalse(@Param("typeName") String typeName);

    @Query("SELECT ct " +
            "FROM CropType ct " +
            "JOIN FETCH ct.category " +
            "WHERE ct.deleted = false")
    List<CropType> findAllByDeletedFalse();

    @Query("SELECT ct " +
            "FROM CropType ct " +
            "JOIN FETCH ct.category " +
            "WHERE ct.category.categoryId = :categoryId " +
            "AND ct.deleted = false")
    List<CropType> findByCategoryCategoryIdAndDeletedFalse(Long categoryId);

    boolean existsByTypeIdAndDeletedFalse(@Param("typeId") Long typeId);

}


