package com.cropkeeper.domain.user.dto;

import com.cropkeeper.domain.user.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long userId;
    private String username;
    private String name;
    private String role;

    public static LoginResponse of(String accessToken, String refreshToken, Member user) {

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getMemberId())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }
}
