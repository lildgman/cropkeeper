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
public class RegisterResponse {

    private Long userId;
    private String username;
    private String name;
    private String role;

    public static RegisterResponse from(Member user) {

        return RegisterResponse.builder()
                .userId(user.getMemberId())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }
}
