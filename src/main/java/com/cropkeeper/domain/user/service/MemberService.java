package com.cropkeeper.domain.user.service;

import com.cropkeeper.domain.user.dto.UpdateMemberInfoRequest;
import com.cropkeeper.domain.user.entity.Member;
import com.cropkeeper.domain.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원 ID로 회원 조회
     * @param memberId
     * @return 회원 엔티티
     * @throws IllegalArgumentException 회원을 찾을 수 없는 경우
     */
    public Member findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));
    }

    /**
     * 회원 정보 수정
     * @param memberId 회원 id
     * @param request 수정할 회원 정보
     * @return 수정된 회원 엔티티
     * @throws IllegalArgumentException 회원을 찾을 수 없거나 수정할 필드가 없는 경우
     */
    @Transactional
    public Member updateMemberInfo(Long memberId, UpdateMemberInfoRequest request) {

        if (!request.hasLeastOneField()) {
            throw new IllegalArgumentException("수정할 정보가 없습니다. 이름 또는 연락처를 입력해주세요.");
        }

        Member member = findById(memberId);

        member.updateInfo(request.getName(), request.getContact());

        log.info("회원 정보 수정 완료: memberId = {}, name = {}, contact = {}",
                memberId, request.getName(), request.getContact());

        return member;
    }



}
