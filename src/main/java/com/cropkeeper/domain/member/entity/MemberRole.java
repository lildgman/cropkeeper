package com.cropkeeper.domain.member.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 회원 권한
 */
@Getter
@RequiredArgsConstructor
public enum MemberRole {

    ADMIN("관리자", "시스템 전체 관리"),
    MANAGER("매니저", "농장 관리 및 데이터 관리"),
    USER("일반 사용자", "기본 농장 기능 사용");

    private final String displayName;
    private final String description;
}
