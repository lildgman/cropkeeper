package com.cropkeeper.global.security;

import com.cropkeeper.domain.user.entity.Users;
import com.cropkeeper.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security의 UserDetailsService 구현체
 *
 * 역할:
 * - 사용자 이름(username)으로 DB에서 사용자 정보 조회
 * - User 엔티티를 UserPrincipal(UserDetails)로 변환
 * - Spring Security가 인증 시 자동으로 호출
 *
 * 사용 시점:
 * 1. 로그인 시: 사용자가 입력한 username으로 DB 조회
 * 2. JWT 검증 시: 토큰에서 추출한 username으로 DB 조회
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 사용자 이름으로 사용자 정보 조회
     *
     * Spring Security가 자동으로 호출하는 메서드
     *
     * @param username 사용자 이름 (로그인 ID)
     * @return UserDetails 객체 (UserPrincipal)
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. DB에서 사용자 조회
        Users users = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "사용자를 찾을 수 없습니다: " + username
                ));

        // 2. User 엔티티를 UserPrincipal로 변환하여 반환
        return new UserPrincipal(users);
    }

    /**
     * 사용자 ID로 사용자 정보 조회 (선택적 메서드)
     *
     * 컨트롤러에서 직접 사용할 수 있는 편의 메서드
     *
     * @param userId 사용자 ID
     * @return UserPrincipal 객체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때
     */
    @Transactional(readOnly = true)
    public UserPrincipal loadUserById(Long userId) {
        Users users = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "사용자를 찾을 수 없습니다. ID: " + userId
                ));

        return new UserPrincipal(users);
    }
}
