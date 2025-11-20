package com.cropkeeper.domain.farm.repository;

import com.cropkeeper.domain.farm.entity.Farm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FarmRepository extends JpaRepository<Farm, Long> {

    @Query("SELECT f FROM Farm f WHERE f.member.memberId = :memberId")
    List<Farm> findByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT f FROM Farm f WHERE f.farmId = :farmId AND f.member.memberId = :memberId")
    Optional<Farm> findByFarmIdAndMemberId(
            @Param("farmId") Long farmId,
            @Param("memberId") Long memberId);
}
