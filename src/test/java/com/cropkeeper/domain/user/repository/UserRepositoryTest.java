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
                .password("password")
                .name("odg")
                .role(UserRole.USER)
                .build();
        entityManager.persist(user);
        entityManager.flush();

        // when
        Optional<Users> found = userRepository.findByUsername("testuser01");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser01");
        assertThat(found.get().getName()).isEqualTo("odg");
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
    @DisplayName("username 중복")
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

}
