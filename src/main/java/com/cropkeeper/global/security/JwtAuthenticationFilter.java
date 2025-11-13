package com.cropkeeper.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 *
 * 역할:
 * - 모든 HTTP 요청을 가로채서 JWT 토큰을 검증
 * - 유효한 토큰이면 Spring Security의 SecurityContext에 인증 정보 저장
 * - OncePerRequestFilter를 상속받아 요청당 한 번만 실행됨
 *
 * 실행 시점: 모든 API 요청 전에 자동 실행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    /**
     * 필터의 핵심 메서드 - 모든 요청마다 실행됨
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param filterChain 다음 필터로 요청을 전달하는 체인
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. 요청 헤더에서 JWT 토큰 추출
            String jwt = getJwtFromRequest(request);

            // 2. 토큰이 존재하고 유효한지 검증
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                // 3. 토큰에서 사용자 이름 추출
                String username = jwtTokenProvider.getUsername(jwt);

                // 4. 사용자 이름으로 DB에서 사용자 정보 조회
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 5. 인증 객체 생성
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,            // 인증된 사용자 정보
                                null,                   // 비밀번호 (JWT에서는 불필요)
                                userDetails.getAuthorities()  // 권한 정보
                        );

                // 6. 요청 정보를 인증 객체에 추가
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 7. SecurityContext에 인증 정보 저장
                // 이후 @AuthenticationPrincipal로 현재 사용자 정보 조회 가능
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT 인증 성공: {}", username);
            }
        } catch (Exception e) {
            log.error("SecurityContext에 사용자 인증 정보를 설정할 수 없습니다.", e);
        }

        // 8. 다음 필터로 요청 전달 (중요!)
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청 헤더에서 JWT 토큰 추출
     *
     * Authorization 헤더 형식: "Bearer {JWT 토큰}"
     * 예시: "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     *
     * @param request HTTP 요청
     * @return JWT 토큰 문자열 (없으면 null)
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // Authorization 헤더가 있고, "Bearer "로 시작하는지 확인
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // "Bearer " 이후의 토큰 부분만 반환 (7번째 문자부터)
            return bearerToken.substring(7);
        }

        return null;
    }
}
