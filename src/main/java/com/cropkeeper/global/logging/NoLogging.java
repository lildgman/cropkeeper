package com.cropkeeper.global.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 로깅을 제외할 메서드에 적용하는 어노테이션
 *
 * Controller 또는 Service 메서드에 이 어노테이션을 붙이면
 * AOP 자동 로깅에서 제외됩니다.
 *
 * 사용 사례:
 * - 너무 자주 호출되는 메서드 (헬스체크, 폴링 등)
 * - 민감한 정보를 다루는 메서드 (로그에 남기지 말아야 할 경우)
 * - 내부 헬퍼 메서드 (이미 private이면 제외되지만, public인 경우)
 *
 * 사용 예시:
 * <pre>
 * {@code
 * @NoLogging
 * @GetMapping("/health")
 * public ResponseEntity<String> healthCheck() {
 *     return ResponseEntity.ok("OK");
 * }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NoLogging {
    /**
     * 로깅을 제외하는 이유 (선택적, 문서화 목적)
     */
    String reason() default "";
}
