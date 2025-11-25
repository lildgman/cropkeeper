package com.cropkeeper.domain.farm.service;

import com.cropkeeper.domain.farm.dto.request.CreateFarmRequest;
import com.cropkeeper.domain.farm.dto.request.UpdateFarmRequest;
import com.cropkeeper.domain.farm.dto.response.FarmResponse;
import com.cropkeeper.domain.farm.entity.Farm;
import com.cropkeeper.domain.farm.exception.FarmAlreadyDeletedException;
import com.cropkeeper.domain.farm.exception.FarmErrorCode;
import com.cropkeeper.domain.farm.exception.FarmNotFoundException;
import com.cropkeeper.domain.farm.exception.InvalidFarmRequestException;
import com.cropkeeper.domain.farm.repository.FarmRepository;
import com.cropkeeper.domain.member.entity.Member;
import com.cropkeeper.domain.member.exception.MemberNotFoundException;
import com.cropkeeper.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FarmService {

    private final FarmRepository farmRepository;
    private final MemberRepository memberRepository;

    /**
     * 농장 생성
     * @param memberId 회원ID
     * @param request 농장 생성 요청
     * @return 생성된 농장 응답
     */
    @Transactional
    public FarmResponse createFarm(Long memberId, CreateFarmRequest request) {

        // Member 존재 여부만 확인 (전체 조회 불필요)
        if (!memberRepository.existsById(memberId)) {
            log.warn("농장 생성 실패: 존재하지 않는 회원 - memberId = {}", memberId);
            throw new MemberNotFoundException(memberId);
        }

        // Member 프록시 객체 사용 (실제 DB 조회 없이 FK만 설정)
        Member memberRef = memberRepository.getReferenceById(memberId);

        Farm farm = Farm.builder()
                .farmName(request.getFarmName())
                .address(request.toAddress())
                .farmSize(request.getFarmSize())
                .member(memberRef)
                .build();

        Farm savedFarm = farmRepository.save(farm);

        return FarmResponse.from(savedFarm);
    }

    /**
     * 농장 ID로 농장 조회 (내부 사용용)
     * @param farmId 농장 ID
     * @return 농장 엔티티
     * @throws FarmNotFoundException 농장을 찾을 수 없는 경우
     */
    private Farm findById(Long farmId) {
        return farmRepository.findById(farmId)
                .orElseThrow(() -> new FarmNotFoundException(farmId));
    }

    /**
     * 특정 회원의 모든 농장 조회
     * @param memberId 회원 ID
     * @return 농장 DTO 목록
     */
    public List<FarmResponse> findAllByMemberId(Long memberId) {

        List<Farm> farms = farmRepository.findByMemberId(memberId);

        return farms.stream()
                .map(FarmResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 농장 정보 조회 (외부 API용)
     * @param farmId 농장 ID
     * @return 농장 정보 DTO
     * @throws FarmNotFoundException 농장을 찾을 수 없는 경우
     */
    public FarmResponse getFarmInfo(Long farmId) {
        Farm farm = findById(farmId);
        return FarmResponse.from(farm);
    }

    /**
     * 농장 정보 수정
     * @param farmId 농장 ID
     * @param request 수정 요청
     * @return 수정된 농장 dto
     * @throws FarmNotFoundException 농장을 찾을 수 없는 경우
     * @throws InvalidFarmRequestException 수정할 값이 없는 경우
     */
    @Transactional
    public FarmResponse updateFarm(Long farmId, UpdateFarmRequest request) {

        if (!request.hasAtLeastOneField()) {
            log.warn("농장 수정 실패: 수정할 필드 없음 - farmId = {}", farmId);
            throw new InvalidFarmRequestException(FarmErrorCode.NO_FIELD_TO_UPDATE);
        }

        Farm farm = findById(farmId);

        // 농장 이름 수정
        if (request.getFarmName() != null && !request.getFarmName().isEmpty()) {
            farm.updateFarmName(request.getFarmName());
        }

        // 주소 수정
        if (request.getZipCode() != null || request.getStreet() != null || request.getDetail() != null) {
            farm.updateAddress(request.getZipCode(), request.getStreet(), request.getDetail());
        }

        // 농장 크기 수정
        if (request.getFarmSize() != null && request.getFarmSize() > 0) {
            farm.updateFarmSize(request.getFarmSize());
        }

        return FarmResponse.from(farm);
    }

    /**
     * 농장 삭제
     * @param farmId 농장 ID
     * @throws FarmNotFoundException 농장을 찾을 수 없는 경우
     * @throws FarmAlreadyDeletedException 이미 삭제된 농장인 경우
     */
    @Transactional
    public void deleteFarm(Long farmId) {

        Farm farm = findById(farmId);

        if (farm.isDeleted()) {
            log.warn("이미 삭제된 농장: farmId = {}", farmId);
            throw new FarmAlreadyDeletedException(farmId);
        }

        farm.delete();
    }




}
