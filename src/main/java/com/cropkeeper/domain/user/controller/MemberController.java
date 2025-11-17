package com.cropkeeper.domain.user.controller;

import com.cropkeeper.domain.user.dto.MemberResponse;
import com.cropkeeper.domain.user.dto.UpdateMemberInfoRequest;
import com.cropkeeper.domain.user.entity.Member;
import com.cropkeeper.domain.user.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원 정보 조회 API
     * @param memberId 회원 ID
     * @return 200 OK + 회원정보
     */
    @GetMapping("/{memberId}")
    public ResponseEntity<MemberResponse> getMemberInfo(@PathVariable Long memberId) {

        log.info("회원 정보 조회 API 호출, memberId = {}", memberId);

        Member member = memberService.findById(memberId);
        MemberResponse response = MemberResponse.from(member);

        return ResponseEntity.ok(response);
    }

    /**
     * 회원 정보 수정 API
     * @param memberId 회원 ID
     * @param request 회원 정보 수정 요청 request
     * @return 200 OK + 수정된 회원 정보
     */
    @PutMapping("/{memberId}")
    public ResponseEntity<MemberResponse> updateMemberInfo(
            @PathVariable Long memberId,
            @Valid @RequestBody UpdateMemberInfoRequest request) {

        log.info("회원 정보 수정 API 호출 : MemberId = {}, name = {}, contact = {}",
                memberId, request.getName(), request.getContact());

        Member updatedMember = memberService.updateMemberInfo(memberId, request);
        MemberResponse response = MemberResponse.from(updatedMember);

        return ResponseEntity.ok(response);
    }
}
