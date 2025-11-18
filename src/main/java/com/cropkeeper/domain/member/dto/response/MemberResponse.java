package com.cropkeeper.domain.member.dto.response;

import com.cropkeeper.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberResponse {

    private Long memberId;
    private String username;
    private String name;
    private String contact;
    private String role;

    public static MemberResponse from(Member member) {

        return MemberResponse.builder()
                .memberId(member.getMemberId())
                .username(member.getUsername())
                .name(member.getName())
                .contact(member.getContact())
                .role(member.getRole().name())
                .build();
    }
}
