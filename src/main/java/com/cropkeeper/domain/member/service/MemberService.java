package com.cropkeeper.domain.member.service;

import com.cropkeeper.domain.member.dto.request.UpdateMemberInfoRequest;
import com.cropkeeper.domain.member.dto.request.UpdatePasswordRequest;
import com.cropkeeper.domain.member.dto.response.MemberResponse;
import com.cropkeeper.domain.member.entity.Member;
import com.cropkeeper.domain.member.exception.*;
import com.cropkeeper.domain.member.repository.MemberRepository;
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
     * @throws MemberNotFoundException 회원을 찾을 수 없는 경우
     */
    public Member findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
    }

    /**
     * 회원 정보 수정
     * @param memberId 회원 id
     * @param request 수정할 회원 정보
     * @return 수정된 회원 엔티티
     * @throws MemberNotFoundException 회원을 찾을 수 없는 경우
     * @throws InvalidMemberRequestException 수정할 필드가 없는 경우
     */
    @Transactional
    public MemberResponse updateMemberInfo(Long memberId, UpdateMemberInfoRequest request) {

        if (!request.hasLeastOneField()) {
            throw new InvalidMemberRequestException(MemberErrorCode.NO_FIELD_TO_UPDATE);
        }

        Member member = findById(memberId);

        member.updateInfo(request.getName(), request.getContact());

        log.info("회원 정보 수정 완료: memberId = {}, name = {}, contact = {}",
                memberId, request.getName(), request.getContact());

        return MemberResponse.from(member);
    }

    /**
     * 비밀번호 변경
     * @param memberId 회원 ID
     * @param request 비밀번호 변경 요청
     * @throws MemberNotFoundException 회원을 찾을 수 없는 경우
     * @throws PasswordMismatchException 비밀번호가 일치하지 않는 경우
     * @throws InvalidMemberRequestException 새 비밀번호가 현재 비밀번호와 동일한 경우
     */
    @Transactional
    public void changePassword(Long memberId, UpdatePasswordRequest request) {

        log.info("비밀번호 변경 시도: memberId = {}", memberId);

        Member member = findById(memberId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
            log.warn("비밀번호 변경 실패: 현재 비밀번호 불일치 - memberId = {}", memberId);
            throw new PasswordMismatchException(MemberErrorCode.CURRENT_PASSWORD_MISMATCH);
        }

        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            log.warn("비밀번호 변경 실패: 새 비밀번호 불일치 - memberId = {}", memberId);
            throw new PasswordMismatchException(MemberErrorCode.NEW_PASSWORD_MISMATCH);
        }

        if (passwordEncoder.matches(request.getNewPassword(), member.getPassword())) {
            log.warn("비밀번호 변경 실패: 새 비밀번호가 현재 비밀번호와 동일 - memberId = {}", memberId);
            throw new InvalidMemberRequestException(MemberErrorCode.SAME_AS_CURRENT_PASSWORD);
        }

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        member.changePassword(encodedPassword);

        log.info("비밀번호 변경 완료: memberId = {}", memberId);

    }

    /**
     * 회원 탈퇴
     * @param memberId 회원 ID
     * @throws MemberNotFoundException 회원을 찾을 수 없는 경우
     * @throws AlreadyDeletedException 이미 탈퇴한 회원인 경우
     */
    @Transactional
    public void deleteMember(Long memberId) {

        log.info("회원 탈퇴 시도: memberId = {}", memberId);

        Member member = findById(memberId);

        if (member.isDeleted()) {
            log.warn("이미 탈퇴한 회원: memberId = {}", memberId);
            throw new AlreadyDeletedException(memberId);
        }

        member.delete();

        log.info("회원 탈퇴 완료: memberId = {}, deletedAt = {}",
                memberId, member.getDeletedAt());
    }





}
