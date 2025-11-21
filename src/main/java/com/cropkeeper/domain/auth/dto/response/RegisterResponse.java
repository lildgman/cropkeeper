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
public class RegisterResponse {

    private Long memberId;
    private String username;
    private String name;
    private String role;

    public static RegisterResponse from(Member member) {

        return RegisterResponse.builder()
                .memberId(member.getMemberId())
                .username(member.getUsername())
                .name(member.getName())
                .role(member.getRole().name())
                .build();
    }
}
