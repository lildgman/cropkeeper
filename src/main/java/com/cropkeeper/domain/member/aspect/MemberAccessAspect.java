package com.cropkeeper.domain.member.aspect;

import com.cropkeeper.domain.member.annotation.ValidateMemberAccess;
import com.cropkeeper.domain.member.exception.ForbiddenMemberAccessException;
import com.cropkeeper.domain.member.exception.InvalidAspectConfigurationException;
import com.cropkeeper.domain.member.exception.MemberErrorCode;
import com.cropkeeper.global.aspect.AspectParameterExtractor;
import com.cropkeeper.global.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 회원 접근 권한 검증 AOP
 *
 * @ValidateMemberAccess 어노테이션이 붙은 메서드에 대해
 * PathVariable의 memberId와 인증된 사용자의 ID를 비교하여 권한을 검증합니다.
 */
@Slf4j
@Aspect
@Component
public class MemberAccessAspect {

    /**
     * @ValidateMemberAccess 어노테이션이 붙은 메서드 실행 전에 권한 검증을 수행합니다.
     *
     * 동작 방식:
     * 1. 메서드 파라미터에서 @PathVariable Long memberId를 찾습니다
     * 2. 메서드 파라미터에서 @AuthenticationPrincipal UserPrincipal을 찾습니다
     * 3. memberId와 userPrincipal.getId()가 일치하는지 검증합니다
     * 4. 일치하지 않으면 ForbiddenMemberAccessException 예외를 발생시킵니다
     *
     * @param joinPoint AOP 조인 포인트 (메서드 실행 정보)
     * @param validateMemberAccess 어노테이션 인스턴스
     * @throws ForbiddenMemberAccessException 권한이 없는 경우
     */
    @Before("@annotation(validateMemberAccess)")
    public void validateMemberAccess(JoinPoint joinPoint, ValidateMemberAccess validateMemberAccess) {
        // 메서드 정보 가져오기
        Method method = AspectParameterExtractor.getMethod(joinPoint);

        // 파라미터 추출 (공통 유틸리티 사용)
        Long requestedMemberId = AspectParameterExtractor.extractPathVariableAsLong(joinPoint, "memberId");
        UserPrincipal userPrincipal = AspectParameterExtractor.extractUserPrincipal(joinPoint);

        // 필수 파라미터 검증
        if (requestedMemberId == null || userPrincipal == null) {
            log.error("@ValidateMemberAccess를 사용하려면 메서드에 @PathVariable Long memberId와 " +
                    "@AuthenticationPrincipal UserPrincipal 파라미터가 필요합니다. " +
                    "메서드: {}", method.getName());
            throw new InvalidAspectConfigurationException(
                    MemberErrorCode.INVALID_ASPECT_CONFIGURATION,
                    "메서드: " + method.getName()
            );
        }

        // 작업명 결정 (공통 유틸리티 사용)
        String action = AspectParameterExtractor.getActionName(joinPoint, validateMemberAccess.action());

        // 권한 검증
        if (!requestedMemberId.equals(userPrincipal.getId())) {
            log.warn("권한 없는 {} 시도: 요청 memberId = {}, 실제 memberId = {}",
                    action, requestedMemberId, userPrincipal.getId());
            throw new ForbiddenMemberAccessException(MemberErrorCode.FORBIDDEN_ACCESS);
        }

        log.debug("회원 접근 권한 검증 성공: {} (memberId = {})", action, requestedMemberId);
    }
}
