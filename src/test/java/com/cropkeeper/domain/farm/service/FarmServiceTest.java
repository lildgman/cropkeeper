package com.cropkeeper.domain.farm.service;

import com.cropkeeper.domain.farm.dto.request.CreateFarmRequest;
import com.cropkeeper.domain.farm.dto.response.FarmResponse;
import com.cropkeeper.domain.farm.entity.Farm;
import com.cropkeeper.domain.farm.repository.FarmRepository;
import com.cropkeeper.domain.member.entity.Member;
import com.cropkeeper.domain.member.entity.MemberRole;
import com.cropkeeper.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FarmServiceTest {

    @Autowired
    private FarmService farmService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private FarmRepository farmRepository;

    @Test
    @DisplayName("농장 생성 시 DB 저장 테스트 - 프록시 방식")
    void testCreateFarm() {
        // given: 회원 생성
        Member member = Member.builder()
                .username("testuser")
                .password("password123")
                .name("테스트유저")
                .contact("010-1234-5678")
                .role(MemberRole.USER)
                .build();
        Member savedMember = memberRepository.save(member);

        System.out.println("========================================");
        System.out.println("1. 회원 저장 완료 - memberId: " + savedMember.getMemberId());
        System.out.println("   회원의 농장 수: " + savedMember.getFarms().size());
        System.out.println("========================================\n");

        // given: 농장 생성 요청
        CreateFarmRequest request = CreateFarmRequest.builder()
                .farmName("테스트 농장")
                .zipCode("12345")
                .street("서울시 강남구")
                .detail("테헤란로 123")
                .farmSize(1000L)
                .build();

        System.out.println("========================================");
        System.out.println("2. 농장 생성 요청");
        System.out.println("   farmName: " + request.getFarmName());
        System.out.println("   farmSize: " + request.getFarmSize());
        System.out.println("========================================\n");

        // when: 농장 생성 (FarmService.createFarm 호출)
        System.out.println("========================================");
        System.out.println("3. FarmService.createFarm() 호출 시작");
        System.out.println("   [여기서 SQL이 실행됩니다]");
        System.out.println("========================================\n");

        FarmResponse response = farmService.createFarm(savedMember.getMemberId(), request);

        System.out.println("========================================");
        System.out.println("4. 농장 생성 완료");
        System.out.println("   farmId: " + response.getFarmId());
        System.out.println("   farmName: " + response.getFarmName());
        System.out.println("========================================\n");

        // then: DB에서 실제로 저장되었는지 확인
        System.out.println("========================================");
        System.out.println("5. DB에서 농장 조회");
        System.out.println("========================================\n");

        Farm savedFarm = farmRepository.findById(response.getFarmId()).orElseThrow();

        assertThat(savedFarm).isNotNull();
        assertThat(savedFarm.getFarmName()).isEqualTo("테스트 농장");
        assertThat(savedFarm.getFarmSize()).isEqualTo(1000L);
        assertThat(savedFarm.getMember()).isNotNull();
        assertThat(savedFarm.getMember().getMemberId()).isEqualTo(savedMember.getMemberId());

        System.out.println("========================================");
        System.out.println("6. 검증 완료");
        System.out.println("   savedFarm.farmId: " + savedFarm.getFarmId());
        System.out.println("   savedFarm.farmName: " + savedFarm.getFarmName());
        System.out.println("   savedFarm.member.memberId: " + savedFarm.getMember().getMemberId());
        System.out.println("========================================\n");

        System.out.println("========================================");
        System.out.println("7. 회원의 농장 목록 조회 시도");
        System.out.println("========================================\n");

        // 프록시 방식이므로 member.getFarms()에는 새로운 farm이 없음
        System.out.println("   savedMember.getFarms().size(): " + savedMember.getFarms().size());
        System.out.println("   → 프록시 방식이므로 메모리상 동기화 안 됨 (정상)");

        // DB에서 다시 조회하면 정상적으로 나옴
        Member reloadedMember = memberRepository.findById(savedMember.getMemberId()).orElseThrow();
        System.out.println("   reloadedMember.getFarms().size(): " + reloadedMember.getFarms().size());
        System.out.println("   → DB에서 다시 조회하면 정상 (FK로 연결되어 있음)");
        System.out.println("========================================\n");
    }
}
