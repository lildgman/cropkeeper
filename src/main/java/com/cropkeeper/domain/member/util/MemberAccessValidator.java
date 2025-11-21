package com.cropkeeper.domain.member.util;

import com.cropkeeper.domain.member.exception.ForbiddenMemberAccessException;
import com.cropkeeper.domain.member.exception.MemberErrorCode;
import com.cropkeeper.global.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 회원 접근 권한 검증 유틸리티
 */
@Slf4j
@Component
public class MemberAccessValidator {

    /**
     * 요청한 회원과 인증된 회원이 동일한지 검증
     *
     * @param requestedMemberId 요청한 회원 ID
     * @param authenticatedMemberId 인증된 회원 ID
     * @param action 수행하려는 작업 (로깅용)
     * @throws ForbiddenMemberAccessException 권한이 없는 경우
     */
    public void validateMemberAccess(Long requestedMemberId, Long authenticatedMemberId, String action) {
        if (!requestedMemberId.equals(authenticatedMemberId)) {
            log.warn("권한 없는 {} 시도: 요청 memberId = {}, 실제 memberId = {}",
                    action, requestedMemberId, authenticatedMemberId);
            throw new ForbiddenMemberAccessException(MemberErrorCode.FORBIDDEN_ACCESS);
        }
    }

    /**
     * UserPrincipal을 사용한 검증
     */
    public void validateMemberAccess(Long requestedMemberId, UserPrincipal userPrincipal, String action) {
        validateMemberAccess(requestedMemberId, userPrincipal.getId(), action);
    }
}
