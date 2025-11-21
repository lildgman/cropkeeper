package com.cropkeeper.domain.auth.service;

import com.cropkeeper.domain.auth.dto.request.LoginRequest;
import com.cropkeeper.domain.auth.dto.response.LoginResponse;
import com.cropkeeper.domain.auth.dto.request.RegisterRequest;
import com.cropkeeper.domain.auth.dto.response.RegisterResponse;
import com.cropkeeper.domain.auth.exception.AuthErrorCode;
import com.cropkeeper.domain.auth.exception.InvalidCredentialsException;
import com.cropkeeper.domain.auth.exception.RegisterPasswordMismatchException;
import com.cropkeeper.domain.member.entity.Member;
import com.cropkeeper.domain.member.entity.MemberRole;
import com.cropkeeper.domain.auth.exception.DuplicateUsernameException;
import com.cropkeeper.domain.member.repository.MemberRepository;
import com.cropkeeper.global.security.JwtTokenProvider;
import com.cropkeeper.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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

    private final MemberRepository memberRepository;
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
    public RegisterResponse register(RegisterRequest request) {
        log.info("회원가입 시도: {}", request.getUsername());

        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new RegisterPasswordMismatchException(AuthErrorCode.REGISTER_PASSWORD_MISMATCH);
        }

        if (memberRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUsernameException(request.getUsername());
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        Member member = Member.builder()
                .username(request.getUsername())
                .password(encodedPassword)
                .name(request.getName())
                .contact(request.getContact())
                .role(MemberRole.USER)
                .build();

        Member savedMember = memberRepository.save(member);
        log.info("회원가입 성공: userId={}, username={}", savedMember.getMemberId(), savedMember.getUsername());

        return RegisterResponse.from(savedMember);
    }

    /**
     * 로그인
     *
     * @param request 로그인 요청 DTO
     * @return JWT 토큰을 포함한 인증 응답
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.info("로그인 시도: {}", request.getUsername());

        // AuthenticationManager로 인증 (내부적으로 CustomUserDetailsService.loadUserByUsername 호출)
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            log.info("로그인 인증 성공: {}", request.getUsername());
        } catch (BadCredentialsException e) {
            log.warn("로그인 실패: 잘못된 자격증명 - username: {}", request.getUsername());
            throw new InvalidCredentialsException();
        }

        // 인증된 사용자 정보 가져오기 (DB 재조회 없이)
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Member member = userPrincipal.getMember();

        String accessToken = jwtTokenProvider.generateAccessToken(member.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(member.getUsername());

        log.info("로그인 성공: memberId={}, username={}", member.getMemberId(), member.getUsername());

        return LoginResponse.of(accessToken, refreshToken, member);
    }
}
