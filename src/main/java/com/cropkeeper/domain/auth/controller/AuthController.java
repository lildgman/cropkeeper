package com.cropkeeper.domain.auth.controller;

import com.cropkeeper.domain.auth.dto.request.LoginRequest;
import com.cropkeeper.domain.auth.dto.response.LoginResponse;
import com.cropkeeper.domain.auth.dto.request.RegisterRequest;
import com.cropkeeper.domain.auth.dto.response.RegisterResponse;
import com.cropkeeper.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증/인가 관련 API 엔드포인트
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입 API
     *
     * @param request 회원가입 요청 DTO
     * @return 201 Created + JWT 토큰 및 사용자 정보
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 로그인 API
     *
     * @param request 로그인 요청 DTO
     * @return 200 OK + JWT 토큰 및 사용자 정보
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
