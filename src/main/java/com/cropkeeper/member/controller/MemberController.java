package com.cropkeeper.member.controller;

import com.cropkeeper.member.annotation.ValidateMemberAccess;
import com.cropkeeper.member.dto.response.MemberResponse;
import com.cropkeeper.member.dto.request.UpdateMemberInfoRequest;
import com.cropkeeper.member.dto.request.UpdatePasswordRequest;
import com.cropkeeper.member.service.MemberService;
import com.cropkeeper.common.security.UserPrincipal;
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
     * @param userPrincipal 인증된 사용자 정보
     * @return 200 OK + 회원정보
     */
    @GetMapping("/{memberId}")
    @ValidateMemberAccess(action = "회원 정보 조회")
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
     * @param request       회원 정보 수정 요청 request
     * @param userPrincipal 인증된 사용자 정보
     * @return 200 OK + 수정된 회원 정보
     */
    @PutMapping("/{memberId}")
    @ValidateMemberAccess(action = "회원 정보 수정")
    public ResponseEntity<MemberResponse> updateMemberInfo(
            @PathVariable Long memberId,
            @Valid @RequestBody UpdateMemberInfoRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        MemberResponse response = memberService.updateMemberInfo(memberId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 비밀번호 변경 API
     *
     * @param memberId      회원 ID
     * @param request       비밀번호 변경 요청
     * @param userPrincipal 인증된 사용자 정보
     * @return No Content
     */
    @PatchMapping("/{memberId}/password")
    @ValidateMemberAccess(action = "비밀번호 변경")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long memberId,
            @Valid @RequestBody UpdatePasswordRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        memberService.changePassword(memberId, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * 회원 탈퇴 API
     * @param memberId 회원 ID
     * @param userPrincipal 인증된 사용자 정보
     * @return 204 No Content
     */
    @DeleteMapping("/{memberId}")
    @ValidateMemberAccess(action = "회원 탈퇴")
    public ResponseEntity<Void> deleteMember(
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        memberService.deleteMember(memberId);
        return ResponseEntity.noContent().build();
    }
}
