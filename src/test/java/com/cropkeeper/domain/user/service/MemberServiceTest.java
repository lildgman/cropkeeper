package com.cropkeeper.domain.user.service;

import com.cropkeeper.domain.user.dto.UpdateMemberInfoRequest;
import com.cropkeeper.domain.user.entity.Member;
import com.cropkeeper.domain.user.entity.MemberRole;
import com.cropkeeper.domain.user.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("회원 조회 성공")
    void findById_success() {

        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .memberId(memberId)
                .username("testuser01")
                .password("encodedPassword")
                .name("test")
                .contact("01012345678")
                .role(MemberRole.USER)
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        Member found = memberService.findById(memberId);

        // then
        assertThat(found).isNotNull();
        assertThat(found.getMemberId()).isEqualTo(memberId);
        assertThat(found.getUsername()).isEqualTo("testuser01");
        assertThat(found.getName()).isEqualTo("test");

        verify(memberRepository, times(1)).findById(memberId);
    }

    @Test
    @DisplayName("회원 조회 실패 - 존재하지 않는 회원")
    void findById_NotFound() {

        // given
        Long memberId = 999L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> memberService.findById(memberId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 회원을 찾을 수 없습니다.");

        verify(memberRepository, times(1)).findById(memberId);
    }

    @Test
    @DisplayName("회원 정보 수정 성공 - 이름, 연락처 모두 수정")
    void updateMemberInfo_Success_BothFields() {

        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .memberId(memberId)
                .username("testuser01")
                .password("encodedPassword")
                .name("test01")
                .contact("01012345678")
                .role(MemberRole.USER)
                .build();

        UpdateMemberInfoRequest request = UpdateMemberInfoRequest.builder()
                .name("test02")
                .contact("01098774423")
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        Member updatedMember = memberService.updateMemberInfo(memberId, request);

        // then
        assertThat(updatedMember.getName()).isEqualTo("test02");
        assertThat(updatedMember.getContact()).isEqualTo("01098774423");

        verify(memberRepository, times(1)).findById(memberId);

    }

    @Test
    @DisplayName("회원 정보 수정 성공 - 이름만 수정")
    void updateMemberInfo_Success_Name() {

        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .memberId(memberId)
                .username("testuser01")
                .password("encodedPassword")
                .name("test01")
                .contact("01012345678")
                .role(MemberRole.USER)
                .build();

        UpdateMemberInfoRequest request = UpdateMemberInfoRequest.builder()
                .name("test02")
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when

        Member updatedMember = memberService.updateMemberInfo(memberId, request);

        // then
        assertThat(updatedMember.getName()).isEqualTo("test02");
        assertThat(updatedMember.getContact()).isEqualTo("01012345678");

        verify(memberRepository, times(1)).findById(memberId);
    }

    @Test
    @DisplayName("회원 정보 수정 성공 - 연락처만 수정")
    void updateMemberInfo_Success_Contact() {

        // given
        Long memberId = 1L;
        Member member = Member.builder()
                .memberId(memberId)
                .username("testuser01")
                .password("encodedPassword")
                .name("test01")
                .contact("01012345678")
                .role(MemberRole.USER)
                .build();

        UpdateMemberInfoRequest request = UpdateMemberInfoRequest.builder()
                .contact("01098765432")
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        Member updatedMember = memberService.updateMemberInfo(memberId, request);

        // then
        assertThat(updatedMember.getName()).isEqualTo("test01");
        assertThat(updatedMember.getContact()).isEqualTo("01098765432");

        verify(memberRepository, times(1)).findById(memberId);
    }

    @Test
    @DisplayName("회원 정보 수정 실패 - 존재하지 않는 회원")
    void updateMemberInfo_Fail_MemberNotFound() {

        // given
        Long memberId = 999L;
        UpdateMemberInfoRequest request = UpdateMemberInfoRequest.builder()
                .name("test02")
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> memberService.updateMemberInfo(memberId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 회원을 찾을 수 없습니다.");

        verify(memberRepository, times(1)).findById(memberId);


    }

    @Test
    @DisplayName("회원 정보 수정 실패 - 빈 문자열")
    void updateMemberInfo_Fail_EmptyString() {

        // given
        Long memberId = 1L;
        UpdateMemberInfoRequest request = UpdateMemberInfoRequest.builder()
                .name("")
                .contact("")
                .build();

        // when, then
        assertThatThrownBy(() -> memberService.updateMemberInfo(memberId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("수정할 정보가 없습니다. 이름 또는 연락처를 입력해주세요.");

        verify(memberRepository, never()).findById(any());
    }
}