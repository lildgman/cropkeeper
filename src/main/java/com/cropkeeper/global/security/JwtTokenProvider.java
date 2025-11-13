package com.cropkeeper.global.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT 토큰을 생성하고 검증하는 클래스
 *
 * 역할:
 * 1. 로그인 성공 시 JWT 토큰 생성
 * 2. API 요청 시 토큰 유효성 검증
 * 3. 토큰에서 사용자 정보 추출
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationTime;

    /**
     * 생성자: application.yml에서 설정값을 주입받음
     *
     * @param secretKey Base64로 인코딩된 비밀키 (256비트 이상)
     * @param expirationTime 토큰 만료 시간 (밀리초, 기본 1시간)
     */
    public JwtTokenProvider(
            @Value("${jwt.secret-key}") String secretKey,
            @Value("${jwt.expiration-time}") long expirationTime) {
        // Base64로 인코딩된 비밀키를 디코딩하여 SecretKey 객체 생성
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationTime = expirationTime;
    }

    /**
     * JWT 토큰 생성
     *
     * @param username 사용자 이름 (토큰의 subject로 저장됨)
     * @return 생성된 JWT 토큰 문자열
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .subject(username)              // 토큰 주체 (사용자 이름)
                .issuedAt(now)                  // 발급 시간
                .expiration(expiryDate)         // 만료 시간
                .signWith(secretKey)            // 비밀키로 서명
                .compact();                     // 최종 토큰 문자열 생성
    }

    /**
     * JWT 토큰에서 사용자 이름 추출
     *
     * @param token JWT 토큰
     * @return 토큰에 저장된 사용자 이름
     */
    public String getUsername(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)          // 비밀키로 검증
                .build()
                .parseSignedClaims(token)       // 토큰 파싱
                .getPayload();                  // 페이로드 추출

        return claims.getSubject();             // subject (사용자 이름) 반환
    }

    /**
     * JWT 토큰 유효성 검증
     *
     * @param token JWT 토큰
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    /**
     * JWT 토큰의 만료 시간 확인
     *
     * @param token JWT 토큰
     * @return 만료 시간 (Date 객체)
     */
    public Date getExpirationDate(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getExpiration();
    }
}
