package com.cropkeeper.domain.member.controller;

import com.cropkeeper.domain.member.annotation.ValidateMemberAccess;
import com.cropkeeper.domain.member.dto.response.MemberResponse;
import com.cropkeeper.domain.member.dto.request.UpdateMemberInfoRequest;
import com.cropkeeper.domain.member.dto.request.UpdatePasswordRequest;
import com.cropkeeper.domain.member.service.MemberService;
import com.cropkeeper.global.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원 정보 조회 API
     *
     * @param memberId      회원 ID
     * @param userPrincipal 현재 로그인한 사용자 정보
     * @return 200 OK + 회원정보
     */
    @ValidateMemberAccess(action = "회원 정보 조회")
    @GetMapping("/{memberId}")
    public ResponseEntity<MemberResponse> getMemberInfo(
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        MemberResponse response = memberService.getMemberInfo(memberId);
        return ResponseEntity.ok(response);
    }

    /**
     * 회원 정보 수정 API
     *
     * @param memberId      회원 ID
     * @param userPrincipal 현재 로그인한 사용자 정보
     * @param request       회원 정보 수정 요청 request
     * @return 200 OK + 수정된 회원 정보
     */
    @ValidateMemberAccess(action = "회원 정보 수정")
    @PutMapping("/{memberId}")
    public ResponseEntity<MemberResponse> updateMemberInfo(
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateMemberInfoRequest request) {

        MemberResponse response = memberService.updateMemberInfo(memberId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 비밀번호 변경 API
     *
     * @param memberId      회원ID
     * @param userPrincipal 현재 로그인한 사용자 정보
     * @param request       비밀번호 변경 요청
     * @return No Content
     */
    @ValidateMemberAccess(action = "비밀번호 변경")
    @PatchMapping("/{memberId}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UpdatePasswordRequest request) {

        memberService.changePassword(memberId, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * 회원 탈퇴 API
     * @param memberId 회원 ID
     * @param userPrincipal 현재 로그인한 사용자 정보
     * @return 204 No Content
     */
    @ValidateMemberAccess(action = "회원 탈퇴")
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteMember(
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        memberService.deleteMember(memberId);
        return ResponseEntity.noContent().build();
    }
}
