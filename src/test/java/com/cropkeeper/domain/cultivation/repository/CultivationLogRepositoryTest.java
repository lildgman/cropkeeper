package com.cropkeeper.domain.cultivation.repository;

import com.cropkeeper.domain.crop.entity.CropType;
import com.cropkeeper.domain.crop.entity.CropCategory;
import com.cropkeeper.domain.crop.entity.CropVariety;
import com.cropkeeper.domain.cultivation.entity.CultivationLog;
import com.cropkeeper.domain.farm.entity.Farm;
import com.cropkeeper.domain.farm.vo.Address;
import com.cropkeeper.domain.farminglog.vo.FarmingMetadata;
import com.cropkeeper.domain.member.entity.Member;
import com.cropkeeper.domain.member.entity.MemberRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CultivationLogRepositoryTest {

    @Autowired
    private CultivationLogRepository cultivationLogRepository;

    @Autowired
    private TestEntityManager em;

    // ========== 테스트 상수 ==========

    private static final String TEST_USERNAME = "testuser01";
    private static final String TEST_PASSWORD = "encodedPassword";
    private static final String TEST_NAME = "홍길동";
    private static final String TEST_CONTACT = "01012345678";
    private static final String TEST_FARM_NAME = "테스트농장";
    private static final String TEST_STREET = "서울시 강남구";
    private static final Long TEST_FARM_SIZE = 1500L;
    private static final String TEST_WEATHER = "맑음";

    // ========== 테스트 헬퍼 메서드 ==========

    private Member createAndPersistMember() {
        Member member = Member.builder()
                .username(TEST_USERNAME)
                .password(TEST_PASSWORD)
                .name(TEST_NAME)
                .contact(TEST_CONTACT)
                .role(MemberRole.USER)
                .build();

        return em.persist(member);
    }

    private Farm createAndPersistFarm(Member member, String farmName) {

        Farm farm = Farm.builder()
                .farmName(farmName)
                .address(Address.builder()
                        .street(TEST_STREET)
                        .build())
                .farmSize(TEST_FARM_SIZE)
                .member(member)
                .build();

        return em.persist(farm);
    }

    private CropCategory createAndPersistCategory(String categoryName) {
        CropCategory category = CropCategory.builder()
                .categoryName(categoryName)
                .build();

        return em.persist(category);
    }

    private CropType createAndPersistCrop(CropCategory category, String cropName) {
        CropType cropType = CropType.builder()
                .category(category)
                .typeName(cropName)
                .build();

        return em.persist(cropType);

    }

    private CropVariety createAndPersistVariety(CropType cropType, String varietyName) {
        CropVariety variety = CropVariety.builder()
                .cropType(cropType)
                .varietyName(varietyName)
                .build();

        return em.persist(variety);
    }

    private CultivationLog createAndPersistCultivationLog(
            Farm farm,
            Member member,
            CropVariety variety,
            LocalDateTime logDate,
            Long plantingAmount) {

        CultivationLog log = CultivationLog.builder()
                .farm(farm)
                .member(member)
                .variety(variety)
                .metadata(FarmingMetadata.builder()
                        .logDate(logDate)
                        .weather(TEST_WEATHER)
                        .temperature(25L)
                        .humidity(60L)
                        .memo("test")
                        .build())
                .plantingAmount(plantingAmount)
                .build();

        return em.persist(log);
    }

    @Test
    @DisplayName("특정 농장의 재배기록 조회 성공")
    void 특정_농장의_재배기록_조회_성공() {
        // given
        Member member = createAndPersistMember();
        Farm farm1 = createAndPersistFarm(member, "농장1");
        Farm farm2 = createAndPersistFarm(member, "농장2");

        CropCategory category = createAndPersistCategory("과채류");
        CropType cropType = createAndPersistCrop(category, "토마토");
        CropVariety variety = createAndPersistVariety(cropType, "완숙토마토");

        // farm1에 재배기록 3개
        createAndPersistCultivationLog(farm1, member, variety, LocalDateTime.now().minusDays(3), 100L);
        createAndPersistCultivationLog(farm1, member, variety, LocalDateTime.now().minusDays(2), 200L);
        createAndPersistCultivationLog(farm1, member, variety, LocalDateTime.now().minusDays(1), 300L);

        // farm2에 재배기록 1개
        createAndPersistCultivationLog(farm2, member, variety, LocalDateTime.now(), 400L);

        em.flush();
        em.clear();

        // when
        List<CultivationLog> logs = cultivationLogRepository.findByFarm_FarmId(farm1.getFarmId());

        // then
        assertThat(logs).hasSize(3);
        assertThat(logs).extracting(CultivationLog::getPlantingAmount)
                .containsExactlyInAnyOrder(100L, 200L, 300L);
    }

    @Test
    @DisplayName("기간별 재배기록 조회 성공")
    void 기간별_재배기록_조회_성공() {
        // given
        Member member = createAndPersistMember();
        Farm farm = createAndPersistFarm(member, TEST_FARM_NAME);

        CropCategory category = createAndPersistCategory("과채류");
        CropType cropType = createAndPersistCrop(category, "토마토");
        CropVariety variety = createAndPersistVariety(cropType, "완숙토마토");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tenDaysAgo = now.minusDays(10);
        LocalDateTime fiveDaysAgo = now.minusDays(5);
        LocalDateTime threeDaysAgo = now.minusDays(3);
        LocalDateTime yesterday = now.minusDays(1);

        // 다양한 날짜의 재배기록 생성
        createAndPersistCultivationLog(farm, member, variety, tenDaysAgo, 100L);  // 기간 밖
        createAndPersistCultivationLog(farm, member, variety, fiveDaysAgo, 200L);  // 기간 내
        createAndPersistCultivationLog(farm, member, variety, threeDaysAgo, 300L); // 기간 내
        createAndPersistCultivationLog(farm, member, variety, yesterday, 400L);    // 기간 밖

        em.flush();
        em.clear();

        // when: 6일 전부터 2일 전까지 조회
        LocalDateTime start = now.minusDays(6);
        LocalDateTime end = now.minusDays(2);
        List<CultivationLog> logs = cultivationLogRepository
                .findByFarm_FarmIdAndMetadata_LogDateBetween(farm.getFarmId(), start, end);

        // then: 5일 전, 3일 전 기록만 조회됨
        assertThat(logs).hasSize(2);
        assertThat(logs).extracting(CultivationLog::getPlantingAmount)
                .containsExactlyInAnyOrder(200L, 300L);
    }

    @Test
    @DisplayName("품종별 재배기록 조회 성공")
    void 품종별_재배기록_조회_성공() {
        // given
        Member member = createAndPersistMember();
        Farm farm = createAndPersistFarm(member, TEST_FARM_NAME);

        CropCategory category = createAndPersistCategory("과채류");
        CropType cropType = createAndPersistCrop(category, "토마토");
        CropVariety variety1 = createAndPersistVariety(cropType, "완숙토마토");
        CropVariety variety2 = createAndPersistVariety(cropType, "방울토마토");

        // variety1 재배기록 2개
        createAndPersistCultivationLog(farm, member, variety1, LocalDateTime.now().minusDays(2), 100L);
        createAndPersistCultivationLog(farm, member, variety1, LocalDateTime.now().minusDays(1), 200L);

        // variety2 재배기록 1개
        createAndPersistCultivationLog(farm, member, variety2, LocalDateTime.now(), 300L);

        em.flush();
        em.clear();

        // when
        List<CultivationLog> logs = cultivationLogRepository
                .findByFarm_FarmIdAndVariety_VarietyId(farm.getFarmId(), variety1.getVarietyId());

        // then
        assertThat(logs).hasSize(2);
        assertThat(logs).extracting(CultivationLog::getPlantingAmount)
                .containsExactlyInAnyOrder(100L, 200L);
        assertThat(logs).allMatch(log -> log.getVariety().getVarietyId().equals(variety1.getVarietyId()));
    }

    @Test
    @DisplayName("특정 농장의 재배기록 조회 - 빈 목록")
    void 특정_농장의_재배기록_조회_빈_목록() {
        // given
        Member member = createAndPersistMember();
        Farm farm = createAndPersistFarm(member, TEST_FARM_NAME);

        em.flush();
        em.clear();

        // when
        List<CultivationLog> logs = cultivationLogRepository.findByFarm_FarmId(farm.getFarmId());

        // then
        assertThat(logs).isEmpty();
    }
}