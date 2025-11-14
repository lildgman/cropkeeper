package com.cropkeeper.domain.user.controller;

import com.cropkeeper.domain.user.dto.LoginRequest;
import com.cropkeeper.domain.user.dto.RegisterRequest;
import com.cropkeeper.domain.user.repository.UserRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 통합 테스트
 *
 * 실제 Spring Context를 띄워서 전체 흐름을 테스트
 * - 회원가입/로그인 API 검증
 * - 성공/실패 케이스 모두 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional  // 각 테스트 후 DB 롤백
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // 각 테스트 전에 DB 정리
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 성공")
    void register_Success() throws Exception {
        // given
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser01")
                .password("pass1234")
                .passwordConfirm("pass1234")
                .name("홍길동")
                .contact("01012345678")
                .build();

        // when & then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())  // 요청/응답 출력
                .andExpect(status().isCreated())  // 201 Created
                .andExpect(jsonPath("$.username").value("testuser01"))
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호 불일치")
    void register_Fail_PasswordMismatch() throws Exception {
        // given
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser01")
                .password("pass1234")
                .passwordConfirm("pass5678")  // 다름!
                .name("홍길동")
                .contact("01012345678")
                .build();

        // when & then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())  // 400 Bad Request
                .andExpect(jsonPath("$.message").value("비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("회원가입 실패 - 사용자 이름 중복")
    void register_Fail_DuplicateUsername() throws Exception {
        // given - 이미 존재하는 사용자 생성
        RegisterRequest firstRequest = RegisterRequest.builder()
                .username("testuser01")
                .password("pass1234")
                .passwordConfirm("pass1234")
                .name("홍길동")
                .contact("01012345678")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)));

        // when & then - 같은 사용자 이름으로 재가입 시도
        RegisterRequest duplicateRequest = RegisterRequest.builder()
                .username("testuser01")  // 중복!
                .password("pass1234")
                .passwordConfirm("pass1234")
                .name("김철수")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("이미 사용 중인 사용자 이름입니다")));
    }

    @Test
    @DisplayName("회원가입 실패 - 입력값 검증 실패 (username 너무 짧음)")
    void register_Fail_ValidationError_UsernameTooShort() throws Exception {
        // given
        RegisterRequest request = RegisterRequest.builder()
                .username("test")  // 8자 미만
                .password("pass1234")
                .passwordConfirm("pass1234")
                .name("홍길동")
                .build();

        // when & then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증에 실패했습니다."))
                .andExpect(jsonPath("$.errors.username").exists());
    }

    @Test
    @DisplayName("회원가입 실패 - 입력값 검증 실패 (username 특수문자 포함)")
    void register_Fail_ValidationError_UsernameSpecialChar() throws Exception {
        // given
        RegisterRequest request = RegisterRequest.builder()
                .username("test-user")  // 하이픈 포함
                .password("pass1234")
                .passwordConfirm("pass1234")
                .name("홍길동")
                .build();

        // when & then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").value("사용자 이름은 영문과 숫자만 사용 가능합니다."));
    }

    @Test
    @DisplayName("회원가입 실패 - 입력값 검증 실패 (전화번호 형식 오류)")
    void register_Fail_ValidationError_InvalidPhoneNumber() throws Exception {
        // given
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser01")
                .password("pass1234")
                .passwordConfirm("pass1234")
                .name("홍길동")
                .contact("1012345678")  // 0으로 시작 안함
                .build();

        // when & then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.contact").value(containsString("올바른 휴대폰 번호 형식이 아닙니다")));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() throws Exception {
        // given - 먼저 회원가입
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("testuser01")
                .password("pass1234")
                .passwordConfirm("pass1234")
                .name("홍길동")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // when & then - 로그인
        LoginRequest loginRequest = LoginRequest.builder()
                .username("testuser01")
                .password("pass1234")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())  // 200 OK
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("testuser01"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void login_Fail_WrongPassword() throws Exception {
        // given - 먼저 회원가입
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("testuser01")
                .password("pass1234")
                .passwordConfirm("pass1234")
                .name("홍길동")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // when & then - 잘못된 비밀번호로 로그인
        LoginRequest loginRequest = LoginRequest.builder()
                .username("testuser01")
                .password("wrongpass")  // 틀린 비밀번호
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized())  // 401 Unauthorized
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 사용자")
    void login_Fail_UserNotFound() throws Exception {
        // given
        LoginRequest loginRequest = LoginRequest.builder()
                .username("nonexist")
                .password("pass1234")
                .build();

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 올바르지 않습니다."));
    }
}
