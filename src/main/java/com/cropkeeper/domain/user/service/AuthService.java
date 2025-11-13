package com.cropkeeper.domain.user.service;

import com.cropkeeper.domain.user.dto.AuthResponse;
import com.cropkeeper.domain.user.dto.LoginRequest;
import com.cropkeeper.domain.user.dto.RegisterRequest;
import com.cropkeeper.domain.user.entity.Users;
import com.cropkeeper.domain.user.entity.UserRole;
import com.cropkeeper.domain.user.repository.UserRepository;
import com.cropkeeper.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증/인가 관련 비즈니스 로직을 처리하는 서비스
 *
 * 주요 기능:
 * - 회원가입
 * - 로그인
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * 회원가입
     *
     * @param request 회원가입 요청 DTO
     * @return JWT 토큰을 포함한 인증 응답
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("회원가입 시도: {}", request.getUsername());

        // 1. 비밀번호 확인 검증
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 2. 사용자 이름 중복 체크
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 사용자 이름입니다: " + request.getUsername());
        }

        // 3. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 4. User 엔티티 생성
        Users users = Users.builder()
                .username(request.getUsername())
                .password(encodedPassword)
                .name(request.getName())
                .contact(request.getContact())
                .role(UserRole.USER)  // 회원가입 시 기본 권한은 USER
                .farm(null)           // 농장은 나중에 등록
                .build();

        // 5. DB에 저장
        Users savedUsers = userRepository.save(users);
        log.info("회원가입 성공: userId={}, username={}", savedUsers.getUserId(), savedUsers.getUsername());

        // 6. JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateToken(savedUsers.getUsername());

        // 7. 응답 생성
        return AuthResponse.of(accessToken, savedUsers);
    }

    /**
     * 로그인
     *
     * @param request 로그인 요청 DTO
     * @return JWT 토큰을 포함한 인증 응답
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("로그인 시도: {}", request.getUsername());

        // 1. Spring Security의 AuthenticationManager로 인증
        // - CustomUserDetailsService.loadUserByUsername() 자동 호출
        // - PasswordEncoder로 비밀번호 비교
        // - 인증 실패 시 BadCredentialsException 발생
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        log.info("로그인 인증 성공: {}", request.getUsername());

        // 2. 인증된 사용자 정보로 User 조회
        Users users = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + request.getUsername()));

        // 3. JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateToken(users.getUsername());

        log.info("로그인 성공: userId={}, username={}", users.getUserId(), users.getUsername());

        // 4. 응답 생성
        return AuthResponse.of(accessToken, users);
    }
}
