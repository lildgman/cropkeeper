package com.cropkeeper.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePasswordRequest {

    @NotBlank(message = "현재 비밀번호는 필수입니다.")
    @Size(min = 4, max = 16, message = "비밀번호는 4자 이상 16자 이하여야 합니다.")
    private String currentPassword;

    @NotBlank(message = "변경할 비밀번호는 필수입니다.")
    @Size(min = 4, max = 16, message = "비밀번호는 4자 이상 16자 이하여야 합니다.")
    private String newPassword;

    @NotBlank(message = "변경할 비밀번호 확인은 필수입니다.")
    @Size(min = 4, max = 16, message = "비밀번호는 4자 이상 16자 이하여야 합니다.")
    private String newPasswordConfirm;

}
