package com.cropkeeper.domain.farm.aspect;

import com.cropkeeper.domain.farm.annotation.ValidateFarmAccess;
import com.cropkeeper.domain.farm.entity.Farm;
import com.cropkeeper.domain.farm.exception.FarmErrorCode;
import com.cropkeeper.domain.farm.exception.FarmNotFoundException;
import com.cropkeeper.domain.farm.exception.ForbiddenFarmAccessException;
import com.cropkeeper.domain.farm.repository.FarmRepository;
import com.cropkeeper.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 농장 접근 권한 검증 AOP
 *
 * @ValidateFarmAccess 어노테이션이 붙은 메서드에 대해
 * PathVariable의 farmId로 농장을 조회하고, 해당 농장의 소유자와 인증된 사용자의 ID를 비교하여 권한을 검증합니다.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class FarmAccessAspect {

    private final FarmRepository farmRepository;

    /**
     * @ValidateFarmAccess 어노테이션이 붙은 메서드 실행 전에 권한 검증을 수행합니다.
     *
     * 동작 방식:
     * 1. 메서드 파라미터에서 @PathVariable Long farmId를 찾습니다
     * 2. 메서드 파라미터에서 @AuthenticationPrincipal UserPrincipal을 찾습니다
     * 3. farmId로 Farm을 조회합니다
     * 4. farm.getMember().getMemberId()와 userPrincipal.getId()가 일치하는지 검증합니다
     * 5. 일치하지 않으면 ForbiddenFarmAccessException 예외를 발생시킵니다
     *
     * @param joinPoint AOP 조인 포인트 (메서드 실행 정보)
     * @param validateFarmAccess 어노테이션 인스턴스
     * @throws ForbiddenFarmAccessException 권한이 없는 경우
     * @throws FarmNotFoundException 농장을 찾을 수 없는 경우
     */
    @Before("@annotation(validateFarmAccess)")
    public void validateFarmAccess(JoinPoint joinPoint, ValidateFarmAccess validateFarmAccess) {
        // 메서드 시그니처 정보 가져오기
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        Long requestedFarmId = null;
        UserPrincipal userPrincipal = null;

        // 메서드 파라미터를 순회하면서 필요한 값 추출
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            // @PathVariable Long farmId 찾기
            if (parameter.isAnnotationPresent(PathVariable.class) &&
                    parameter.getType().equals(Long.class) &&
                    parameter.getName().equals("farmId")) {
                requestedFarmId = (Long) args[i];
            }

            // @AuthenticationPrincipal UserPrincipal 찾기
            if (parameter.isAnnotationPresent(AuthenticationPrincipal.class) &&
                    parameter.getType().equals(UserPrincipal.class)) {
                userPrincipal = (UserPrincipal) args[i];
            }
        }

        // 필수 파라미터 검증
        if (requestedFarmId == null || userPrincipal == null) {
            log.error("@ValidateFarmAccess를 사용하려면 메서드에 @PathVariable Long farmId와 " +
                    "@AuthenticationPrincipal UserPrincipal 파라미터가 필요합니다. " +
                    "메서드: {}", method.getName());
            throw new IllegalStateException(
                    "@ValidateFarmAccess를 사용하려면 메서드에 @PathVariable Long farmId와 " +
                    "@AuthenticationPrincipal UserPrincipal 파라미터가 필요합니다."
            );
        }

        // 농장 조회
        Farm farm = farmRepository.findById(requestedFarmId)
                .orElseThrow(() -> new FarmNotFoundException(requestedFarmId));

        // 작업명 결정
        String action = validateFarmAccess.action().isEmpty()
                ? method.getName()
                : validateFarmAccess.action();

        // 권한 검증: 농장 소유자와 현재 사용자가 동일한지 확인
        Long farmOwnerId = farm.getMember().getMemberId();
        Long currentUserId = userPrincipal.getId();

        if (!farmOwnerId.equals(currentUserId)) {
            log.warn("권한 없는 {} 시도: 요청 farmId = {}, 농장 소유자 = {}, 현재 사용자 = {}",
                    action, requestedFarmId, farmOwnerId, currentUserId);
            throw new ForbiddenFarmAccessException();
        }

        log.debug("농장 접근 권한 검증 성공: {} (farmId = {}, memberId = {})",
                action, requestedFarmId, currentUserId);
    }
}
