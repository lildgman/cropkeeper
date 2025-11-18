package com.cropkeeper.domain.auth.service;

import com.cropkeeper.domain.auth.dto.request.LoginRequest;
import com.cropkeeper.domain.auth.dto.response.LoginResponse;
import com.cropkeeper.domain.auth.dto.request.RegisterRequest;
import com.cropkeeper.domain.auth.dto.response.RegisterResponse;
import com.cropkeeper.domain.auth.exception.DeletedMemberLoginException;
import com.cropkeeper.domain.auth.exception.InvalidCredentialsException;
import com.cropkeeper.domain.auth.exception.RegisterPasswordMismatchException;
import com.cropkeeper.domain.member.entity.Member;
import com.cropkeeper.domain.member.entity.MemberRole;
import com.cropkeeper.domain.auth.exception.DuplicateUsernameException;
import com.cropkeeper.domain.member.exception.PasswordMismatchException;
import com.cropkeeper.domain.member.repository.MemberRepository;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

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

        when(memberRepository.existsByUsername("testuser01")).thenReturn(false);
        when(passwordEncoder.encode("pass1234")).thenReturn("$2a$10$encoded");

        Member savedUser = Member.builder()
                .memberId(1L)
                .username("testuser01")
                .password("$2a$10$encoded")
                .name("홍길동")
                .role(MemberRole.USER)
                .build();

        when(memberRepository.save(any(Member.class))).thenReturn(savedUser);

        // when
        RegisterResponse response = authService.register(request);

        // then
        assertThat(response.getUsername()).isEqualTo("testuser01");
        assertThat(response.getRole()).isEqualTo("USER");

        // 검증: save()가 1번 호출되었는지
        verify(memberRepository, times(1)).save(any(Member.class));
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
                .passwordConfirm("pass5678")
                .name("홍길동")
                .build();

        // when & then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RegisterPasswordMismatchException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");

        // 검증: 비밀번호 불일치 시 DB 작업이 일어나지 않아야 함
        verify(memberRepository, never()).existsByUsername(anyString());
        verify(memberRepository, never()).save(any(Member.class));
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

        when(memberRepository.existsByUsername("existing")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateUsernameException.class)
                .hasMessageContaining("이미 사용 중인 사용자 이름입니다");

        // 검증: 중복 체크 후 save()가 호출되지 않아야 함
        verify(memberRepository, times(1)).existsByUsername("existing");
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // given
        LoginRequest request = LoginRequest.builder()
                .username("testuser01")
                .password("pass1234")
                .build();

        Member user = Member.builder()
                .memberId(1L)
                .username("testuser01")
                .password("$2a$10$encoded")
                .name("홍길동")
                .role(MemberRole.USER)
                .build();

        // Mock 설정
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);  // 인증 성공 (반환값은 사용 안함)
        when(memberRepository.findByUsername("testuser01")).thenReturn(Optional.of(user));

        when(jwtTokenProvider.generateAccessToken("testuser01")).thenReturn("access-token-123");
        when(jwtTokenProvider.generateRefreshToken("testuser01")).thenReturn("refresh-token-123");

        // when
        LoginResponse response = authService.login(request);

        // then
        assertThat(response.getAccessToken()).isEqualTo("access-token-123");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token-123");
        assertThat(response.getUsername()).isEqualTo("testuser01");

        // 검증
        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtTokenProvider, times(1)).generateAccessToken("testuser01");
        verify(jwtTokenProvider, times(1)).generateRefreshToken("testuser01");
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 증명")
    void login_Fail_BadCredentials() {
        // given
        LoginRequest request = LoginRequest.builder()
                .username("testuser01")
                .password("wrongpass")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("자격 증명에 실패하였습니다."));

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("아이디 또는 비밀번호가 일치하지 않습니다.");

        // 검증: 인증 실패 시 토큰이 생성되지 않아야 함
        verify(jwtTokenProvider, never()).generateAccessToken(anyString());
        verify(jwtTokenProvider, never()).generateRefreshToken(anyString());
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

        when(memberRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        Member savedUser = Member.builder()
                .memberId(1L)
                .username("testuser01")
                .role(MemberRole.USER)
                .build();
        when(memberRepository.save(any(Member.class))).thenReturn(savedUser);

        // when
        RegisterResponse response = authService.register(request);

        // then
        assertThat(response.getRole()).isEqualTo("USER");

        // 검증: save() 호출 시 role이 USER로 설정되었는지 확인
        verify(memberRepository).save(argThat(user ->
                user.getRole() == MemberRole.USER
        ));
    }

    @Test
    @DisplayName("로그인 실패 - 탈퇴한 회원은 로그인 불가")
    void login_Fail_DeletedMember() {

        LoginRequest request = LoginRequest.builder()
                .username("deleteUser")
                .password("pass1234")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DeletedMemberLoginException("deleteUser"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(DeletedMemberLoginException.class)
                .hasMessage("탈퇴한 회원입니다. 로그인할 수 없습니다. (username: deleteUser)");

        verify(jwtTokenProvider, never()).generateAccessToken(anyString());
        verify(memberRepository, never()).findByUsername(anyString());

    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 사용자")
    void login_Fail_UserNotFound() {

        LoginRequest request = LoginRequest.builder()
                .username("testusername")
                .password("pass1234")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new UsernameNotFoundException("사용자를 찾을 수 없습니다: testusername"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UsernameNotFoundException.class);

        verify(jwtTokenProvider, never()).generateAccessToken(anyString());
    }

}
