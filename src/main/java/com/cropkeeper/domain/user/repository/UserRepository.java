package com.cropkeeper.domain.user.repository;

import com.cropkeeper.domain.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User 엔티티의 데이터베이스 접근을 담당하는 Repository
 *
 * JpaRepository를 상속받아 기본 CRUD 기능 자동 제공:
 * - save(), findById(), findAll(), delete() 등
 */
@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    /**
     * 사용자 이름(username)으로 사용자 조회
     * 로그인 시 사용됨
     *
     * @param username 사용자 이름
     * @return Optional로 감싸진 User 객체 (없으면 Optional.empty())
     */
    Optional<Users> findByUsername(String username);

    /**
     * 사용자 이름이 이미 존재하는지 확인
     * 회원가입 시 중복 체크에 사용
     *
     * @param username 확인할 사용자 이름
     * @return 존재하면 true, 아니면 false
     */
    boolean existsByUsername(String username);
}
