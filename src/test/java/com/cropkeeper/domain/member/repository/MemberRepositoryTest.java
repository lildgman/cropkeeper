package com.cropkeeper.domain.member.repository;

import com.cropkeeper.domain.member.entity.Member;
import com.cropkeeper.domain.member.entity.MemberRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("findByUsername - 사용자 조회 성공")
    void findByUsername_Success() {
        // given
        Member user = Member.builder()
                .username("testuser01")
                .password("password")
                .name("odg")
                .role(MemberRole.USER)
                .build();
        entityManager.persist(user);
        entityManager.flush();

        // when
        Optional<Member> found = memberRepository.findByUsername("testuser01");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser01");
        assertThat(found.get().getName()).isEqualTo("odg");
        assertThat(found.get().getRole()).isEqualTo(MemberRole.USER);
    }

    @Test
    @DisplayName("findByUsername - 사용자 없음")
    void findByUsername_NotFound() {
        // when
        Optional<Member> found = memberRepository.findByUsername("nonexistent");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("existsByUsername - 존재하는 사용자")
    void existsByUsername_True() {
        // given
        Member user = Member.builder()
                .username("existing")
                .password("password")
                .name("홍길동")
                .role(MemberRole.USER)
                .build();
        entityManager.persist(user);
        entityManager.flush();

        // when
        boolean exists = memberRepository.existsByUsername("existing");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByUsername - 존재하지 않는 사용자")
    void existsByUsername_False() {
        // when
        boolean exists = memberRepository.existsByUsername("nonexistent");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("username 중복")
    void save_DuplicateUsername_ThrowsException() {
        // given
        Member user1 = Member.builder()
                .username("duplicate")
                .password("password1")
                .name("홍길동")
                .role(MemberRole.USER)
                .build();
        entityManager.persist(user1);
        entityManager.flush();

        Member user2 = Member.builder()
                .username("duplicate")  // 중복!
                .password("password2")
                .name("김철수")
                .role(MemberRole.USER)
                .build();

        // when & then
        assertThatThrownBy(() -> {
            memberRepository.save(user2);
            entityManager.flush();  // flush 시점에 unique 제약조건 체크
        }).isInstanceOf(Exception.class);  // ConstraintViolationException 등
    }

    @Test
    @DisplayName("findById - 회원 조회 성공")
    void findById_Success() {

        // given
        Member member = Member.builder()
                .username("testuser01")
                .password("password1234")
                .name("test")
                .contact("01012345678")
                .role(MemberRole.USER)
                .build();

        Member savedMember = entityManager.persist(member);
        entityManager.flush();

        // when
        Optional<Member> found = memberRepository.findById(savedMember.getMemberId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getMemberId()).isEqualTo(savedMember.getMemberId());
        assertThat(found.get().getUsername()).isEqualTo("testuser01");
        assertThat(found.get().getName()).isEqualTo("test");

    }

    @Test
    @DisplayName("findById - 회원없음")
    void findById_NotFound() {

        // when
        Optional<Member> found = memberRepository.findById(999L);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findByUsername - 탈퇴 회원 조회 안됨")
    void findByUsername_DeletedMember() {

        // given
        Member member = Member.builder()
                .username("testuser01")
                .password("password1234")
                .name("test")
                .contact("01012345678")
                .role(MemberRole.USER)
                .build();
        member.delete();
        entityManager.persist(member);
        entityManager.flush();

        // when
        Optional<Member> found = memberRepository.findByUsername("testuser01");

        // then
        assertThat(found).isEmpty();
    }



}
