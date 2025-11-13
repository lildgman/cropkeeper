package com.cropkeeper.domain.user.repository;

import com.cropkeeper.domain.user.entity.UserRole;
import com.cropkeeper.domain.user.entity.Users;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * UserRepository 단위 테스트
 *
 * 특징:
 * - @DataJpaTest: JPA 관련 Bean만 로드 (가벼움)
 * - 실제 H2 DB 사용 (빠름)
 * - 각 테스트 후 자동 롤백
 * - TestEntityManager: 테스트용 엔티티 매니저
 *
 * 테스트 대상:
 * - 커스텀 쿼리 메서드 (findByUsername, existsByUsername)
 * - 복잡한 쿼리가 있다면 반드시 테스트 필요
 */
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("findByUsername - 사용자 조회 성공")
    void findByUsername_Success() {
        // given
        Users user = Users.builder()
                .username("testuser01")
                .password("encoded_password")
                .name("홍길동")
                .role(UserRole.USER)
                .build();
        entityManager.persist(user);
        entityManager.flush();

        // when
        Optional<Users> found = userRepository.findByUsername("testuser01");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser01");
        assertThat(found.get().getName()).isEqualTo("홍길동");
        assertThat(found.get().getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("findByUsername - 사용자 없음")
    void findByUsername_NotFound() {
        // when
        Optional<Users> found = userRepository.findByUsername("nonexistent");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("existsByUsername - 존재하는 사용자")
    void existsByUsername_True() {
        // given
        Users user = Users.builder()
                .username("existing")
                .password("password")
                .name("홍길동")
                .role(UserRole.USER)
                .build();
        entityManager.persist(user);
        entityManager.flush();

        // when
        boolean exists = userRepository.existsByUsername("existing");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByUsername - 존재하지 않는 사용자")
    void existsByUsername_False() {
        // when
        boolean exists = userRepository.existsByUsername("nonexistent");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("save - 새 사용자 저장")
    void save_NewUser() {
        // given
        Users user = Users.builder()
                .username("newuser")
                .password("password")
                .name("김철수")
                .contact("01012345678")
                .role(UserRole.USER)
                .build();

        // when
        Users saved = userRepository.save(user);

        // then
        assertThat(saved.getUserId()).isNotNull();  // ID 자동 생성 확인
        assertThat(saved.getUsername()).isEqualTo("newuser");
        assertThat(saved.getCreatedAt()).isNotNull();  // BaseTimeEntity 동작 확인
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("username은 unique 제약조건이 있음")
    void save_DuplicateUsername_ThrowsException() {
        // given
        Users user1 = Users.builder()
                .username("duplicate")
                .password("password1")
                .name("홍길동")
                .role(UserRole.USER)
                .build();
        entityManager.persist(user1);
        entityManager.flush();

        Users user2 = Users.builder()
                .username("duplicate")  // 중복!
                .password("password2")
                .name("김철수")
                .role(UserRole.USER)
                .build();

        // when & then
        assertThatThrownBy(() -> {
            userRepository.save(user2);
            entityManager.flush();  // flush 시점에 unique 제약조건 체크
        }).isInstanceOf(Exception.class);  // ConstraintViolationException 등
    }

    @Test
    @DisplayName("여러 사용자 저장 및 조회")
    void findAll_MultipleUsers() {
        // given
        Users user1 = Users.builder()
                .username("user1")
                .password("pass1")
                .name("사용자1")
                .role(UserRole.USER)
                .build();

        Users user2 = Users.builder()
                .username("user2")
                .password("pass2")
                .name("사용자2")
                .role(UserRole.ADMIN)
                .build();

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        // when
        var users = userRepository.findAll();

        // then
        assertThat(users).hasSize(2);
        assertThat(users).extracting(Users::getUsername)
                .containsExactlyInAnyOrder("user1", "user2");
    }
}
