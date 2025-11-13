package com.cropkeeper.domain.user.dto;

import com.cropkeeper.domain.user.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 인증 응답 DTO (로그인/회원가입 성공 시)
 *
 * JWT 토큰과 사용자 기본 정보를 포함
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;  // JWT 토큰
    private String tokenType;    // 토큰 타입 (기본: "Bearer")
    private Long userId;         // 사용자 ID
    private String username;     // 사용자 이름 (로그인 ID)
    private String name;         // 이름
    private String role;         // 권한 (USER, ADMIN 등)

    /**
     * User 엔티티와 JWT 토큰으로 AuthResponse 생성
     */
    public static AuthResponse of(String accessToken, Users users) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .userId(users.getUserId())
                .username(users.getUsername())
                .name(users.getName())
                .role(users.getRole().name())
                .build();
    }
}
