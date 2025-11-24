package com.cropkeeper.domain.farm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 농장 접근 권한 검증을 위한 커스텀 어노테이션
 *
 * 이 어노테이션이 붙은 메서드는 AOP를 통해 자동으로 농장 접근 권한을 검증합니다.
 * PathVariable로 전달된 farmId의 소유자와 인증된 사용자의 ID가 일치하는지 확인합니다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateFarmAccess {

    /**
     * 로깅 및 예외 메시지에 사용될 작업명
     * 기본값은 빈 문자열이며, 이 경우 메서드명이 사용됩니다.
     *
     * @return 작업명 (예: "농장 조회", "농장 수정")
     */
    String action() default "";
}
