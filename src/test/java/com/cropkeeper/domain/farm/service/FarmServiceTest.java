package com.cropkeeper.domain.farm.service;

import com.cropkeeper.domain.farm.dto.request.CreateFarmRequest;
import com.cropkeeper.domain.farm.dto.request.UpdateFarmRequest;
import com.cropkeeper.domain.farm.dto.response.FarmResponse;
import com.cropkeeper.domain.farm.entity.Farm;
import com.cropkeeper.domain.farm.exception.FarmAlreadyDeletedException;
import com.cropkeeper.domain.farm.exception.FarmNotFoundException;
import com.cropkeeper.domain.farm.exception.InvalidFarmRequestException;
import com.cropkeeper.domain.farm.repository.FarmRepository;
import com.cropkeeper.domain.farm.vo.Address;
import com.cropkeeper.domain.member.entity.Member;
import com.cropkeeper.domain.member.entity.MemberRole;
import com.cropkeeper.domain.member.exception.MemberNotFoundException;
import com.cropkeeper.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FarmServiceTest {

    @Mock
    private FarmRepository farmRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private FarmService farmService;

    // ========== 테스트 상수 ==========

    private static final Long TEST_MEMBER_ID = 1L;
    private static final Long NON_EXISTENT_ID = 999L;
    private static final String TEST_USERNAME = "testuser01";
    private static final String TEST_NAME = "홍길동";
    private static final String TEST_ZIPCODE = "12345";
    private static final String TEST_STREET = "서울시 강남구";
    private static final String TEST_DETAIL = "테헤란로 123";
    private static final Long TEST_FARM_SIZE = 1500L;
    private static final String TEST_FARM_NAME = "우리농장";

    // ========== 테스트 헬퍼 메서드 ==========

    /**
     * 기본 테스트용 Member 객체 생성
     */
    private Member createDefaultTestMember() {
        return Member.builder()
                .memberId(TEST_MEMBER_ID)
                .username(TEST_USERNAME)
                .password("encodedPassword")
                .name(TEST_NAME)
                .contact("01012345678")
                .role(MemberRole.USER)
                .build();
    }

    /**
     * 기본 주소를 가진 테스트용 Farm 객체 생성
     */
    private Farm createDefaultTestFarm(Long farmId, Member member) {
        return Farm.builder()
                .farmId(farmId)
                .farmName(FarmServiceTest.TEST_FARM_NAME)
                .address(createDefaultAddress())
                .farmSize(TEST_FARM_SIZE)
                .member(member)
                .build();
    }

    /**
     * 기본 테스트용 Address 객체 생성
     */
    private Address createDefaultAddress() {
        return Address.builder()
                .zipCode(TEST_ZIPCODE)
                .street(TEST_STREET)
                .detail(TEST_DETAIL)
                .build();
    }

    /**
     * 간단한 주소를 가진 테스트용 Farm 객체 생성
     */
    private Farm createSimpleTestFarm(Long farmId, String farmName, String street, Member member) {
        return Farm.builder()
                .farmId(farmId)
                .farmName(farmName)
                .address(Address.builder().street(street).build())
                .farmSize(TEST_FARM_SIZE)
                .member(member)
                .build();
    }

    // ========== createFarm 테스트 ==========

    @Test
    @DisplayName("농장 생성 성공")
    void createFarm_Success() {

        // given
        CreateFarmRequest request = CreateFarmRequest.builder()
                .farmName(TEST_FARM_NAME)
                .zipCode(TEST_ZIPCODE)
                .street(TEST_STREET)
                .detail(TEST_DETAIL)
                .farmSize(TEST_FARM_SIZE)
                .build();

        Member memberRef = createDefaultTestMember();
        Farm savedFarm = createDefaultTestFarm(1L, memberRef);

        when(memberRepository.existsById(TEST_MEMBER_ID)).thenReturn(true);
        when(memberRepository.getReferenceById(TEST_MEMBER_ID)).thenReturn(memberRef);
        when(farmRepository.save(any(Farm.class))).thenReturn(savedFarm);

        // when
        FarmResponse response = farmService.createFarm(TEST_MEMBER_ID, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getFarmId()).isEqualTo(1L);
        assertThat(response.getFarmName()).isEqualTo(TEST_FARM_NAME);
        assertThat(response.getFarmSize()).isEqualTo(TEST_FARM_SIZE);
        assertThat(response.getZipCode()).isEqualTo(TEST_ZIPCODE);
        assertThat(response.getStreet()).isEqualTo(TEST_STREET);
        assertThat(response.getDetail()).isEqualTo(TEST_DETAIL);

        verify(memberRepository, times(1)).existsById(TEST_MEMBER_ID);
        verify(memberRepository, times(1)).getReferenceById(TEST_MEMBER_ID);
        verify(farmRepository, times(1)).save(any(Farm.class));
    }

    @Test
    @DisplayName("농장 생성 실패 - 존재하지 않는 회원")
    void createFarm_Fail_MemberNotFound() {

        // given
        CreateFarmRequest request = CreateFarmRequest.builder()
                .farmName(TEST_FARM_NAME)
                .street(TEST_STREET)
                .farmSize(TEST_FARM_SIZE)
                .build();

        when(memberRepository.existsById(NON_EXISTENT_ID)).thenReturn(false);

        // when, then
        assertThatThrownBy(() -> farmService.createFarm(NON_EXISTENT_ID, request))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessageContaining("회원을 찾을 수 없습니다");

        verify(memberRepository, times(1)).existsById(NON_EXISTENT_ID);
        verify(memberRepository, never()).getReferenceById(any());
        verify(farmRepository, never()).save(any(Farm.class));
    }

    // ========== findAllByMemberId 테스트 ==========

    @Test
    @DisplayName("회원 농장 목록 조회 성공 - 여러 개")
    void findAllByMemberId_Success_Multiple() {

        // given
        Member member = createDefaultTestMember();
        Farm farm1 = createSimpleTestFarm(1L, "첫번째농장", TEST_STREET, member);
        Farm farm2 = createSimpleTestFarm(2L, "두번째농장", "경기도 성남시", member);

        List<Farm> farms = Arrays.asList(farm1, farm2);
        when(farmRepository.findByMemberId(TEST_MEMBER_ID)).thenReturn(farms);

        // when
        List<FarmResponse> responses = farmService.findAllByMemberId(TEST_MEMBER_ID);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(FarmResponse::getFarmName)
                .containsExactly("첫번째농장", "두번째농장");
        assertThat(responses).extracting(FarmResponse::getFarmSize)
                .containsExactly(TEST_FARM_SIZE, TEST_FARM_SIZE);

        verify(farmRepository, times(1)).findByMemberId(TEST_MEMBER_ID);
    }

    @Test
    @DisplayName("회원 농장 목록 조회 성공 - 빈 목록")
    void findAllByMemberId_Success_Empty() {

        // given
        when(farmRepository.findByMemberId(TEST_MEMBER_ID)).thenReturn(List.of());

        // when
        List<FarmResponse> responses = farmService.findAllByMemberId(TEST_MEMBER_ID);

        // then
        assertThat(responses).isEmpty();

        verify(farmRepository, times(1)).findByMemberId(TEST_MEMBER_ID);
    }

    // ========== getFarmInfo 테스트 ==========

    @Test
    @DisplayName("농장 정보 조회 성공")
    void getFarmInfo_Success() {

        // given
        Long farmId = 1L;
        Member member = createDefaultTestMember();
        Farm farm = createDefaultTestFarm(farmId, member);

        when(farmRepository.findById(farmId)).thenReturn(Optional.of(farm));

        // when
        FarmResponse response = farmService.getFarmInfo(farmId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getFarmId()).isEqualTo(farmId);
        assertThat(response.getFarmName()).isEqualTo(TEST_FARM_NAME);
        assertThat(response.getFarmSize()).isEqualTo(TEST_FARM_SIZE);
        assertThat(response.getZipCode()).isEqualTo(TEST_ZIPCODE);
        assertThat(response.getMemberId()).isEqualTo(TEST_MEMBER_ID);

        verify(farmRepository, times(1)).findById(farmId);
    }

    @Test
    @DisplayName("농장 정보 조회 실패 - 존재하지 않는 농장")
    void getFarmInfo_Fail_FarmNotFound() {

        // given
        when(farmRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> farmService.getFarmInfo(NON_EXISTENT_ID))
                .isInstanceOf(FarmNotFoundException.class)
                .hasMessageContaining("농장을 찾을 수 없습니다");

        verify(farmRepository, times(1)).findById(NON_EXISTENT_ID);
    }

    // ========== updateFarm 테스트 ==========

    @Test
    @DisplayName("농장 정보 수정 성공 - 모든 필드 수정")
    void updateFarm_Success_AllFields() {

        // given
        Long farmId = 1L;
        Member member = createDefaultTestMember();
        Farm farm = createDefaultTestFarm(farmId, member);

        UpdateFarmRequest request = UpdateFarmRequest.builder()
                .farmName("새농장")
                .zipCode("54321")
                .street("경기도 성남시")
                .detail("분당구 456")
                .farmSize(2500L)
                .build();

        when(farmRepository.findById(farmId)).thenReturn(Optional.of(farm));

        // when
        FarmResponse response = farmService.updateFarm(farmId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getFarmName()).isEqualTo("새농장");
        assertThat(response.getFarmSize()).isEqualTo(2500L);
        assertThat(response.getZipCode()).isEqualTo("54321");
        assertThat(response.getStreet()).isEqualTo("경기도 성남시");
        assertThat(response.getDetail()).isEqualTo("분당구 456");

        verify(farmRepository, times(1)).findById(farmId);
    }

    @Test
    @DisplayName("농장 정보 수정 성공 - 농장 이름만 수정")
    void updateFarm_Success_NameOnly() {

        // given
        Long farmId = 1L;
        Member member = createDefaultTestMember();
        Farm farm = createDefaultTestFarm(farmId, member);

        UpdateFarmRequest request = UpdateFarmRequest.builder()
                .farmName("새농장")
                .build();

        when(farmRepository.findById(farmId)).thenReturn(Optional.of(farm));

        // when
        FarmResponse response = farmService.updateFarm(farmId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getFarmName()).isEqualTo("새농장");
        assertThat(response.getFarmSize()).isEqualTo(TEST_FARM_SIZE); // 변경되지 않음
        assertThat(response.getZipCode()).isEqualTo(TEST_ZIPCODE); // 변경되지 않음

        verify(farmRepository, times(1)).findById(farmId);
    }

    @Test
    @DisplayName("농장 정보 수정 성공 - 주소만 수정")
    void updateFarm_Success_AddressOnly() {

        // given
        Long farmId = 1L;
        Member member = createDefaultTestMember();
        Farm farm = createDefaultTestFarm(farmId, member);

        UpdateFarmRequest request = UpdateFarmRequest.builder()
                .zipCode("54321")
                .street("경기도 성남시")
                .detail("분당구 456")
                .build();

        when(farmRepository.findById(farmId)).thenReturn(Optional.of(farm));

        // when
        FarmResponse response = farmService.updateFarm(farmId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getFarmName()).isEqualTo(TEST_FARM_NAME); // 변경되지 않음
        assertThat(response.getZipCode()).isEqualTo("54321");
        assertThat(response.getStreet()).isEqualTo("경기도 성남시");
        assertThat(response.getDetail()).isEqualTo("분당구 456");

        verify(farmRepository, times(1)).findById(farmId);
    }

    @Test
    @DisplayName("농장 정보 수정 성공 - 농장 크기만 수정")
    void updateFarm_Success_SizeOnly() {

        // given
        Long farmId = 1L;
        Member member = createDefaultTestMember();
        Farm farm = createSimpleTestFarm(farmId, TEST_FARM_NAME, TEST_STREET, member);

        UpdateFarmRequest request = UpdateFarmRequest.builder()
                .farmSize(2500L)
                .build();

        when(farmRepository.findById(farmId)).thenReturn(Optional.of(farm));

        // when
        FarmResponse response = farmService.updateFarm(farmId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getFarmName()).isEqualTo(TEST_FARM_NAME); // 변경되지 않음
        assertThat(response.getFarmSize()).isEqualTo(2500L);

        verify(farmRepository, times(1)).findById(farmId);
    }

    @Test
    @DisplayName("농장 정보 수정 실패 - 수정할 필드 없음")
    void updateFarm_Fail_NoFieldToUpdate() {

        // given
        UpdateFarmRequest request = UpdateFarmRequest.builder()
                .build();

        // when, then
        assertThatThrownBy(() -> farmService.updateFarm(1L, request))
                .isInstanceOf(InvalidFarmRequestException.class)
                .hasMessageContaining("수정할 정보가 없습니다");

        verify(farmRepository, never()).findById(any());
    }

    @Test
    @DisplayName("농장 정보 수정 실패 - 존재하지 않는 농장")
    void updateFarm_Fail_FarmNotFound() {

        // given
        UpdateFarmRequest request = UpdateFarmRequest.builder()
                .farmName("새농장")
                .build();

        when(farmRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> farmService.updateFarm(NON_EXISTENT_ID, request))
                .isInstanceOf(FarmNotFoundException.class)
                .hasMessageContaining("농장을 찾을 수 없습니다");

        verify(farmRepository, times(1)).findById(NON_EXISTENT_ID);
    }

    @Test
    @DisplayName("농장 정보 수정 실패 - 빈 문자열은 수정으로 인정 안됨")
    void updateFarm_Fail_EmptyString() {

        // given
        UpdateFarmRequest request = UpdateFarmRequest.builder()
                .farmName("")
                .build();

        // when, then
        assertThatThrownBy(() -> farmService.updateFarm(1L, request))
                .isInstanceOf(InvalidFarmRequestException.class)
                .hasMessageContaining("수정할 정보가 없습니다");

        verify(farmRepository, never()).findById(any());
    }

    // ========== deleteFarm 테스트 ==========

    @Test
    @DisplayName("농장 삭제 성공")
    void deleteFarm_Success() {

        // given
        Long farmId = 1L;
        Member member = createDefaultTestMember();
        Farm farm = createSimpleTestFarm(farmId, TEST_FARM_NAME, TEST_STREET, member);

        when(farmRepository.findById(farmId)).thenReturn(Optional.of(farm));

        // when
        farmService.deleteFarm(farmId);

        // then
        assertThat(farm.isDeleted()).isTrue();
        assertThat(farm.getDeletedAt()).isNotNull();

        verify(farmRepository, times(1)).findById(farmId);
    }

    @Test
    @DisplayName("농장 삭제 실패 - 존재하지 않는 농장")
    void deleteFarm_Fail_FarmNotFound() {

        // given
        when(farmRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> farmService.deleteFarm(NON_EXISTENT_ID))
                .isInstanceOf(FarmNotFoundException.class)
                .hasMessageContaining("농장을 찾을 수 없습니다");

        verify(farmRepository, times(1)).findById(NON_EXISTENT_ID);
    }

    @Test
    @DisplayName("농장 삭제 실패 - 이미 삭제된 농장")
    void deleteFarm_Fail_AlreadyDeleted() {

        // given
        Long farmId = 1L;
        Member member = createDefaultTestMember();
        Farm farm = createSimpleTestFarm(farmId, TEST_FARM_NAME, TEST_STREET, member);
        farm.delete(); // 미리 삭제 처리

        when(farmRepository.findById(farmId)).thenReturn(Optional.of(farm));

        // when, then
        assertThatThrownBy(() -> farmService.deleteFarm(farmId))
                .isInstanceOf(FarmAlreadyDeletedException.class)
                .hasMessageContaining("이미 삭제된 농장입니다");

        verify(farmRepository, times(1)).findById(farmId);
    }
}
