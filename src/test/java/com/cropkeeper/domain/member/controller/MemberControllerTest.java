package com.cropkeeper.domain.member.controller;

import com.cropkeeper.domain.auth.dto.request.LoginRequest;
import com.cropkeeper.domain.auth.dto.request.RegisterRequest;
import com.cropkeeper.domain.member.dto.request.UpdateMemberInfoRequest;
import com.cropkeeper.domain.member.dto.request.UpdatePasswordRequest;
import com.cropkeeper.domain.member.entity.Member;
import com.cropkeeper.domain.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    private Long testMemberId;
    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {

        // db 정리
        memberRepository.deleteAll();

        // 테스트용 회원 생성
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("testuser01")
                .password("Pass123!")
                .passwordConfirm("Pass123!")
                .name("test01")
                .contact("01012345678")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        Member member = memberRepository.findByUsername("testuser01").orElseThrow();
        testMemberId = member.getMemberId();

        LoginRequest loginRequest = LoginRequest.builder()
                .username("testuser01")
                .password("Pass123!")
                .build();

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(loginResponse);
        accessToken = jsonNode.get("accessToken").asText();

    }

    @Test
    @DisplayName("회원 정보 조회 성공")
    void getMember_Success() throws Exception {

        mockMvc.perform(get("/api/members/{memberId}", testMemberId)
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(testMemberId))
                .andExpect(jsonPath("$.username").value("testuser01"))
                .andExpect(jsonPath("$.name").value("test01"))
                .andExpect(jsonPath("$.contact").value("01012345678"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("회원 정보 조회 실패 - 토큰 없음")
    void getMember_Fail_NoToken() throws Exception {

        mockMvc.perform(get("/api/members/{memberId}", testMemberId))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("회원 정보 조회 실패 - 로그인한 회원과 다른 회원")
    void getMember_NotFound() throws Exception {

        // given
        Long nonExistId = 999L;

        mockMvc.perform(get("/api/members/{memberId}", nonExistId)
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isForbidden())  // 403 Forbidden
                .andExpect(jsonPath("$.message").value(containsString("접근 권한이 없습니다.")));

    }

    @Test
    @DisplayName("회원 정보 수정 성공 - 이름, 연락처 모두 수정")
    void updateMemberInfo_Success_BothFields() throws Exception {

        UpdateMemberInfoRequest request = UpdateMemberInfoRequest.builder()
                .name("updatedName")
                .contact("01098765432")
                .build();

        mockMvc.perform(put("/api/members/{memberId}", testMemberId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(testMemberId))
                .andExpect(jsonPath("$.username").value("testuser01"))
                .andExpect(jsonPath("$.name").value("updatedName"))
                .andExpect(jsonPath("$.contact").value("01098765432"))
                .andExpect(jsonPath("$.role").value("USER"));

    }

    @Test
    @DisplayName("회원 정보 수정 성공 - 이름만 수정")
    void updateMemberInfo_Success_Name() throws Exception {

        // given
        UpdateMemberInfoRequest request = UpdateMemberInfoRequest.builder()
                .name("updatedName")
                .build();

        // when, then
        mockMvc.perform(put("/api/members/{memberId}", testMemberId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("updatedName"))
                .andExpect(jsonPath("$.contact").value("01012345678"));

    }

    @Test
    @DisplayName("회원 정보 수정 성공 - 연락처만 변경")
    void updateMemberInfo_Success_Contact() throws Exception {

        // given
        UpdateMemberInfoRequest request = UpdateMemberInfoRequest.builder()
                .contact("01098765432")
                .build();

        mockMvc.perform(put("/api/members/{memberId}", testMemberId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test01"))
                .andExpect(jsonPath("$.contact").value("01098765432"));
    }

    @Test
    @DisplayName("회원 정보 수정 실패 - 정보 누락")
    void updateMemberInfo_Fail_NoFields() throws Exception {

        UpdateMemberInfoRequest request = UpdateMemberInfoRequest.builder()
                .build();

        mockMvc.perform(put("/api/members/{memberId}", testMemberId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("수정할 정보가 없습니다. 수정할 이름 또는 연락처를 입력해주세요."));
    }

    @Test
    @DisplayName("회원 정보 수정 실패 - 잘못된 전화번호 양식")
    void updateMemberInfo_Fail_InvalidPhoneNumber() throws Exception {

        UpdateMemberInfoRequest request = UpdateMemberInfoRequest.builder()
                .name("test02")
                .contact("1234567890")
                .build();

        mockMvc.perform(put("/api/members/{memberId}", testMemberId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.contact").value(containsString("올바른 휴대폰 번호 형식이 아닙니다")));
    }

    @Test
    @DisplayName("회원 정보 수정 실패 - 긴 이름")
    void updateMemberInfo_Fail_NameLength() throws Exception {

        UpdateMemberInfoRequest request = UpdateMemberInfoRequest.builder()
                .name("가".repeat(21))
                .build();

        mockMvc.perform(put("/api/members/{memberId}", testMemberId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.name").value(containsString("이름은")));

    }

    @Test
    @DisplayName("회원 정보 수정 실패 - 빈 문자열")
    void updateNameInfo_Fail_EmptyString() throws Exception {

        UpdateMemberInfoRequest request = UpdateMemberInfoRequest.builder()
                .name("")
                .contact("")
                .build();

        mockMvc.perform(put("/api/members/{memberId}", testMemberId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.contact").exists());
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_Success() throws Exception {

        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .currentPassword("Pass123!")
                .newPassword("NewPass1!")
                .newPasswordConfirm("NewPass1!")
                .build();

        mockMvc.perform(patch("/api/members/{memberId}/password", testMemberId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNoContent());

        LoginRequest newLogin = LoginRequest.builder()
                .username("testuser01")
                .password("NewPass1!")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLogin)))
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("비밀번호 번경 실패 - 인증 토큰 없음")
    void changePassword_Fail_NoToken() throws Exception {

        // given
        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .currentPassword("Pass123!")
                .newPassword("NewPass1!")
                .newPasswordConfirm("NewPass1!")
                .build();

        // when ,then
        mockMvc.perform(patch("/api/members/{memberId}/password", testMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("다른 회원의 비밀번호 변경 시도 - 실패")
    void changePassword_Fail_Unauthorized() throws Exception {

        RegisterRequest anotherUser = RegisterRequest.builder()
                .username("testuser02")
                .password("Pass123!")
                .passwordConfirm("Pass123!")
                .name("test02")
                .contact("01098765432")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(anotherUser)))
                .andExpect(status().isCreated());

        Member anotherMember = memberRepository.findByUsername("testuser02").orElseThrow();
        Long anotherMemberId = anotherMember.getMemberId();

        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .currentPassword("Pass123!")
                .newPassword("NewPass1!")
                .newPasswordConfirm("NewPass1!")
                .build();

        mockMvc.perform(patch("/api/members/{memberId}/password", anotherMemberId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())  // 403 Forbidden
                .andExpect(jsonPath("$.message").value(containsString("접근 권한이 없습니다.")));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
    void changePassword_Fail_WrongPassword() throws Exception {

        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .currentPassword("Wrong789#")
                .newPassword("NewPass1!")
                .newPasswordConfirm("NewPass1!")
                .build();

        mockMvc.perform(patch("/api/members/{memberId}/password", testMemberId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("현재 비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 새 비밀번호 불일치")
    void changePassword_Fail_NewPasswordMismatch() throws Exception {

        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .currentPassword("Pass123!")
                .newPassword("NewPass1!")
                .newPasswordConfirm("DiffPass1@")
                .build();

        mockMvc.perform(patch("/api/members/{memberId}/password", testMemberId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("새 비밀번호가 일치하지 않습니다."));

    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 새 비밀번호와 현재 비밀번호 일치")
    void changePassword_Fail_SameCurrentPassword() throws Exception {

        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .currentPassword("Pass123!")
                .newPassword("Pass123!")
                .newPasswordConfirm("Pass123!")
                .build();

        mockMvc.perform(patch("/api/members/{memberId}/password", testMemberId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("새 비밀번호는 현재 비밀번호와 달라야합니다."));

    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 너무 긴 새 비밀번호")
    void changePassword_Fail_LongNewPassword() throws Exception {

        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .currentPassword("Pass123!")
                .newPassword("VeryLongPass123!@#$%&")  // 21자
                .newPasswordConfirm("VeryLongPass123!@#$%&")
                .build();

        mockMvc.perform(patch("/api/members/{memberId}/password", testMemberId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.newPassword").value(containsString("20자 이하")));

    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 값 누락")
    void changePassword_Fail_MissingFields() throws Exception {

        String requestJson = "{\"currentPassword\": \"Pass123!\"}";

        mockMvc.perform(patch("/api/members/{memberId}/password", testMemberId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.newPassword").exists())
                .andExpect(jsonPath("$.errors.newPasswordConfirm").exists());

    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void deleteMember_Success() throws Exception {

        mockMvc.perform(delete("/api/members/{memberId}", testMemberId)
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNoContent());

        LoginRequest loginRequest = LoginRequest.builder()
                .username("testuser01")
                .password("pass1234")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 토큰 없음")
    void deleteMember_Fail_NoToken() throws Exception {

        mockMvc.perform(delete("/api/members/{memberId}", testMemberId))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 다른 사용자 계정에서 탈퇴 시도")
    void deleteMember_Fail_UnauthorizedAccess() throws Exception {

        RegisterRequest anotherUserRequest = RegisterRequest.builder()
                .username("testuser02")
                .password("Pass123!")
                .passwordConfirm("Pass123!")
                .name("test02")
                .contact("01098765432")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(anotherUserRequest)))
                .andExpect(status().isCreated());

        Member anotherMember = memberRepository.findByUsername("testuser02").orElseThrow();
        Long anotherMemberId = anotherMember.getMemberId();

        mockMvc.perform(delete("/api/members/{memberId}", anotherMemberId)
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isForbidden())  // 403 Forbidden
                .andExpect(jsonPath("$.message").value(containsString("접근 권한이 없습니다.")));

    }
}