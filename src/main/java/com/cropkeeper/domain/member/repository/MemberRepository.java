package com.cropkeeper.domain.member.repository;

import com.cropkeeper.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * username으로 회원 조회(탈퇴하지 않은 회원만)
     *
     * @param username 사용자 이름
     * @return 회원 정보(탈퇴하지 않은 경우)
     */
    @Query("SELECT m FROM Member m WHERE m.username = :username AND m.deleted = false")
    Optional<Member> findByUsername(@Param("username") String username);

    /**
     * ID로 회원 조회(탈퇴하지 않은 회원)
     *
     * @param memberId
     * @return 회원 정보(탈퇴하지 않은 경우)
     */
    @Query("SELECT m FROM Member m WHERE m.memberId = :memberId AND m.deleted = false")
    Optional<Member> findById(@Param("memberId") Long memberId);


    /**
     * username 중복체크 (탈퇴하지 않은 회원만)
     * @param username 사용자 이름
     * @return username 중복여부
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Member m WHERE m.username = :username AND m.deleted = false")
    boolean existsByUsername(@Param("username") String username);
}
