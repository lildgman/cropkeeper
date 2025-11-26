package com.cropkeeper.domain.farm.repository;

import com.cropkeeper.domain.farm.entity.Farm;
import com.cropkeeper.domain.farm.vo.Address;
import com.cropkeeper.domain.member.entity.Member;
import com.cropkeeper.domain.member.entity.MemberRole;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class FarmRepositoryTest {

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Member createMember(String username, String name) {

        Member member = Member.builder()
                .username(username)
                .password("encodedPassword")
                .name(name)
                .contact("01011112222")
                .role(MemberRole.USER)
                .build();

        return entityManager.persist(member);

    }

    private Farm createFarm(String farmName, Member member, Long farmSize) {

        Address address = new Address("00000", "서울시 관악구", "어디어디로");

        Farm farm = Farm.builder()
                .farmName(farmName)
                .address(address)
                .farmSize(farmSize)
                // member는 여기서 설정하지 않음
                .build();

        // 연관관계 편의 메서드 사용 (양방향 설정)
        member.addFarm(farm);

        return entityManager.persist(farm);
    }

    @Test
    @DisplayName("농장 조회 성공")
    void findById_Success() {

        // given
        Member member = createMember("testUser", "test");
        Farm farm = createFarm("testFarm", member, 1500L);

        entityManager.flush();
        entityManager.clear();

        // when
        Farm foundFarm = farmRepository.findById(farm.getFarmId()).get();

        // then
        assertThat(foundFarm).isNotNull();
        assertThat(foundFarm.getFarmId()).isEqualTo(farm.getFarmId());
        assertThat(foundFarm.getFarmName()).isEqualTo(farm.getFarmName());
        assertThat(foundFarm.getFarmSize()).isEqualTo(farm.getFarmSize());
    }

    @Test
    @DisplayName("농장 조회 - 없음")
    void findById_NotFound() {

        Optional<Farm> foundFarm = farmRepository.findById(999L);

        assertThat(foundFarm).isEmpty();
    }

    @Test
    @DisplayName("삭제된 농장은 조회되지 않음")
    void findById_DeletedFarm() {

        // given
        Member member = createMember("testUser", "test");
        Farm farm = createFarm("testFarm", member, 1000L);
        farm.delete();

        entityManager.flush();
        entityManager.clear();

        // when
        Optional<Farm> foundFarm = farmRepository.findById(farm.getFarmId());

        // then
        assertThat(foundFarm).isEmpty();
    }

    @Test
    @DisplayName("회원 농장 목록 조회 성공")
    void findByMemberId_Success() {

        Member member = createMember("testUser", "test");
        Farm farm1 = createFarm("testFarm1", member, 1000L);
        Farm farm2 = createFarm("testFarm2", member, 1500L);
        Farm farm3 = createFarm("testFarm3", member, 2000L);

        entityManager.flush();
        entityManager.clear();

        List<Farm> farms = farmRepository.findByMemberId(member.getMemberId());

        assertThat(farms).hasSize(3);
        assertThat(farms).extracting(Farm::getFarmName)
                .containsExactlyInAnyOrder("testFarm1", "testFarm2", "testFarm3");
        assertThat(farms).extracting(Farm::getFarmSize)
                .containsExactlyInAnyOrder(1000L, 1500L, 2000L);

    }

    @Test
    @DisplayName("농장 조회 시 삭제된 농장은 조회 안됨")
    void findByMemberId_ExcludeDeletedFarm() {

        Member member = createMember("testUser", "test");
        Farm farm1 = createFarm("testFarm1", member, 1000L);
        Farm farm2 = createFarm("testFarm2", member, 1500L);
        Farm farm3 = createFarm("testFarm3", member, 2000L);
        farm2.delete();

        entityManager.flush();
        entityManager.clear();

        List<Farm> farms = farmRepository.findByMemberId(member.getMemberId());

        assertThat(farms).hasSize(2);

    }

    @Test
    @DisplayName("JOIN FETCH로 Member 함께 조회")
    void findByMemberId_WithMemberJoinFetch() {

        Member member = createMember("testUser", "test");
        Farm farm = createFarm("testFarm1", member, 1000L);
        entityManager.flush();
        entityManager.clear();

        List<Farm> farms = farmRepository.findByMemberId(member.getMemberId());

        assertThat(farms).hasSize(1);
        Farm foundFarm = farms.get(0);

        assertThat(foundFarm.getMember()).isNotNull();
        assertThat(foundFarm.getMember().getName()).isEqualTo("test");
        assertThat(foundFarm.getMember().getUsername()).isEqualTo("testUser");
    }

    @Test
    @DisplayName("다른 회원 농장은 조회x")
    void findByFarmIdAndMemberId_WrongMember() {

        Member member1 = createMember("testUser1", "test");
        Member member2 = createMember("testUser2", "test");
        Farm farm = createFarm("testFarm1", member1, 1000L);

        entityManager.flush();
        entityManager.clear();

        Optional<Farm> foundFarm = farmRepository.findByFarmIdAndMemberId(
                farm.getFarmId(),
                member2.getMemberId());

        assertThat(foundFarm).isEmpty();
    }

    @Test
    @DisplayName("삭제된 농장 조회x")
    void findByFarmIdAndMemberId_DeleteFarm() {

        Member member = createMember("testUser", "test");
        Farm farm = createFarm("testFarm", member, 1000L);
        farm.delete();
        entityManager.flush();
        entityManager.clear();

        Optional<Farm> found = farmRepository.findByFarmIdAndMemberId(
                farm.getFarmId(),
                member.getMemberId()
        );

        assertThat(found).isEmpty();

    }

    @Test
    @DisplayName("농장 존재함")
    void existById_True() {

        Member member = createMember("testUser", "test");
        Farm farm = createFarm("testFarm", member, 1000L);
        entityManager.flush();
        entityManager.clear();

        boolean exists = farmRepository.existsById(farm.getFarmId());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("농장 존재x")
    void existById_false() {

        boolean exists = farmRepository.existsById(999L);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("삭제된 농장은 조회 x")
    void existsById_DeletedFarm() {

        Member member = createMember("testUser", "test");
        Farm farm = createFarm("testFarm", member, 1000L);
        farm.delete();
        entityManager.flush();
        entityManager.clear();

        boolean exists = farmRepository.existsById(farm.getFarmId());

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("농장 저장 성공")
    void save_Success() {

        Member member = createMember("testUser", "test");
        Address address = new Address("12345", "서울시 강남구", "테헤란로 123");
        Farm farm = Farm.builder()
                .farmName("newFarm")
                .address(address)
                .farmSize(1500L)
                .member(member)
                .build();

        Farm savedFarm = farmRepository.save(farm);
        entityManager.flush();

        assertThat(savedFarm.getFarmId()).isNotNull();
        assertThat(savedFarm.getFarmName()).isEqualTo("newFarm");
        assertThat(savedFarm.getFarmSize()).isEqualTo(1500L);
        assertThat(savedFarm.getMember().getMemberId()).isEqualTo(member.getMemberId());
        assertThat(savedFarm.getDeleted()).isFalse();
        assertThat(savedFarm.getCreatedAt()).isNotNull();
        assertThat(savedFarm.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Soft delete")
    void soft_delete() {

        Member member = createMember("testUser", "test");
        Farm farm = createFarm("testFarm", member, 1000L);
        entityManager.flush();
        Long farmId = farm.getFarmId();

        farm.delete();
        entityManager.flush();
        entityManager.clear();

        Optional<Farm> found = farmRepository.findById(farmId);
        assertThat(found).isEmpty();

        Farm actualFarm = entityManager.find(Farm.class, farmId);
        assertThat(actualFarm).isNotNull();
        assertThat(actualFarm.getDeleted()).isTrue();

    }

    @Test
    @DisplayName("농장 수정")
    void update() {

        Member member = createMember("testUser", "test");
        Farm farm = createFarm("testFarm", member, 1000L);
        entityManager.flush();
        Long farmId = farm.getFarmId();

        farm.updateFarmName("updateFarm");
        farm.updateFarmSize(2000L);
        farm.updateAddress("53231", "제주 서귀포", "52-1");
        entityManager.flush();
        entityManager.clear();

        Optional<Farm> found = farmRepository.findById(farmId);
        assertThat(found).isPresent();
        assertThat(found.get().getFarmName()).isEqualTo("updateFarm");
        assertThat(found.get().getFarmSize()).isEqualTo(2000L);
        assertThat(found.get().getAddress().getZipCode()).isEqualTo("53231");
        assertThat(found.get().getAddress().getStreet()).isEqualTo("제주 서귀포");
        assertThat(found.get().getAddress().getDetail()).isEqualTo("52-1");

    }

    @Test
    @DisplayName("회원 삭제 시 농장도 삭제")
    void cascade_Delete() {

        Member member = createMember("testUser", "test");
        Farm farm1 = createFarm("testFarm1", member, 1000L);
        Farm farm2 = createFarm("testFarm2", member, 500L);
        entityManager.flush();
        Long farm1Id = farm1.getFarmId();
        Long farm2Id = farm2.getFarmId();

        member.delete();
        entityManager.flush();
        entityManager.clear();

        assertThat(farmRepository.findById(farm1Id)).isEmpty();
        assertThat(farmRepository.findById(farm2Id)).isEmpty();
    }
}