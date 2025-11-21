package com.cropkeeper.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원가입 요청 DTO
 * 클라이언트가 회원가입 시 전송하는 데이터
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "사용자 이름은 필수입니다.")
    @Size(min = 4, max = 16, message = "사용자 이름은 4자 이상 16자 이하여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "사용자 이름은 영문과 숫자만 사용 가능합니다.")
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]+$",
             message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String password;

    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    private String passwordConfirm;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 20, message = "이름은 20자 이하여야 합니다.")
    private String name;

    @Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 휴대폰 번호 형식이 아닙니다. (예: 01012345678)")
    private String contact;  // 선택 사항
}
