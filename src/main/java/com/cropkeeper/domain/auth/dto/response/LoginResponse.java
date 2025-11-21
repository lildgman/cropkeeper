package com.cropkeeper.domain.auth.dto.response;

import com.cropkeeper.domain.member.entity.Member;
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
    private Long memberId;
    private String username;
    private String name;
    private String role;

    public static LoginResponse of(String accessToken, String refreshToken, Member member) {

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .memberId(member.getMemberId())
                .username(member.getUsername())
                .name(member.getName())
                .role(member.getRole().name())
                .build();
    }
}
