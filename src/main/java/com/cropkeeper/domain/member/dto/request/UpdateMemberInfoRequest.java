package com.cropkeeper.domain.member.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMemberInfoRequest {

    @Size(min = 1, max = 20, message = "이름은 20자 이하여야 합니다.")
    private String name;

    @Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 휴대폰 번호 형식이 아닙니다. (예: 01012345678)")
    private String contact;

    public boolean hasLeastOneField() {
        return (name != null && !name.isEmpty()) ||
                (contact != null && !contact.isEmpty());
    }
}
