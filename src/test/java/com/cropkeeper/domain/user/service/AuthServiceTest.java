package com.cropkeeper.domain.user.service;

import com.cropkeeper.domain.user.dto.LoginRequest;
import com.cropkeeper.domain.user.dto.RegisterRequest;
import com.cropkeeper.domain.user.entity.UserRole;
import com.cropkeeper.domain.user.entity.Users;
import com.cropkeeper.domain.user.repository.UserRepository;
import com.cropkeeper.global.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * AuthService 단위 테스트
 *
 * 특징:
 * - @ExtendWith(MockitoExtension.class): Mockito 사용 (Spring 없이 빠름)
 * - @Mock: 의존성을 가짜 객체로 대체
 * - @InjectMocks: Mock들을 주입받아 테스트 대상 생성
 * - 비즈니스 로직만 집중 테스트
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("회원가입 성공")
    void register_Success() {
        // given
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser01")
                .password("pass1234")
                .passwordConfirm("pass1234")
                .name("홍길동")
                .contact("01012345678")
                .build();

        // Mock 동작 정의
        when(userRepository.existsByUsername("testuser01")).thenReturn(false);
        when(passwordEncoder.encode("pass1234")).thenReturn("$2a$10$encoded");
        when(jwtTokenProvider.generateToken("testuser01")).thenReturn("jwt-token");

        Users savedUser = Users.builder()
                .userId(1L)
                .username("testuser01")
                .password("$2a$10$encoded")
                .name("홍길동")
                .role(UserRole.USER)
                .build();
        when(userRepository.save(any(Users.class))).thenReturn(savedUser);

        // when
        var response = authService.register(request);

        // then
        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getUsername()).isEqualTo("testuser01");
        assertThat(response.getRole()).isEqualTo("USER");

        // 검증: save()가 1번 호출되었는지
        verify(userRepository, times(1)).save(any(Users.class));
        // 검증: 저장된 User의 비밀번호가 암호화되었는지
        verify(passwordEncoder, times(1)).encode("pass1234");
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호 불일치")
    void register_Fail_PasswordMismatch() {
        // given
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser01")
                .password("pass1234")
                .passwordConfirm("pass5678")  // 다름!
                .name("홍길동")
                .build();

        // when & then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");

        // 검증: 비밀번호 불일치 시 DB 작업이 일어나지 않아야 함
        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).save(any(Users.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 사용자 이름 중복")
    void register_Fail_DuplicateUsername() {
        // given
        RegisterRequest request = RegisterRequest.builder()
                .username("existing")
                .password("pass1234")
                .passwordConfirm("pass1234")
                .name("홍길동")
                .build();

        when(userRepository.existsByUsername("existing")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 사용 중인 사용자 이름입니다");

        // 검증: 중복 체크 후 save()가 호출되지 않아야 함
        verify(userRepository, times(1)).existsByUsername("existing");
        verify(userRepository, never()).save(any(Users.class));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // given
        LoginRequest request = LoginRequest.builder()
                .username("testuser01")
                .password("pass1234")
                .build();

        Users user = Users.builder()
                .userId(1L)
                .username("testuser01")
                .password("$2a$10$encoded")
                .name("홍길동")
                .role(UserRole.USER)
                .build();

        // Mock 설정
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);  // 인증 성공 (반환값은 사용 안함)
        when(userRepository.findByUsername("testuser01")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken("testuser01")).thenReturn("jwt-token");

        // when
        var response = authService.login(request);

        // then
        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getUsername()).isEqualTo("testuser01");

        // 검증
        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtTokenProvider, times(1)).generateToken("testuser01");
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void login_Fail_BadCredentials() {
        // given
        LoginRequest request = LoginRequest.builder()
                .username("testuser01")
                .password("wrongpass")
                .build();

        // authenticationManager가 예외를 던지도록 설정
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("자격 증명에 실패하였습니다."));

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        // 검증: 인증 실패 시 토큰이 생성되지 않아야 함
        verify(jwtTokenProvider, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("회원가입 시 기본 권한은 USER")
    void register_DefaultRole_IsUser() {
        // given
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser01")
                .password("pass1234")
                .passwordConfirm("pass1234")
                .name("홍길동")
                .build();

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(jwtTokenProvider.generateToken(anyString())).thenReturn("token");

        Users savedUser = Users.builder()
                .userId(1L)
                .username("testuser01")
                .role(UserRole.USER)
                .build();
        when(userRepository.save(any(Users.class))).thenReturn(savedUser);

        // when
        var response = authService.register(request);

        // then
        assertThat(response.getRole()).isEqualTo("USER");

        // 검증: save() 호출 시 role이 USER로 설정되었는지 확인
        verify(userRepository).save(argThat(user ->
                user.getRole() == UserRole.USER
        ));
    }
}
