package com.cropkeeper.global.aspect;

import com.cropkeeper.global.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * AOP에서 사용하는 공통 파라미터 추출 유틸리티
 *
 * 주요 기능:
 * - @PathVariable 파라미터 추출
 * - @AuthenticationPrincipal UserPrincipal 추출
 * - 작업명(action) 결정
 */
@Slf4j
public class AspectParameterExtractor {

    /**
     * JoinPoint에서 특정 이름의 @PathVariable Long 파라미터를 추출합니다.
     *
     * @param joinPoint     AOP 조인 포인트
     * @param parameterName 찾을 파라미터 이름 (예: "memberId", "farmId")
     * @return 추출된 Long 값, 찾지 못하면 null
     */
    public static Long extractPathVariableAsLong(JoinPoint joinPoint, String parameterName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            // @PathVariable Long {parameterName} 찾기
            if (parameter.isAnnotationPresent(PathVariable.class) &&
                    parameter.getType().equals(Long.class) &&
                    parameter.getName().equals(parameterName)) {
                return (Long) args[i];
            }
        }

        return null;
    }

    /**
     * JoinPoint에서 @AuthenticationPrincipal UserPrincipal 파라미터를 추출합니다.
     *
     * @param joinPoint AOP 조인 포인트
     * @return 추출된 UserPrincipal, 찾지 못하면 null
     */
    public static UserPrincipal extractUserPrincipal(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            // @AuthenticationPrincipal UserPrincipal 찾기
            if (parameter.isAnnotationPresent(AuthenticationPrincipal.class) &&
                    parameter.getType().equals(UserPrincipal.class)) {
                return (UserPrincipal) args[i];
            }
        }

        return null;
    }

    /**
     * 작업명(action)을 결정합니다.
     * 어노테이션에 명시된 action이 있으면 사용하고, 없으면 메서드명을 사용합니다.
     *
     * @param joinPoint      AOP 조인 포인트
     * @param annotationAction 어노테이션에 지정된 action 값
     * @return 작업명
     */
    public static String getActionName(JoinPoint joinPoint, String annotationAction) {
        if (annotationAction != null && !annotationAction.isEmpty()) {
            return annotationAction;
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod().getName();
    }

    /**
     * JoinPoint에서 Method 객체를 추출합니다.
     *
     * @param joinPoint AOP 조인 포인트
     * @return Method 객체
     */
    public static Method getMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod();
    }

    /**
     * 필수 파라미터가 null인지 검증하고 에러 로그를 출력합니다.
     *
     * @param value          검증할 값
     * @param parameterName  파라미터 이름 (로깅용)
     * @param annotationName 어노테이션 이름 (로깅용)
     * @param method         메서드 객체 (로깅용)
     * @return 값이 null이면 true, 아니면 false
     */
    public static boolean isParameterMissing(Object value, String parameterName,
                                              String annotationName, Method method) {
        if (value == null) {
            log.error("{}를 사용하려면 메서드에 {} 파라미터가 필요합니다. 메서드: {}",
                    annotationName, parameterName, method.getName());
            return true;
        }
        return false;
    }

    /**
     * 여러 필수 파라미터를 검증합니다.
     *
     * @param annotationName 어노테이션 이름
     * @param method         메서드 객체
     * @param parameters     검증할 파라미터들 (이름, 값) 쌍
     * @throws IllegalStateException 필수 파라미터가 누락된 경우
     */
    public static void validateRequiredParameters(String annotationName, Method method,
                                                   ParameterPair... parameters) {
        StringBuilder missingParams = new StringBuilder();

        for (ParameterPair param : parameters) {
            if (param.value == null) {
                if (missingParams.length() > 0) {
                    missingParams.append(", ");
                }
                missingParams.append(param.description);
            }
        }

        if (missingParams.length() > 0) {
            String errorMsg = String.format("%s를 사용하려면 메서드에 다음 파라미터가 필요합니다: %s (메서드: %s)",
                    annotationName, missingParams, method.getName());
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
    }

    /**
     * 파라미터 이름과 값을 묶는 헬퍼 클래스
     */
    public static class ParameterPair {
        private final String description;
        private final Object value;

        public ParameterPair(String description, Object value) {
            this.description = description;
            this.value = value;
        }

        public static ParameterPair of(String description, Object value) {
            return new ParameterPair(description, value);
        }
    }
}
