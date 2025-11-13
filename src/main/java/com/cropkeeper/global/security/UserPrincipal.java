package com.cropkeeper.global.security;

import com.cropkeeper.domain.user.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security의 UserDetails 구현체
 *
 * 역할:
 * - User 엔티티를 Spring Security가 이해할 수 있는 형식으로 변환
 * - 인증/인가 정보를 Spring Security에 제공
 *
 * UserDetails 인터페이스의 필수 메서드:
 * - getUsername(): 사용자 식별자
 * - getPassword(): 비밀번호
 * - getAuthorities(): 권한 목록
 * - isAccountNonExpired(): 계정 만료 여부
 * - isAccountNonLocked(): 계정 잠김 여부
 * - isCredentialsNonExpired(): 비밀번호 만료 여부
 * - isEnabled(): 계정 활성화 여부
 */
@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private final Users users;  // 우리의 User 엔티티

    /**
     * 권한 목록 반환
     *
     * @return 사용자의 권한 목록 (예: ROLE_USER, ROLE_ADMIN)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // UserRole enum을 Spring Security의 권한 형식으로 변환
        // 예: UserRole.USER -> "ROLE_USER"
        return Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + users.getRole().name())
        );
    }

    /**
     * 비밀번호 반환
     */
    @Override
    public String getPassword() {
        return users.getPassword();
    }

    /**
     * 사용자 이름(로그인 ID) 반환
     */
    @Override
    public String getUsername() {
        return users.getUsername();
    }

    /**
     * 계정 만료 여부
     * @return true면 만료되지 않음
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;  // 우리 시스템에서는 계정 만료 기능 없음
    }

    /**
     * 계정 잠김 여부
     * @return true면 잠기지 않음
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;  // 우리 시스템에서는 계정 잠금 기능 없음
    }

    /**
     * 비밀번호 만료 여부
     * @return true면 만료되지 않음
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // 우리 시스템에서는 비밀번호 만료 기능 없음
    }

    /**
     * 계정 활성화 여부
     * @return true면 활성화됨
     */
    @Override
    public boolean isEnabled() {
        return true;  // 우리 시스템에서는 모든 계정이 활성화됨
    }

    /**
     * User 엔티티의 ID 조회 편의 메서드
     */
    public Long getId() {
        return users.getUserId();
    }

    /**
     * User 엔티티의 이름 조회 편의 메서드
     */
    public String getName() {
        return users.getName();
    }
}
