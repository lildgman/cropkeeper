package com.cropkeeper.global.logging;

import com.cropkeeper.global.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Controller 레이어 자동 로깅 AOP
 *
 * 모든 @RestController의 요청/응답을 자동으로 로깅합니다.
 *
 * 로그 형식:
 * - [API-REQ] {METHOD} {URI} | user={username} | userId={id} | ip={ip} | params={...}
 * - [API-RES] {METHOD} {URI} | status={code} | duration={ms}ms
 * - [API-ERR] {METHOD} {URI} | status={code} | duration={ms}ms | exception={class}
 */
@Slf4j
@Aspect
@Component
public class ControllerLoggingAspect {

    /**
     * 모든 @RestController의 public 메서드를 대상으로 AOP 적용
     * @NoLogging 어노테이션이 있으면 로깅 제외
     */
    @Around("execution(public * com.cropkeeper.domain..controller.*Controller.*(..))")
    public Object logApiRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        // @NoLogging 체크
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (method.isAnnotationPresent(NoLogging.class)) {
            // 로깅 없이 바로 실행
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();

        // HTTP 요청 정보 가져오기
        HttpServletRequest request = getHttpServletRequest();
        if (request == null) {
            // HTTP 요청이 아닌 경우 (테스트 등) 로깅 없이 실행
            return joinPoint.proceed();
        }

        String httpMethod = request.getMethod();
        String uri = request.getRequestURI();
        String clientIp = getClientIp(request);

        // 인증 정보 가져오기
        UserPrincipal userPrincipal = extractUserPrincipal(joinPoint);
        String userInfo = formatUserInfo(userPrincipal);

        // 파라미터 정보 가져오기
        String params = extractParameters(joinPoint);

        // Request Body 가져오기 (POST, PUT, PATCH만)
        String requestBody = "";
        if (httpMethod.equals("POST") || httpMethod.equals("PUT") || httpMethod.equals("PATCH")) {
            requestBody = extractRequestBody(joinPoint);
        }

        // 요청 시작 로그
        logApiRequest(httpMethod, uri, userInfo, clientIp, params, requestBody);

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

            // 응답 로그
            if (exception == null) {
                logApiResponse(httpMethod, uri, result, duration);
            } else {
                logApiError(httpMethod, uri, exception, duration);
            }
        }
    }

    /**
     * API 요청 시작 로그 출력
     */
    private void logApiRequest(String httpMethod, String uri, String userInfo,
                                String clientIp, String params, String requestBody) {
        StringBuilder logMsg = new StringBuilder();
        logMsg.append("[API-REQ] ").append(httpMethod).append(" ").append(uri);
        logMsg.append("\n  └─ ").append(userInfo).append(" | ip=").append(clientIp);

        if (!params.isEmpty()) {
            logMsg.append(" | params=").append(params);
        }

        if (!requestBody.isEmpty()) {
            logMsg.append("\n  └─ body=").append(requestBody);
        }

        log.info(logMsg.toString());
    }

    /**
     * API 응답 성공 로그 출력
     */
    private void logApiResponse(String httpMethod, String uri, Object result, long duration) {
        int statusCode = extractStatusCode(result);
        String statusText = getStatusText(statusCode);

        StringBuilder logMsg = new StringBuilder();
        logMsg.append("[API-RES] ").append(httpMethod).append(" ").append(uri);
        logMsg.append("\n  └─ status=").append(statusCode).append(" ").append(statusText);
        logMsg.append(" | duration=").append(duration).append("ms");

        // 응답 크기 추정 (선택적)
        if (result != null) {
            logMsg.append(" | responseType=").append(result.getClass().getSimpleName());
        }

        log.info(logMsg.toString());
    }

    /**
     * API 에러 로그 출력
     */
    private void logApiError(String httpMethod, String uri, Throwable exception, long duration) {
        StringBuilder logMsg = new StringBuilder();
        logMsg.append("[API-ERR] ").append(httpMethod).append(" ").append(uri);
        logMsg.append("\n  └─ duration=").append(duration).append("ms");
        logMsg.append(" | exception=").append(exception.getClass().getSimpleName());
        logMsg.append("\n  └─ message=").append(exception.getMessage());

        log.error(logMsg.toString());
    }

    /**
     * HttpServletRequest 가져오기
     */
    private HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 클라이언트 IP 주소 가져오기 (프록시 고려)
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 메서드 파라미터에서 UserPrincipal 추출
     */
    private UserPrincipal extractUserPrincipal(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(AuthenticationPrincipal.class)) {
                if (args[i] instanceof UserPrincipal) {
                    return (UserPrincipal) args[i];
                }
            }
        }
        return null;
    }

    /**
     * UserPrincipal 정보를 포맷팅
     */
    private String formatUserInfo(UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return "user=anonymous";
        }
        return "user=" + userPrincipal.getUsername() + " | userId=" + userPrincipal.getId();
    }

    /**
     * @PathVariable 파라미터 추출
     */
    private String extractParameters(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        StringBuilder params = new StringBuilder();
        params.append("{");

        int count = 0;
        for (int i = 0; i < parameters.length; i++) {
            PathVariable pathVariable = parameters[i].getAnnotation(PathVariable.class);
            RequestParam requestParam = parameters[i].getAnnotation(RequestParam.class);

            if (pathVariable != null) {
                if (count > 0) params.append(", ");
                String name = pathVariable.value().isEmpty()
                        ? parameters[i].getName()
                        : pathVariable.value();
                params.append(name).append("=").append(args[i]);
                count++;
            } else if (requestParam != null) {
                if (count > 0) params.append(", ");
                String name = requestParam.value().isEmpty()
                        ? parameters[i].getName()
                        : requestParam.value();
                params.append(name).append("=").append(args[i]);
                count++;
            }
        }

        params.append("}");
        return count > 0 ? params.toString() : "";
    }

    /**
     * @RequestBody 파라미터 추출 및 마스킹
     */
    private String extractRequestBody(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(RequestBody.class)) {
                // 민감정보 마스킹 후 반환
                return SensitiveDataMasker.maskSensitiveData(args[i]);
            }
        }
        return "";
    }

    /**
     * ResponseEntity에서 HTTP 상태 코드 추출
     */
    private int extractStatusCode(Object result) {
        if (result instanceof ResponseEntity) {
            return ((ResponseEntity<?>) result).getStatusCode().value();
        }
        return 200; // 기본값
    }

    /**
     * HTTP 상태 코드에 대한 텍스트 반환
     */
    private String getStatusText(int statusCode) {
        return switch (statusCode) {
            case 200 -> "OK";
            case 201 -> "CREATED";
            case 204 -> "NO_CONTENT";
            case 400 -> "BAD_REQUEST";
            case 401 -> "UNAUTHORIZED";
            case 403 -> "FORBIDDEN";
            case 404 -> "NOT_FOUND";
            case 500 -> "INTERNAL_SERVER_ERROR";
            default -> "";
        };
    }
}
