package com.cropkeeper.domain.farm.service;

import com.cropkeeper.domain.farm.dto.request.CreateFarmRequest;
import com.cropkeeper.domain.farm.dto.request.UpdateFarmRequest;
import com.cropkeeper.domain.farm.dto.response.FarmResponse;
import com.cropkeeper.domain.farm.entity.Farm;
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

        log.info("농장 생성 시도: memberId = {}, farmName = {}", memberId, request.getFarmName());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        Farm farm = Farm.builder()
                .farmName(request.getFarmName())
                .address(request.getAddress())
                .farmSize(request.getFarmSize())
                .member(member)
                .build();

        Farm savedFarm = farmRepository.save(farm);

        log.info("농장 생성 완료: farmId = {}, farmName = {}", savedFarm.getFarmId(), savedFarm.getFarmName());

        return FarmResponse.from(farm);
    }

    /**
     * 특정 회원의 모든 농장 조회
     * @param memberId
     * @return 농장 DTO 목록
     */
    public List<FarmResponse> findAllByMemberId(Long memberId) {

        log.info("회원의 농장 목록 조회: memberId = {}", memberId);

        List<Farm> farms = farmRepository.findByMemberId(memberId);

        return farms.stream()
                .map(FarmResponse::from)
                .collect(Collectors.toList());
    }

    public FarmResponse findById(Long farmId) {

        log.info("농장 조회: farmId = {}", farmId);

        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new FarmNotFoundException(farmId));

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

        log.info("농장 정보 수정 시도: farmId = {}", farmId);

        if (!request.hasAtLeastOneField()) {
            throw new InvalidFarmRequestException(FarmErrorCode.NO_FIELD_TO_UPDATE);
        }

        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new FarmNotFoundException(farmId));

        farm.updateInfo(request.getFarmName(), request.getAddress(), request.getFarmSize());

        log.info("농장 정보 수정 완료: farmId = {}, farmName = {}, address = {}, farmSize = {}",
                farmId, request.getFarmName(), request.getAddress(), request.getFarmSize());

        return FarmResponse.from(farm);
    }

    @Transactional
    public void deleteFarm(Long farmId) {

        log.info("농장 삭제 시도: farmId = {}", farmId);

        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new FarmNotFoundException(farmId));

        farmRepository.delete(farm);

        log.info("농장 삭제 완료: farmId = {}", farmId);

    }




}
