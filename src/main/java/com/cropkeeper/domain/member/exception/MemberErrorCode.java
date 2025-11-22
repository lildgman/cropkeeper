package com.cropkeeper.domain.member.exception;

import com.cropkeeper.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {

    MEMBER_NOT_FOUND("M001", "회원을 찾을 수 없습니다."),

    DUPLICATE_USERNAME("M002", "이미 사용 중인 사용자 이름입니다."),
    PASSWORD_MISMATCH("M003", "비밀번호가 일치하지 않습니다."),

    CURRENT_PASSWORD_MISMATCH("M004", "현재 비밀번호가 일치하지 않습니다."),
    NEW_PASSWORD_MISMATCH("M005", "새 비밀번호가 일치하지 않습니다."),
    SAME_AS_CURRENT_PASSWORD("M006", "새 비밀번호는 현재 비밀번호와 달라야합니다."),

    FORBIDDEN_ACCESS("M007", "접근 권한이 없습니다."),

    NO_FIELD_TO_UPDATE("M008", "수정할 정보가 없습니다. 수정할 이름 또는 연락처를 입력해주세요."),

    ALREADY_DELETED("M009", "이미 탈퇴한 회원입니다."),

    INVALID_ASPECT_CONFIGURATION("M010", "@ValidateMemberAccess를 사용하려면 메서드에 @PathVariable Long memberId와 @AuthenticationPrincipal UserPrincipal 파라미터가 필요합니다.");

    private final String code;
    private final String message;

}
