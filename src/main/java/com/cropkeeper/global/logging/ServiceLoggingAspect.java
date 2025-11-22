package com.cropkeeper.global.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * Service 레이어 자동 로깅 AOP
 *
 * 모든 @Service의 public 메서드를 자동으로 로깅합니다.
 *
 * 로그 형식:
 * - [SVC-IN] {ClassName}.{methodName} | params=[...]
 * - [SVC-OUT] {ClassName}.{methodName} | duration={ms}ms | status=SUCCESS
 * - [SVC-ERR] {ClassName}.{methodName} | duration={ms}ms | exception={class}
 *
 * 느린 메서드 감지:
 * - 1초 이상 소요 시 WARN 레벨로 경고 출력
 */
@Slf4j
@Aspect
@Component
public class ServiceLoggingAspect {

    // 느린 메서드 임계값 (밀리초)
    private static final long SLOW_METHOD_THRESHOLD_MS = 1000;

    /**
     * 모든 @Service의 public 메서드를 대상으로 AOP 적용
     * private 메서드는 제외 (내부 헬퍼 메서드는 로깅 불필요)
     * @NoLogging 어노테이션이 있으면 로깅 제외
     */
    @Around("execution(public * com.cropkeeper.domain..service.*Service.*(..))")
    public Object logServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        // @NoLogging 체크
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        if (signature.getMethod().isAnnotationPresent(NoLogging.class)) {
            // 로깅 없이 바로 실행
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();

        // 메서드 정보 추출
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        String fullMethodName = className + "." + methodName;

        // 파라미터 마스킹
        String params = SensitiveDataMasker.maskParameters(joinPoint.getArgs());

        // Service 메서드 시작 로그
        logServiceStart(fullMethodName, params);

        // 실제 메서드 실행
        Object result = null;
        Throwable exception = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Service 메서드 종료 로그
            if (exception == null) {
                logServiceSuccess(fullMethodName, duration, result);
            } else {
                logServiceError(fullMethodName, duration, exception);
            }
        }
    }

    /**
     * Service 메서드 시작 로그
     */
    private void logServiceStart(String fullMethodName, String params) {
        log.info("[SVC-IN] {}\n  └─ params={}", fullMethodName, params);
    }

    /**
     * Service 메서드 정상 종료 로그
     */
    private void logServiceSuccess(String fullMethodName, long duration, Object result) {
        StringBuilder logMsg = new StringBuilder();
        logMsg.append("[SVC-OUT] ").append(fullMethodName);
        logMsg.append("\n  └─ duration=").append(duration).append("ms | status=SUCCESS");

        // 느린 메서드 감지
        if (duration >= SLOW_METHOD_THRESHOLD_MS) {
            logMsg.append(" ⚠️ SLOW METHOD DETECTED!");
            logMsg.append("\n  └─ threshold=").append(SLOW_METHOD_THRESHOLD_MS).append("ms");
            log.warn(logMsg.toString());
        } else {
            log.info(logMsg.toString());
        }
    }

    /**
     * Service 메서드 예외 발생 로그
     */
    private void logServiceError(String fullMethodName, long duration, Throwable exception) {
        StringBuilder logMsg = new StringBuilder();
        logMsg.append("[SVC-ERR] ").append(fullMethodName);
        logMsg.append("\n  └─ duration=").append(duration).append("ms | status=FAIL");
        logMsg.append(" | exception=").append(exception.getClass().getSimpleName());
        logMsg.append("\n  └─ message=").append(exception.getMessage());

        // 예외 타입에 따라 로그 레벨 결정
        if (isBusinessException(exception)) {
            // 비즈니스 예외는 WARN (예상된 예외)
            log.warn(logMsg.toString());
        } else {
            // 시스템 예외는 ERROR (예상치 못한 예외)
            log.error(logMsg.toString());
        }
    }

    /**
     * 비즈니스 예외인지 확인
     * (BaseException을 상속한 예외는 비즈니스 예외로 간주)
     */
    private boolean isBusinessException(Throwable exception) {
        return exception.getClass().getName().contains("cropkeeper")
                && (exception.getClass().getName().contains("Exception"));
    }
}
