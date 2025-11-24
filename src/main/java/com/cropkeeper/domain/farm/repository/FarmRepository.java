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

    /**
     * ID로 농장 조회 (삭제되지 않은 농장만)
     * @param farmId 농장 ID
     * @return 농장 정보 (삭제되지 않은 경우)
     */
    @Query("SELECT f FROM Farm f WHERE f.farmId = :farmId AND f.deleted = false")
    Optional<Farm> findById(@Param("farmId") Long farmId);

    /**
     * 회원 ID로 농장 목록 조회 (삭제되지 않은 농장만)
     * @param memberId 회원 ID
     * @return 농장 목록 (삭제되지 않은 농장만)
     */
    @Query("SELECT f FROM Farm f JOIN FETCH f.member WHERE f.member.memberId = :memberId AND f.deleted = false")
    List<Farm> findByMemberId(@Param("memberId") Long memberId);

    /**
     * 농장 ID와 회원 ID로 농장 조회 (삭제되지 않은 농장만)
     * @param farmId 농장 ID
     * @param memberId 회원 ID
     * @return 농장 정보 (삭제되지 않은 경우)
     */
    @Query("SELECT f FROM Farm f JOIN FETCH f.member WHERE f.farmId = :farmId AND f.member.memberId = :memberId AND f.deleted = false")
    Optional<Farm> findByFarmIdAndMemberId(
            @Param("farmId") Long farmId,
            @Param("memberId") Long memberId);

    /**
     * 농장 존재 여부 확인 (삭제되지 않은 농장만)
     * @param farmId 농장 ID
     * @return 농장 존재 여부 (삭제되지 않은 경우만 true)
     */
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Farm f WHERE f.farmId = :farmId AND f.deleted = false")
    boolean existsById(@Param("farmId") Long farmId);
}
