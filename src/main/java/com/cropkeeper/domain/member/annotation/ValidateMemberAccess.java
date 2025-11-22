package com.cropkeeper.domain.member.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 회원 접근 권한 검증을 위한 커스텀 어노테이션
 *
 * 이 어노테이션이 붙은 메서드는 AOP를 통해 자동으로 회원 접근 권한을 검증합니다.
 * PathVariable로 전달된 memberId와 인증된 사용자의 ID가 일치하는지 확인합니다.
 *
 */
@Target(ElementType.METHOD)  // 메서드에만 적용 가능
@Retention(RetentionPolicy.RUNTIME)  // 런타임에 어노테이션 정보 유지
public @interface ValidateMemberAccess {

    /**
     * 로깅 및 예외 메시지에 사용될 작업명
     * 기본값은 빈 문자열이며, 이 경우 메서드명이 사용됩니다.
     *
     * @return 작업명 (예: "회원 정보 조회", "비밀번호 변경")
     */
    String action() default "";
}
