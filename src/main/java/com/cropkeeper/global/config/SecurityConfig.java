package com.cropkeeper.global.config;

import com.cropkeeper.global.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정 클래스
 *
 * 역할:
 * - Spring Security의 전체 보안 정책 설정
 * - JWT 인증 필터 등록
 * - URL별 접근 권한 설정
 * - 비밀번호 암호화 방식 설정
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // @PreAuthorize, @PostAuthorize 등의 메서드 보안 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Spring Security 필터 체인 설정
     *
     * 이 메서드가 Spring Security의 핵심 설정을 담당합니다.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT 사용 시 필요 없음)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 설정 (필요 시 추가 설정)
                .cors(AbstractHttpConfigurer::disable)

                // 세션 사용 안 함 (JWT는 stateless)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 경로
                        .requestMatchers(
                                "/api/auth/**",          // 로그인, 회원가입
                                "/api/public/**",        // 공개 API
                                "/error"                 // 에러 페이지
                        ).permitAll()

                        // Thymeleaf 뷰 접근 설정 (필요시)
                        .requestMatchers("/", "/login", "/register").permitAll()

                        // 나머지 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 전에 추가
                // 즉, 모든 요청이 컨트롤러에 도달하기 전에 JWT 필터가 먼저 실행됨
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 비밀번호 암호화 인코더
     *
     * BCrypt 알고리즘을 사용하여 비밀번호를 암호화합니다.
     * - 회원가입 시: 평문 비밀번호를 암호화하여 DB에 저장
     * - 로그인 시: 입력한 비밀번호를 암호화하여 DB의 암호화된 비밀번호와 비교
     *
     * @return BCryptPasswordEncoder 객체
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager Bean 등록
     *
     * 로그인 처리 시 사용됩니다.
     * - 사용자가 입력한 username과 password를 검증
     * - CustomUserDetailsService와 PasswordEncoder를 사용하여 인증
     *
     * @param authConfig Spring이 자동으로 주입
     * @return AuthenticationManager 객체
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
